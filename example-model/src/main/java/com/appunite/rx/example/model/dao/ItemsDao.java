package com.appunite.rx.example.model.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.ItemsService;
import com.appunite.rx.example.model.api.ItemsServiceFake;
import com.appunite.rx.example.model.model.Item;
import com.appunite.rx.example.model.model.ItemWithBody;
import com.appunite.rx.example.model.model.Response;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class ItemsDao {
    @Nonnull
    private final Observable<ResponseOrError<Response>> data;
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();
    @Nonnull
    private final LoadingCache<String, ItemDao> cache;

    /*
        Normally we rather use dagger instead of static, but for testing purposes is ok
         */
    private static final Object LOCK = new Object();
    private static ItemsDao itemsDao;

    @Nonnull
    private Scheduler networkScheduler;
    @Nonnull
    private ItemsService itemsService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    public static ItemsDao getInstance(@Nonnull Scheduler networkScheduler) {
        synchronized (LOCK) {
            if (itemsDao != null) {
                return itemsDao;
            }
            itemsDao = new ItemsDao(networkScheduler, new ItemsServiceFake());
            return itemsDao;
        }
    }

    public ItemsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final ItemsService itemsService) {
        this.networkScheduler = networkScheduler;
        this.itemsService = itemsService;

        final OperatorMergeNextToken<Response, Object> mergeNextToken = OperatorMergeNextToken
                .create(new Func1<Response, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(@Nullable final Response response) {
                        if (response == null) {
                            return itemsService.listItems(null)
                                    .subscribeOn(networkScheduler);
                        } else {
                            final String nextToken = response.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            final Observable<Response> apiRequest = itemsService.listItems(nextToken)
                                    .subscribeOn(networkScheduler);
                            return Observable.just(response).zipWith(apiRequest, new MergeTwoResponses());
                        }

                    }
                });

        data = loadMoreSubject.startWith((Object) null)
                .lift(mergeNextToken)
                .compose(ResponseOrError.<Response>toResponseOrErrorObservable())
                .compose(MoreOperators.<Response>repeatOnError(networkScheduler))
                .compose(MoreOperators.<ResponseOrError<Response>>refresh(refreshSubject))
                .compose(MoreOperators.<ResponseOrError<Response>>cacheWithTimeout(networkScheduler));

        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, ItemDao>() {
                    @Override
                    public ItemDao load(@Nonnull final String id) throws Exception {
                        return new ItemDao(id);
                    }
                });
    }

    @Nonnull
    public ItemDao itemDao(@Nonnull final String id) {
        return cache.getUnchecked(id);
    }

    @Nonnull
    public Observer<Object> loadMoreObserver() {
        return loadMoreSubject;
    }

    @Nonnull
    public Observable<ResponseOrError<Response>> dataObservable() {
        return data;
    }

    @Nonnull
    public Observer<Object> refreshObserver() {
        return refreshSubject;
    }

    public class ItemDao {
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final Observable<ResponseOrError<ItemWithBody>> data;

        public ItemDao(@Nonnull String id) {

            data = itemsService.getItem(id)
                    .compose(ResponseOrError.<ItemWithBody>toResponseOrErrorObservable())
                    .compose(MoreOperators.<ItemWithBody>repeatOnError(networkScheduler))
                    .compose(MoreOperators.<ResponseOrError<ItemWithBody>>refresh(refreshSubject))
                    .compose(MoreOperators.<ResponseOrError<ItemWithBody>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<ItemWithBody>> dataObservable() {
            return data;
        }

        @Nonnull
        public Observer<Object> refreshObserver() {
            return refreshSubject;
        }
    }

    private static class MergeTwoResponses implements rx.functions.Func2<Response, Response, Response> {
        @Override
        public Response call(Response previous, Response moreData) {
            final ImmutableList<Item> items = ImmutableList.<Item>builder()
                    .addAll(previous.items())
                    .addAll(moreData.items())
                    .build();
            return new Response(moreData.title(), items, moreData.nextToken());
        }
    }


}
