package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.operators.OperatorSampleWithLastWithObservable;
import com.google.common.collect.ImmutableList;
import com.appunite.rx.operators.OnSubscribeCombineLatestWithoutBackPressure;
import javax.annotation.Nonnull;

import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class CreatePostPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
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

    public CreatePostPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                               @Nonnull PostsDao postsDao) {

        this.uiScheduler = uiScheduler;
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
                .subscribe(postsDao.postRequestObserver());

        closeActivitySubject = OnSubscribeCombineLatestWithoutBackPressure.combineLatest(
                postsDao.postSuccesObserver().compose(ResponseOrError.<Response>onlySuccess()),
                navigationClickSubject.startWith((Object) null),
                new Func2<Response, Object, Object>() {
                    @Override
                    public Object call(Response response, Object o) {
                        return new Object();
                    }
                }
        );

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
        return postsDao.postSuccesObserver().compose(ResponseOrError.<Response>onlyError());
    }

    @Nonnull
    public Observer<Object> navigationClickObserver() {
        return navigationClickSubject;
    }
}
