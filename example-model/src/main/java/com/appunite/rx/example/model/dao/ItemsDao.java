package com.appunite.rx.example.model.dao;

import com.appunite.gson.AndroidUnderscoreNamingStrategy;
import com.appunite.gson.ImmutableListDeserializer;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.ItemsService;
import com.appunite.rx.example.model.api.ItemsServiceFake;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.Response;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.Executor;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
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


    private static class SyncExecutor implements Executor {
        @Override
        public void execute(@Nonnull final Runnable command) {
            command.run();
        }
    }

    public static ItemsDao getInstance(@Nonnull Scheduler networkScheduler) {
        synchronized (LOCK) {
            if (itemsDao != null) {
                return itemsDao;
            }
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
                    .setFieldNamingStrategy(new AndroidUnderscoreNamingStrategy())
                    .create();

            final RestAdapter restAdapter = new RestAdapter.Builder()
                    .setClient(new OkClient(new OkHttpClient()))
                    .setEndpoint("https://atlantean-field-90117.appspot.com/_ah/api/guestbook/")
                    .setExecutors(new SyncExecutor(), new SyncExecutor())
                    .setConverter(new GsonConverter(gson))
                    .build();
            final ItemsService itemsService = restAdapter.create(ItemsService.class);
            itemsDao = new ItemsDao(networkScheduler, itemsService);
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
        private final Observable<ResponseOrError<PostWithBody>> data;

        public ItemDao(@Nonnull String id) {

            data = itemsService.getItem(id)
                    .compose(ResponseOrError.<PostWithBody>toResponseOrErrorObservable())
                    .compose(MoreOperators.<PostWithBody>repeatOnError(networkScheduler))
                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>refresh(refreshSubject))
                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<PostWithBody>> dataObservable() {
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
            final ImmutableList<Post> posts = ImmutableList.<Post>builder()
                    .addAll(previous.items())
                    .addAll(moreData.items())
                    .build();
            return new Response(moreData.title(), posts, moreData.nextToken());
        }
    }


}
