package com.appunite.rx.example.model.dao;

import com.appunite.gson.AndroidUnderscoreNamingStrategy;
import com.appunite.gson.ImmutableListDeserializer;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.GuestbookService;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostId;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.PostsIdsResponse;
import com.appunite.rx.example.model.model.PostsResponse;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class PostsDao {
    @Nonnull
    private final Observable<ResponseOrError<PostsResponse>> posts;
    @Nonnull
    private final Observable<ResponseOrError<PostsIdsResponse>> postsIds;
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
    private final Scheduler networkScheduler;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final GuestbookService guestbookService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();


    private static class SyncExecutor implements Executor {
        @Override
        public void execute(@Nonnull final Runnable command) {
            command.run();
        }
    }

    public static PostsDao getInstance(@Nonnull File cacheDirectory, @Nonnull Scheduler networkScheduler, @Nonnull Scheduler uiScheduler) {
        synchronized (LOCK) {
            if (postsDao != null) {
                return postsDao;
            }
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
                    .setFieldNamingStrategy(new AndroidUnderscoreNamingStrategy())
                    .create();

            final OkHttpClient client = new OkHttpClient();
            client.setCache(getCacheOrNull(cacheDirectory));

            final RestAdapter restAdapter = new RestAdapter.Builder()
                    .setClient(new OkClient(client))
                    .setEndpoint("https://atlantean-field-90117.appspot.com/_ah/api/guestbook/")
                    .setExecutors(new SyncExecutor(), new SyncExecutor())
                    .setConverter(new GsonConverter(gson))
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("Retrofit"))
                    .build();
            final GuestbookService guestbookService = restAdapter.create(GuestbookService.class);
            postsDao = new PostsDao(networkScheduler, uiScheduler, guestbookService);
            return postsDao;
        }
    }

    @Nullable
    private static Cache getCacheOrNull(@Nonnull File cacheDirectory) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        try {
            return new Cache(cacheDirectory, cacheSize);
        } catch (IOException e) {
            return null;
        }
    }

    public PostsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final Scheduler uiScheduler,
                    @Nonnull final GuestbookService guestbookService) {
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
        this.guestbookService = guestbookService;

        final OperatorMergeNextToken<PostsResponse, Object> mergePostsNextToken =
                OperatorMergeNextToken
                .create(new Func1<PostsResponse, Observable<PostsResponse>>() {
                    @Override
                    public Observable<PostsResponse> call(@Nullable final PostsResponse response) {
                        if (response == null) {
                            return guestbookService.listPosts(null)
                                    .subscribeOn(networkScheduler);
                        } else {
                            final String nextToken = response.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            final Observable<PostsResponse> apiRequest = guestbookService
                                    .listPosts(nextToken)
                                    .subscribeOn(networkScheduler);
                            return Observable.just(response)
                                    .zipWith(apiRequest,
                                            new MergeTwoResponses());
                        }

                    }
                });

        posts = loadMoreSubject.startWith((Object) null)
                .lift(mergePostsNextToken)
                .compose(ResponseOrError.<PostsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<PostsResponse>repeatOnError(networkScheduler))
                .compose(MoreOperators.<ResponseOrError<PostsResponse>>refresh(refreshSubject))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(MoreOperators.<ResponseOrError<PostsResponse>>cacheWithTimeout(uiScheduler));

        final OperatorMergeNextToken<PostsIdsResponse, Object> mergePostsIdsNextToken = OperatorMergeNextToken
                .create(new Func1<PostsIdsResponse, Observable<PostsIdsResponse>>() {
                    @Override
                    public Observable<PostsIdsResponse> call(@Nullable final PostsIdsResponse response) {
                        if (response == null) {
                            return guestbookService.listPostsIds(null)
                                    .subscribeOn(networkScheduler);
                        } else {
                            final String nextToken = response.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            final Observable<PostsIdsResponse> apiRequest = guestbookService.listPostsIds(nextToken)
                                    .subscribeOn(networkScheduler);
                            return Observable.just(response).zipWith(apiRequest, new MergeTwoPostsIdsResponses());
                        }

                    }
                });

        postsIds = loadMoreSubject.startWith((Object) null)
                .lift(mergePostsIdsNextToken)
                .compose(ResponseOrError.<PostsIdsResponse>toResponseOrErrorObservable())
                .compose(MoreOperators.<PostsIdsResponse>repeatOnError(networkScheduler))
                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>refresh(refreshSubject))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>cacheWithTimeout(uiScheduler));

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
    public Observable<ResponseOrError<PostsResponse>> postsObservable() {
        return posts;
    }

    @Nonnull
    public Observable<ResponseOrError<PostsIdsResponse>> postsIdsObservable() {
        return postsIds;
    }

    @Nonnull
    public Observer<Object> refreshObserver() {
        return refreshSubject;
    }

    public class PostDao {
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final Observable<ResponseOrError<PostWithBody>> postWithBodyObservable;

        public PostDao(@Nonnull String id) {
            postWithBodyObservable = guestbookService.getPost(id)
                    .compose(ResponseOrError.<PostWithBody>toResponseOrErrorObservable())
                    .compose(MoreOperators.<PostWithBody>repeatOnError(networkScheduler))
                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>refresh(refreshSubject))
                    .subscribeOn(networkScheduler)
                    .observeOn(uiScheduler)
                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>cacheWithTimeout(uiScheduler));
        }

        @Nonnull
        public Observable<ResponseOrError<PostWithBody>> postWithBodyObservable() {
            return postWithBodyObservable;
        }

        @Nonnull
        public Observable<ResponseOrError<Post>> postObservable() {
            return postWithBodyObservable
                    .compose(ResponseOrError.map(new Func1<PostWithBody, Post>() {
                        @Override
                        public Post call(PostWithBody o) {
                            return o;
                        }
                    }));
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


    private class MergeTwoPostsIdsResponses implements rx.functions.Func2<PostsIdsResponse, PostsIdsResponse, PostsIdsResponse> {
        @Override
        public PostsIdsResponse call(PostsIdsResponse previous, PostsIdsResponse moreData) {
            final ImmutableList<PostId> posts = ImmutableList.<PostId>builder()
                    .addAll(previous.items())
                    .addAll(moreData.items())
                    .build();
            return new PostsIdsResponse(moreData.title(), posts, moreData.nextToken());
        }
    }
}
