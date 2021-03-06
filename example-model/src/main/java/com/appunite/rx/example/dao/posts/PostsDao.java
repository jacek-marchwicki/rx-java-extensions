/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.example.dao.posts;

import com.appunite.cache.Cache;
import com.appunite.login.CurrentLoggedInUserDao;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.dao.internal.helpers.CacheProvider;
import com.appunite.rx.example.dao.internal.helpers.RequestHelper;
import com.appunite.rx.example.dao.posts.model.AddPost;
import com.appunite.rx.example.dao.posts.model.Post;
import com.appunite.rx.example.dao.posts.model.PostWithBody;
import com.appunite.rx.example.dao.posts.model.PostsIdsResponse;
import com.appunite.rx.example.dao.posts.model.PostsResponse;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.rx.operators.OperatorMergeNextToken;
import com.appunite.rx.subjects.CacheSubject;

import java.util.ArrayList;
import java.util.List;

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
    private final Cache<String, PostDao> cache;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final PostsService postsService;
    @Nonnull
    private final PublishSubject<Object> loadMoreSubject = PublishSubject.create();
    @Nonnull
    private final CurrentLoggedInUserDao currentLoggedInUserDao;

    public PostsDao(@Nonnull final Scheduler networkScheduler,
                    @Nonnull final PostsService postsService,
                    @Nonnull final CacheProvider cacheProvider,
                    @Nonnull final CurrentLoggedInUserDao currentLoggedInUserDao) {
        this.networkScheduler = networkScheduler;
        this.postsService = postsService;
        this.currentLoggedInUserDao = currentLoggedInUserDao;

        posts = currentLoggedInUserDao
                .currentLoggedInUserObservable()
                .compose(ResponseOrError.switchMap(new Func1<CurrentLoggedInUserDao.LoggedInUserDao, Observable<ResponseOrError<PostsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<PostsResponse>> call(@Nonnull CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao) {
                        return loadMoreSubject.startWith((Object) null)
                                .lift(loadMorePosts(networkScheduler, postsService, loggedInUserDao))
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
                                .lift(loadMorePostsIds(networkScheduler, postsService, loggedInUserDao))
                                .compose(ResponseOrError.<PostsIdsResponse>toResponseOrErrorObservable())
                                .compose(MoreOperators.<PostsIdsResponse>repeatOnError(networkScheduler))
                                .subscribeOn(networkScheduler)
                                .unsubscribeOn(networkScheduler)
                                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>refresh(refreshSubject))
                                .compose(MoreOperators.<ResponseOrError<PostsIdsResponse>>cacheWithTimeout(networkScheduler));
                    }
                }));

        cache = new Cache<>(new Cache.CacheProvider<String, PostDao>() {
            @Nonnull
            @Override
            public PostDao load(@Nonnull String id) {
                return new PostDao(id);
            }
        });
    }

    @Nonnull
    private OperatorMergeNextToken<PostsIdsResponse, Object> loadMorePostsIds(@Nonnull final Scheduler networkScheduler,
                                                                              @Nonnull final PostsService postsService,
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
                                return new PostsIdsResponse(
                                        moreData.title(),
                                        concatTwoLists(previous.items(), moreData.items()),
                                        moreData.nextToken());
                            }
                        };
                    }

                    @Nonnull
                    private Observable<PostsIdsResponse> createRequest(@Nullable final String nextToken) {
                        return RequestHelper.request(loggedInUserDao, networkScheduler,
                                new Func1<String, Observable<PostsIdsResponse>>() {
                                    @Override
                                    public Observable<PostsIdsResponse> call(String authorization) {
                                        return postsService.listPostsIds(authorization, nextToken);
                                    }
                                });
                    }
                });
    }

    @Nonnull
    private OperatorMergeNextToken<PostsResponse, Object> loadMorePosts(@Nonnull final Scheduler networkScheduler,
                                                                        @Nonnull final PostsService postsService,
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
                                return new PostsResponse(
                                        moreData.title(),
                                        concatTwoLists(previous.items(), moreData.items()),
                                        moreData.nextToken());
                            }
                        };
                    }

                    @Nonnull
                    private Observable<PostsResponse> createRequest(@Nullable final String nextToken) {
                        return RequestHelper.request(loggedInUserDao, networkScheduler,
                                new Func1<String, Observable<PostsResponse>>() {
                                    @Override
                                    public Observable<PostsResponse> call(String authorization) {
                                        return postsService.listPosts(authorization, nextToken);
                                    }
                                });
                    }
                });
    }

    @Nonnull
    public PostDao postDao(@Nonnull final String id) {
        return cache.get(id);
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
                        return postsService.createPost(authorization, post);
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
                                            return postsService.getPost(authorization, id);
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


    @Nonnull
    private static <T> List<T> concatTwoLists(@Nonnull List<T> firstList, @Nonnull List<T> secondList) {
        final ArrayList<T> posts = new ArrayList<>(firstList.size() + secondList.size());
        posts.addAll(firstList);
        posts.addAll(secondList);
        return posts;
    }
}
