package com.appunite.rx.example.ui.posts.create;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.dao.posts.PostsDao;
import com.appunite.rx.example.dao.posts.model.AddPost;
import com.appunite.rx.example.dao.posts.model.PostWithBody;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.OperatorSampleWithLastWithObservable;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

class CreatePostPresenter {

    @Nonnull
    private final BehaviorSubject<CharSequence> nameSubject = BehaviorSubject.<CharSequence>create("");
    @Nonnull
    private final BehaviorSubject<CharSequence> bodySubject = BehaviorSubject.<CharSequence>create("");
    @Nonnull
    private final BehaviorSubject<Boolean> showProgress = BehaviorSubject.create(false);
    @Nonnull
    private final PublishSubject<Object> sendSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> navigationClickSubject = PublishSubject.create();
    @Nonnull
    private final ConnectableObservable<ResponseOrError<PostWithBody>> addPostResult;

    CreatePostPresenter(@Nonnull final PostsDao postsDao,
                               @Nonnull final Scheduler uiScheduler) {
        addPostResult = Observable
                .combineLatest(
                        nameSubject.map(Functions1.toStringFunction()),
                        bodySubject.map(Functions1.toStringFunction()),
                        new Func2<String, String, AddPost>() {
                            @Override
                            public AddPost call(String name, String body) {
                                return new AddPost(name, body);
                            }
                        })
                .lift(OperatorSampleWithLastWithObservable.<AddPost>create(sendSubject))
                .filter(new Func1<AddPost, Boolean>() {
                    @Override
                    public Boolean call(AddPost addPost) {
                        final boolean nameIsPresent = !Strings.isNullOrEmpty(addPost.name());
                        final boolean bodyIsPresent = !Strings.isNullOrEmpty(addPost.body());
                        return (nameIsPresent && bodyIsPresent);
                    }
                })
                .flatMap(new Func1<AddPost, Observable<ResponseOrError<PostWithBody>>>() {
                    @Override
                    public Observable<ResponseOrError<PostWithBody>> call(AddPost addPost) {
                        return postsDao.postRequestObserver(addPost)
                                .observeOn(uiScheduler)
                                .doOnSubscribe(new Action0() {
                                    @Override
                                    public void call() {
                                        showProgress.onNext(true);
                                    }
                                })
                                .doOnNext(new Action1<ResponseOrError<PostWithBody>>() {
                                    @Override
                                    public void call(ResponseOrError<PostWithBody> postWithBodyResponseOrError) {
                                        showProgress.onNext(false);
                                    }
                                });
                    }
                })
                .publish();

        addPostResult.connect();
    }

    @Nonnull
    Observer<CharSequence> bodyObservable() {
        return bodySubject;
    }

    @Nonnull
    Observer<CharSequence> nameObservable() {
        return nameSubject;
    }

    @Nonnull
    Observer<Object> sendObservable() {
        return sendSubject;
    }

    @Nonnull
    Observable<Object> finishActivityObservable() {
        return Observable.merge(
                addPostResult.compose(ResponseOrError.<PostWithBody>onlySuccess()),
                navigationClickSubject);
    }

    @Nonnull
    Observable<Throwable> postErrorObservable() {
        return addPostResult
                .compose(ResponseOrError.<PostWithBody>onlyError());
    }

    @Nonnull
    Observer<Object> navigationClickObserver() {
        return navigationClickSubject;
    }

    @Nonnull
    Observable<Object> showBodyIsEmptyErrorObservable() {
        return bodySubject
                .lift(OperatorSampleWithLastWithObservable.<CharSequence>create(sendSubject))
                .map(Functions1.toStringFunction())
                .filter(fieldNullOrEmpty())
                .map(Functions1.toObject());
    }

    @Nonnull
    Observable<Object> showNameIsEmptyErrorObservable() {
        return nameSubject
                .lift(OperatorSampleWithLastWithObservable.<CharSequence>create(sendSubject))
                .map(Functions1.toStringFunction())
                .filter(fieldNullOrEmpty())
                .map(Functions1.toObject());
    }

    @Nonnull
    public Observable<Boolean> progressObservable() {
        return showProgress;
    }

    @Nonnull
    private Func1<String, Boolean> fieldNullOrEmpty() {
        return new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                return Strings.isNullOrEmpty(s);
            }
        };
    }

}
