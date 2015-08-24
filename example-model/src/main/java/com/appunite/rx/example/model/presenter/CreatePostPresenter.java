package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.operators.OnSubscribeCombineLatestWithoutBackPressure;
import com.appunite.rx.operators.OperatorSampleWithLastWithObservable;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class CreatePostPresenter {

    @Nonnull
    private final PostsDao postsDao;
    @Nonnull
    private final PublishSubject<String> nameSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> bodySubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> sendSubject = PublishSubject.create();
    @Nonnull
    private final Observable<Object> closeActivitySubject;
    @Nonnull
    private final PublishSubject<Object> navigationClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> nameErrorSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> bodyErrorSubject = PublishSubject.create();
    @Nonnull
    private final BehaviorSubject<Boolean> showProgress = BehaviorSubject.create(false);

    public CreatePostPresenter(@Nonnull PostsDao postsDao) {

        this.postsDao = postsDao;

        OnSubscribeCombineLatestWithoutBackPressure.combineLatest(
                nameSubject,
                bodySubject,
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
                        return (nameIsPresent || bodyIsPresent);
                    }
                })
                .doOnNext(new Action1<AddPost>() {
                    @Override
                    public void call(AddPost addPost) {
                        showProgress.onNext(true);
                    }
                })
                .subscribe(postsDao.postRequestObserver());

        closeActivitySubject = Observable.merge(
                postsDao.postSuccesObserver().compose(ResponseOrError.<PostWithBody>onlySuccess()),
                navigationClickSubject);

        nameSubject.startWith("")
                .lift(OperatorSampleWithLastWithObservable.<String>create(sendSubject))
                .filter(fieldNullOrEmpty())
                .subscribe(nameErrorSubject);

        bodySubject.startWith("")
                .lift(OperatorSampleWithLastWithObservable.<String>create(sendSubject))
                .filter(fieldNullOrEmpty())
                .subscribe(bodyErrorSubject);

        postsDao.postSuccesObserver()
                .doOnNext(new Action1<ResponseOrError<PostWithBody>>() {
                    @Override
                    public void call(ResponseOrError<PostWithBody> postWithBodyResponseOrError) {
                        showProgress.onNext(false);
                    }
                });
    }

    @Nonnull
    public Observer<String> bodyObservable() {
        return bodySubject;
    }

    @Nonnull
    public Observer<String> nameObservable() {
        return nameSubject;
    }

    @Nonnull
    public Observer<Object> sendObservable() {
        return sendSubject;
    }

    @Nonnull
    public Observable<Object> finishActivityObservable() {
        return closeActivitySubject;
    }

    @Nonnull
    public Observable<Throwable> postErrorObservable() {
        return postsDao.postSuccesObserver().compose(ResponseOrError.<PostWithBody>onlyError());
    }

    @Nonnull
    public Observer<Object> navigationClickObserver() {
        return navigationClickSubject;
    }

    @Nonnull
    public Observable<Object> showBodyIsEmptyErrorObservable() {
        return bodyErrorSubject;
    }

    @Nonnull
    public Observable<Object> showNameIsEmptyErrorObservable() {
        return nameErrorSubject;
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
