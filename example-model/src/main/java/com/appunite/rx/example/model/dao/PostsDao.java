package com.appunite.rx.example.model.dao;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.GuestbookService;
import com.appunite.rx.example.model.helpers.CacheProvider;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.PostId;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.PostsIdsResponse;
import com.appunite.rx.example.model.model.PostsResponse;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.appunite.rx.subjects.CacheSubject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class PostsDao {
    @Nonnull
    private final Observable<ResponseOrError<PostsResponse>> posts;
    @Nonnull
    private final Observable<ResponseOrError<PostsIdsResponse>> postsIds;
    @Nonnull
    private final PublishSubject<AddPost> sendPost= PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();
    @Nonnull
    private final LoadingCache<String, PostDao> cache;
    @Nonnull
    private final PublishSubject<ResponseOrError<PostWithBody>> postSuccesSubject = PublishSubject.create();
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final GuestbookService guestbookService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();

    public PostsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final Scheduler uiScheduler,
                    @Nonnull final GuestbookService guestbookService,
                    @Nonnull final CacheProvider cacheProvider) {
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

        sendPost
                .flatMap(new Func1<AddPost, Observable<ResponseOrError<PostWithBody>>>() {
                    @Override
                    public Observable<ResponseOrError<PostWithBody>> call(AddPost post) {
                        return guestbookService.createPost(post)
                                .subscribeOn(networkScheduler)
                                .compose(ResponseOrError.<PostWithBody>toResponseOrErrorObservable());
                    }
                })
                .observeOn(uiScheduler)
                .subscribe(postSuccesSubject);


        posts = loadMoreSubject.startWith((Object) null)
                .lift(mergePostsNextToken)
                .compose(CacheSubject.behaviorRefCount(cacheProvider.<PostsResponse>getCacheCreatorForKey("posts", PostsResponse.class)))
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

    @Nonnull
    public Observable<ResponseOrError<PostWithBody>> postSuccesObserver() {
        return postSuccesSubject;
    }

    @Nonnull
    public Observer<AddPost> postRequestObserver() {
        return sendPost;
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
