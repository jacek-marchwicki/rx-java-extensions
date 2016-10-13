package com.appunite.rx.example.model.dao;

import com.appunite.login.CurrentLoggedInUserDao;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.api.GuestbookService;
import com.appunite.rx.example.model.helpers.CacheProvider;
import com.appunite.rx.example.model.helpers.RequestHelper;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.Post;
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
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final GuestbookService guestbookService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final CurrentLoggedInUserDao currentLoggedInUserDao;

    public PostsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final GuestbookService guestbookService,
                    @Nonnull final CacheProvider cacheProvider,
                    @Nonnull final CurrentLoggedInUserDao currentLoggedInUserDao) {
        this.networkScheduler = networkScheduler;
        this.guestbookService = guestbookService;
        this.currentLoggedInUserDao = currentLoggedInUserDao;

        posts = currentLoggedInUserDao
                .currentLoggedInUserObservable()
                .compose(ResponseOrError.switchMap(new Func1<CurrentLoggedInUserDao.LoggedInUserDao, Observable<ResponseOrError<PostsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<PostsResponse>> call(@Nonnull CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
                        return loadMoreSubject.startWith((Object) null)
                                .lift(loadMorePosts(networkScheduler, guestbookService, loggedInUserDao))
                                .compose(CacheSubject.behaviorRefCount(cacheProvider.<PostsResponse>getCacheCreatorForKey("user: " + loggedInUserDao.userId() +", posts", PostsResponse.class)))
                                .compose(ResponseOrError.<PostsResponse>toResponseOrErrorObservable())
                                .compose(MoreOperators.<PostsResponse>repeatOnError(networkScheduler))
                                .subscribeOn(networkScheduler)
                                .unsubscribeOn(networkScheduler)
                                .compose(MoreOperators.<ResponseOrError<PostsResponse>>refresh(refreshSubject))
                                .compose(MoreOperators.<ResponseOrError<PostsResponse>>cacheWithTimeout(networkScheduler));
                    }
                }));

        postsIds = currentLoggedInUserDao
                .currentLoggedInUserObservable()
                .compose(ResponseOrError.switchMap(new Func1<CurrentLoggedInUserDao.LoggedInUserDao, Observable<ResponseOrError<PostsIdsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<PostsIdsResponse>> call(CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
                        return loadMoreSubject.startWith((Object) null)
                                .lift(loadMorePostsIds(networkScheduler, guestbookService, loggedInUserDao))
                                .compose(ResponseOrError.<PostsIdsResponse>toResponseOrErrorObservable())
                                .compose(MoreOperators.<PostsIdsResponse>repeatOnError(networkScheduler))
                                .subscribeOn(networkScheduler)
                                .unsubscribeOn(networkScheduler)
                                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>refresh(refreshSubject))
                                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>cacheWithTimeout(networkScheduler));
                    }
                }));

        cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, PostDao>() {
                    @Override
                    public PostDao load(@Nonnull final String id) throws Exception {
                        return new PostDao(id);
                    }
                });
    }

    @Nonnull
    private OperatorMergeNextToken<PostsIdsResponse, Object> loadMorePostsIds(@Nonnull final Scheduler networkScheduler,
                                                                              @Nonnull final GuestbookService guestbookService,
                                                                              @Nonnull final CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
        return OperatorMergeNextToken
                .create(new Func1<PostsIdsResponse, Observable<PostsIdsResponse>>() {
                    @Override
                    public Observable<PostsIdsResponse> call(@Nullable final PostsIdsResponse previous) {
                        if (previous == null) {
                            return createRequest(null);
                        } else {
                            final String nextToken = previous.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            return createRequest(nextToken)
                                    .map(joinWithPreviousResponse(previous));
                        }

                    }

                    @Nonnull
                    private Func1<PostsIdsResponse, PostsIdsResponse> joinWithPreviousResponse(@Nonnull final PostsIdsResponse previous) {
                        return new Func1<PostsIdsResponse, PostsIdsResponse>() {
                            @Override
                            public PostsIdsResponse call(PostsIdsResponse moreData) {
                                final ImmutableList<PostId> posts = ImmutableList.<PostId>builder()
                                        .addAll(previous.items())
                                        .addAll(moreData.items())
                                        .build();
                                return new PostsIdsResponse(moreData.title(), posts, moreData.nextToken());
                            }
                        };
                    }

                    @Nonnull
                    private Observable<PostsIdsResponse> createRequest(@Nullable final String nextToken) {
                        return RequestHelper.request(loggedInUserDao, networkScheduler,
                                new Func1<String, Observable<PostsIdsResponse>>() {
                                    @Override
                                    public Observable<PostsIdsResponse> call(String authorization) {
                                        return guestbookService.listPostsIds(authorization, nextToken);
                                    }
                                });
                    }
                });
    }

    @Nonnull
    private OperatorMergeNextToken<PostsResponse, Object> loadMorePosts(@Nonnull final Scheduler networkScheduler,
                                                                        @Nonnull final GuestbookService guestbookService,
                                                                        @Nonnull final CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
        return OperatorMergeNextToken
                .create(new Func1<PostsResponse, Observable<PostsResponse>>() {
                    @Override
                    public Observable<PostsResponse> call(@Nullable final PostsResponse previous) {
                        if (previous == null) {
                            return createRequest(null);
                        } else {
                            final String nextToken = previous.nextToken();
                            if (nextToken == null) {
                                return Observable.never();
                            }
                            return createRequest(nextToken)
                                    .map(joinWithPreviousResponse(previous));
                        }

                    }

                    @Nonnull
                    private Func1<PostsResponse, PostsResponse> joinWithPreviousResponse(@Nonnull final PostsResponse previous) {
                        return new Func1<PostsResponse, PostsResponse>() {
                            @Override
                            public PostsResponse call(PostsResponse moreData) {
                                final ImmutableList<Post> posts = ImmutableList.<Post>builder()
                                        .addAll(previous.items())
                                        .addAll(moreData.items())
                                        .build();
                                return new PostsResponse(moreData.title(), posts, moreData.nextToken());
                            }
                        };
                    }

                    @Nonnull
                    private Observable<PostsResponse> createRequest(@Nullable final String nextToken) {
                        return RequestHelper.request(loggedInUserDao, networkScheduler,
                                new Func1<String, Observable<PostsResponse>>() {
                                    @Override
                                    public Observable<PostsResponse> call(String authorization) {
                                        return guestbookService.listPosts(authorization, nextToken);
                                    }
                                });
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
    public Observable<ResponseOrError<PostWithBody>> postRequestObserver(@Nonnull final AddPost post) {
        return currentLoggedInUserDao.currentLoggedInUserObservable()
                .first()
                .compose(ResponseOrError.flatMap(new Func1<CurrentLoggedInUserDao.LoggedInUserDao, Observable<ResponseOrError<PostWithBody>>>() {
                    @Override
                    public Observable<ResponseOrError<PostWithBody>> call(CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
                        return postRequestObserver(loggedInUserDao, post);
                    }
                }));
    }


    @Nonnull
    private Observable<ResponseOrError<PostWithBody>> postRequestObserver(@Nonnull CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao, @Nonnull final AddPost post) {
        return RequestHelper.request(loggedInUserDao, networkScheduler,
                new Func1<String, Observable<PostWithBody>>() {
                    @Override
                    public Observable<PostWithBody> call(String authorization) {
                        return guestbookService.createPost(authorization, post);
                    }
                })
                .doOnNext(new Action1<PostWithBody>() {
                    @Override
                    public void call(PostWithBody postWithBody) {
                        refreshSubject.doOnNext(null);
                    }
                })
                .compose(ResponseOrError.<PostWithBody>toResponseOrErrorObservable());
    }

    public class PostDao {
        @Nonnull
        private final PublishSubject<Object> refreshSubject = PublishSubject.create();
        @Nonnull
        private final Observable<ResponseOrError<PostWithBody>> postWithBodyObservable;

        PostDao(@Nonnull final String id) {
            postWithBodyObservable = currentLoggedInUserDao.currentLoggedInUserObservable()
                    .compose(ResponseOrError.switchMap(new Func1<CurrentLoggedInUserDao.LoggedInUserDao, Observable<ResponseOrError<PostWithBody>>>() {
                        @Override
                        public Observable<ResponseOrError<PostWithBody>> call(CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
                            return RequestHelper.request(loggedInUserDao, networkScheduler,
                                    new Func1<String, Observable<PostWithBody>>() {
                                        @Override
                                        public Observable<PostWithBody> call(String authorization) {
                                            return guestbookService.getPost(authorization, id);
                                        }
                                    })
                                    .compose(ResponseOrError.<PostWithBody>toResponseOrErrorObservable())
                                    .compose(MoreOperators.<PostWithBody>repeatOnError(networkScheduler))
                                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>refresh(refreshSubject))
                                    .compose(MoreOperators.<ResponseOrError<PostWithBody>>cacheWithTimeout(networkScheduler));
                        }
                    }));
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

}
