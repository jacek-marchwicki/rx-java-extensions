package com.appunite.rx.example.model.presenter;

import com.appunite.login.CurrentLoggedInUserDao;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.example.model.dao.MyCurrentLoggedInUserDao;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostId;
import com.appunite.rx.example.model.model.PostsIdsResponse;
import com.appunite.rx.example.model.model.PostsResponse;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.functions.FunctionsN;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class MainPresenter {

    @Nonnull
    private final Observable<String> titleObservable;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> itemsObservable;
    @Nonnull
    private final Observable<Throwable> errorObservable;
    @Nonnull
    private final Observable<Boolean> progressObservable;
    @Nonnull
    private final Subject<AdapterItem, AdapterItem> openDetailsSubject = PublishSubject.create();
    @Nonnull
    private final PostsDao postsDao;
    @Nonnull
    private final MyCurrentLoggedInUserDao currentLoggedInUserDao;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PublishSubject<Object> clickOnFabSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> clickLogoutSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> clickLoginSubject = PublishSubject.create();

    public MainPresenter(@Nonnull final PostsDao postsDao,
                         @Nonnull final MyCurrentLoggedInUserDao currentLoggedInUserDao,
                         @Nonnull Scheduler uiScheduler) {
        this.postsDao = postsDao;
        this.currentLoggedInUserDao = currentLoggedInUserDao;
        this.uiScheduler = uiScheduler;

        // Two solutions - you can choose one
        if (true) {
            final Observable<ResponseOrError<PostsResponse>> postsObservable = postsDao.postsObservable()
                    .observeOn(uiScheduler)
                    .replay(1)
                    .refCount();

            titleObservable = postsObservable
                    .map(new Func1<ResponseOrError<PostsResponse>, String>() {
                        @Override
                        public String call(ResponseOrError<PostsResponse> responseOrError) {
                            return responseOrError.isError() ? "" : responseOrError.data().title();
                        }
                    });

            itemsObservable = postsObservable
                    .map(new Func1<ResponseOrError<PostsResponse>, List<Post>>() {
                        @Override
                        public List<Post> call(ResponseOrError<PostsResponse> responseOrError) {
                            return responseOrError.isError() ? Collections.<Post>emptyList() : responseOrError.data().items();
                        }
                    })
                    .switchMap(new Func1<List<Post>, Observable<? extends List<BaseAdapterItem>>>() {
                        @Override
                        public Observable<? extends List<BaseAdapterItem>> call(List<Post> items) {
                            return Observable.from(items)
                                    .map(new Func1<Post, BaseAdapterItem>() {
                                        @Override
                                        public BaseAdapterItem call(Post post) {
                                            return new AdapterItem(post.id(), post.name());
                                        }
                                    })
                                    .toList();
                        }
                    });

            errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                    ResponseOrError.transform(postsObservable)))
                    .distinctUntilChanged();

            progressObservable = ResponseOrError.combineProgressObservable(ImmutableList.of(
                    ResponseOrError.transform(postsObservable)));
        } else {
            final Observable<ResponseOrError<PostsIdsResponse>> postIds = postsDao.postsIdsObservable()
                    .observeOn(uiScheduler)
                    .replay(1)
                    .refCount();
            itemsObservable =  postIds
                    .compose(ResponseOrError.map(new Func1<PostsIdsResponse, List<PostId>>() {
                        @Override
                        public List<PostId> call(PostsIdsResponse postsIdsResponse) {
                            return postsIdsResponse.items();
                        }
                    }))
                    .map(new Func1<ResponseOrError<List<PostId>>, List<PostId>>() {
                        @Override
                        public List<PostId> call(ResponseOrError<List<PostId>> responseOrError) {
                            return responseOrError.isError() ? Collections.<PostId>emptyList() : responseOrError.data();
                        }
                    })
                    .compose(MoreOperators.observableSwitch(new Func1<PostId, Observable<BaseAdapterItem>>() {
                        @Override
                        public Observable<BaseAdapterItem> call(final PostId postId) {
                            return postsDao.postDao(postId.id()).postObservable()
                                    .map(new Func1<ResponseOrError<Post>, BaseAdapterItem>() {
                                        @Override
                                        public BaseAdapterItem call(ResponseOrError<Post> postResponseOrError) {
                                            if (postResponseOrError.isData()) {
                                                final Post post = postResponseOrError.data();
                                                return new AdapterItem(post.id(), post.name());
                                            } else {
                                                return new ErrorAdapterItem(postId.id(), postResponseOrError.error());
                                            }
                                        }
                                    });
                        }
                    }))
                    .onBackpressureLatest()
                    .observeOn(uiScheduler);

            titleObservable = postIds
                    .map(new Func1<ResponseOrError<PostsIdsResponse>, String>() {
                        @Override
                        public String call(ResponseOrError<PostsIdsResponse> responseOrError) {
                            return responseOrError.isError() ? "" : responseOrError.data().title();
                        }
                    });


            errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                    ResponseOrError.transform(postIds)))
                    .distinctUntilChanged();

            progressObservable = Observable.combineLatest(Arrays.asList(postIds, itemsObservable), FunctionsN.returnFalse())
                    .startWith(true);
        }
    }

    @Nonnull
    public Observable<AdapterItem> openDetailsObservable() {
        return openDetailsSubject;
    }

    @Nonnull
    public Observable<String> titleObservable() {
        return titleObservable;
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> itemsObservable() {
        return itemsObservable;
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return errorObservable;

    }

    @Nonnull
    public Observable<Boolean> progressObservable() {
        return progressObservable;
    }

    @Nonnull
    public Observer<Object> loadMoreObserver() {
        return postsDao.loadMoreObserver();
    }

    @Nonnull
    public Observer<Object> clickOnFabObserver() {
        return clickOnFabSubject;
    }

    @Nonnull
    public Observable<Object> startCreatePostActivityObservable() {
        return clickOnFabSubject;
    }

    @Nonnull
    public Observer<Object> clickLoginObserver() {
        return clickLoginSubject;
    }

    @Nonnull
    public Observer<Object> clickLogoutObserver() {
        return clickLogoutSubject;
    }

    @Nonnull
    public Observable<Boolean> loginVisibleObservable() {
        return currentLoggedInUserDao.currentLoggedInUserObservable()
                .map(new Func1<ResponseOrError<CurrentLoggedInUserDao.LoggedInUserDao>, Boolean>() {
                    @Override
                    public Boolean call(ResponseOrError<CurrentLoggedInUserDao.LoggedInUserDao> loggedInUserDaoResponseOrError) {
                        return loggedInUserDaoResponseOrError.isError()
                                && loggedInUserDaoResponseOrError.error() instanceof CurrentLoggedInUserDao.NotAuthorizedException;
                    }
                })
                .observeOn(uiScheduler)
                .startWith(false);
    }

    @Nonnull
    public Observable<Boolean> logoutVisibleObservable() {
        return currentLoggedInUserDao.currentLoggedInUserObservable()
                .map(new Func1<ResponseOrError<CurrentLoggedInUserDao.LoggedInUserDao>, Boolean>() {
                    @Override
                    public Boolean call(ResponseOrError<CurrentLoggedInUserDao.LoggedInUserDao> loggedInUserDaoResponseOrError) {
                        return loggedInUserDaoResponseOrError.isData();
                    }
                })
                .observeOn(uiScheduler)
                .startWith(false);
    }

    @Nonnull
    public Observable<Object> startFirebaseLoginActivity() {
        return clickLoginSubject;
    }

    @Nonnull
    public Observable<Object> logoutUserObservable() {
        return clickLogoutSubject;
    }

    public static class ErrorAdapterItem implements BaseAdapterItem {

        @Nonnull
        private final String id;
        @Nonnull
        private final Throwable error;

        ErrorAdapterItem(@Nonnull String id, @Nonnull Throwable error) {
            this.id = id;
            this.error = error;
        }

        @Nonnull
        public Throwable error() {
            return error;
        }

        @Override
        public long adapterId() {
            return id.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof ErrorAdapterItem && Objects.equal(id, ((ErrorAdapterItem)item).id);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return equals(item);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ErrorAdapterItem)) return false;
            final ErrorAdapterItem that = (ErrorAdapterItem) o;
            return Objects.equal(id, that.id) &&
                    Objects.equal(error, that.error);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, error);
        }
    }

    public class AdapterItem implements BaseAdapterItem {

        @Nonnull
        private final String id;
        @Nullable
        private final String text;

        AdapterItem(@Nonnull String id,
                           @Nullable String text) {
            this.id = id;
            this.text = text;
        }

        @Nonnull
        public String id() {
            return id;
        }

        @Nullable
        public String text() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AdapterItem)) return false;

            final AdapterItem that = (AdapterItem) o;

            return id.equals(that.id) && !(text != null ? !text.equals(that.text) : that.text != null);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (text != null ? text.hashCode() : 0);
            return result;
        }

        @Override
        public long adapterId() {
            return id.hashCode();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem item) {
            return item instanceof AdapterItem && Objects.equal(id, ((AdapterItem)item).id);
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem item) {
            return equals(item);
        }

        @Nonnull
        public Observer<Object> clickObserver() {
            return Observers.create(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    openDetailsSubject.onNext(AdapterItem.this);
                }
            });
        }

    }
}
