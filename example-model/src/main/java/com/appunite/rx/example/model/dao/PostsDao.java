package com.appunite.rx.example.model.dao;

import com.appunite.gson.AndroidUnderscoreNamingStrategy;
import com.appunite.gson.ImmutableListDeserializer;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.GuestbookService;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.PostsResponse;
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
public class PostsDao {
    @Nonnull
    private final Observable<ResponseOrError<PostsResponse>> data;
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();
    @Nonnull
    private final LoadingCache<String, PostDao> cache;

    /*
        Normally we rather use dagger instead of static, but for testing purposes is ok
         */
    private static final Object LOCK = new Object();
    private static PostsDao postsDao;

    @Nonnull
    private Scheduler networkScheduler;
    @Nonnull
    private GuestbookService guestbookService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();


    private static class SyncExecutor implements Executor {
        @Override
        public void execute(@Nonnull final Runnable command) {
            command.run();
        }
    }

    public static PostsDao getInstance(@Nonnull Scheduler networkScheduler) {
        synchronized (LOCK) {
            if (postsDao != null) {
                return postsDao;
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
            final GuestbookService guestbookService = restAdapter.create(GuestbookService.class);
            postsDao = new PostsDao(networkScheduler, guestbookService);
            return postsDao;
        }
    }

    public PostsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final GuestbookService guestbookService) {
        this.networkScheduler = networkScheduler;
        this.guestbookService = guestbookService;

        final OperatorMergeNextToken<PostsResponse, Object> mergeNextToken = OperatorMergeNextToken
                .create(new Func1<PostsResponse, Observable<PostsResponse>>() {
                    @Override
                    public Observable<PostsResponse> call(@Nullable final PostsResponse response) {
                        if (response == null) {
                            return guestbookService.listItems(null)
                                    .subscribeOn(networkScheduler);
                        } else {
                            final String nextToken = response.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            final Observable<PostsResponse> apiRequest = guestbookService.listItems(nextToken)
                                    .subscribeOn(networkScheduler);
                            return Observable.just(response).zipWith(apiRequest, new MergeTwoResponses());
                        }

                    }
                });

        data = loadMoreSubject.startWith((Object) null)
                .lift(mergeNextToken)
                .compose(ResponseOrError.<PostsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<PostsResponse>repeatOnError(networkScheduler))
                .compose(MoreOperators.<ResponseOrError<PostsResponse>>refresh(refreshSubject))
                .compose(MoreOperators.<ResponseOrError<PostsResponse>>cacheWithTimeout(networkScheduler));

        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, PostDao>() {
                    @Override
                    public PostDao load(@Nonnull final String id) throws Exception {
                        return new PostDao(id);
                    }
                });
    }

    @Nonnull
    public PostDao postDao(@Nonnull final String id) {
        return cache.getUnchecked(id);
    }

    @Nonnull
    public Observer<Object> loadMoreObserver() {
        return loadMoreSubject;
    }

    @Nonnull
    public Observable<ResponseOrError<PostsResponse>> dataObservable() {
        return data;
    }

    @Nonnull
    public Observer<Object> refreshObserver() {
        return refreshSubject;
    }

    public class PostDao {
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final Observable<ResponseOrError<PostWithBody>> data;

        public PostDao(@Nonnull String id) {

            data = guestbookService.getItem(id)
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

    private static class MergeTwoResponses implements rx.functions.Func2<PostsResponse, PostsResponse, PostsResponse> {
        @Override
        public PostsResponse call(PostsResponse previous, PostsResponse moreData) {
            final ImmutableList<Post> posts = ImmutableList.<Post>builder()
                    .addAll(previous.items())
                    .addAll(moreData.items())
                    .build();
            return new PostsResponse(moreData.title(), posts, moreData.nextToken());
        }
    }


}
