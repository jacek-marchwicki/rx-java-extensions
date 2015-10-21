package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.functions.Functions1;
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
    private final PublishSubject<CharSequence> nameSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<CharSequence> bodySubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> sendSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> closeActivitySubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> navigationClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> nameErrorSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> bodyErrorSubject = PublishSubject.create();
    @Nonnull
    private final BehaviorSubject<Boolean> showProgress = BehaviorSubject.create(false);
    @Nonnull
    private final PublishSubject<Throwable> postErrorSubject = PublishSubject.create();

    public CreatePostPresenter(@Nonnull PostsDao postsDao) {
        Observable.combineLatest(
                nameSubject,
                bodySubject,
                new Func2<CharSequence, CharSequence, AddPost>() {
                    @Override
                    public AddPost call(CharSequence name, CharSequence body) {
                        return new AddPost(name.toString(), body.toString());
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
                .doOnNext(new Action1<AddPost>() {
                    @Override
                    public void call(AddPost addPost) {
                        showProgress.onNext(true);
                    }
                })
                .subscribe(postsDao.postRequestObserver());

        Observable.merge(
                postsDao.postSuccesObservable().compose(ResponseOrError.<PostWithBody>onlySuccess()),
                navigationClickSubject)
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        showProgress.onNext(false);
                    }
                })
                .subscribe(closeActivitySubject);

        postsDao.postSuccesObservable()
                .compose(ResponseOrError.<PostWithBody>onlyError())
                .doOnNext(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showProgress.onNext(false);
                    }
                })
                .subscribe(postErrorSubject);

        nameSubject.startWith("")
                .lift(OperatorSampleWithLastWithObservable.<CharSequence>create(sendSubject))
                .map(Functions1.charSequenceToString())
                .filter(fieldNullOrEmpty())
                .subscribe(nameErrorSubject);

        bodySubject.startWith("")
                .lift(OperatorSampleWithLastWithObservable.<CharSequence>create(sendSubject))
                .map(Functions1.charSequenceToString())
                .filter(fieldNullOrEmpty())
                .subscribe(bodyErrorSubject);
    }

    @Nonnull
    public Observer<CharSequence> bodyObservable() {
        return bodySubject;
    }

    @Nonnull
    public Observer<CharSequence> nameObservable() {
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
        return postErrorSubject;
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
