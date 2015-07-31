package com.appunite.rx.example.model.presenter;

import com.appunite.detector.SimpleDetector;
import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostId;
import com.appunite.rx.example.model.model.PostsIdsResponse;
import com.appunite.rx.example.model.model.PostsResponse;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class MainPresenter {

    @Nonnull
    private final Observable<ResponseOrError<String>> titleObservable;
    @Nonnull
    private final Observable<ResponseOrError<List<AdapterItem>>> itemsObservable;
    @Nonnull
    private final Subject<AdapterItem, AdapterItem> openDetailsSubject = PublishSubject.create();
    @Nonnull
    private final PostsDao postsDao;

    public MainPresenter(@Nonnull PostsDao postsDao) {
        this.postsDao = postsDao;
        titleObservable = postsObservable()
                .compose(ResponseOrError.map(new Func1<PostsResponse, String>() {
                    @Override
                    public String call(PostsResponse postsResponse) {
                        return Strings.nullToEmpty(postsResponse.title());
                    }
                }))
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        itemsObservable = postsObservable()
                .compose(ResponseOrError.map(new Func1<PostsResponse, List<AdapterItem>>() {
                    @Override
                    public List<AdapterItem> call(PostsResponse postsResponse) {
                        final List<Post> posts = postsResponse.items();
                        return FluentIterable.from(posts).transform(new Function<Post, AdapterItem>() {
                            @Nonnull
                            @Override
                            public AdapterItem apply(Post input) {
                                return new AdapterItem(input.id(), input.name());
                            }
                        }).toList();
                    }
                }))
                .compose(ObservableExtensions.<ResponseOrError<List<AdapterItem>>>behaviorRefCount());
    }

    @Nonnull
    private Observable<ResponseOrError<PostsResponse>> postsObservable() {
        return this.postsDao.postsObservable();
    }

    @Nonnull
    private Observable<ResponseOrError<PostsResponse>> postsObservable2() {
        return this.postsDao.postsIdsObservable()
                .compose(ResponseOrError.switchMap(new Func1<PostsIdsResponse, Observable<ResponseOrError<PostsResponse>>>() {
                    @Override
                    public Observable<ResponseOrError<PostsResponse>> call(final PostsIdsResponse o) {

                        return Observable.from(o.items())
                                .map(new Func1<PostId, Observable<ResponseOrError<Post>>>() {
                                    @Override
                                    public Observable<ResponseOrError<Post>> call(PostId postId) {
                                        return postsDao.postDao(postId.id()).postObservable();
                                    }
                                })
                                .toList()
                                .compose(MoreOperators.<ResponseOrError<Post>>newCombineAll())
                                .compose(ResponseOrError.<Post>newFromListObservable())
                                .compose(ResponseOrError.map(new Func1<List<Post>, PostsResponse>() {
                                    @Override
                                    public PostsResponse call(List<Post> posts) {
                                        return new PostsResponse(o.title(), posts, o.nextToken());
                                    }
                                }));
                    }
                }));
    }

    @Nonnull
    public Observable<AdapterItem> openDetailsObservable() {
        return openDetailsSubject;
    }

    @Nonnull
    public Observable<String> titleObservable() {
        return titleObservable.compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    public Observable<List<AdapterItem>> itemsObservable() {
        return itemsObservable.compose(ResponseOrError.<List<AdapterItem>>onlySuccess());
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)))
                .distinctUntilChanged();

    }

    @Nonnull
    public Observable<Boolean> progressObservable() {
        return ResponseOrError.combineProgressObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)));
    }

    @Nonnull
    public Observer<Object> loadMoreObserver() {
        return postsDao.loadMoreObserver();
    }

    public class AdapterItem implements SimpleDetector.Detectable<AdapterItem> {

        @Nonnull
        private final String id;
        @Nullable
        private final String text;

        public AdapterItem(@Nonnull String id,
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
        public boolean matches(@Nonnull AdapterItem item) {
            return Objects.equal(id, item.id);
        }

        @Override
        public boolean same(@Nonnull AdapterItem item) {
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
