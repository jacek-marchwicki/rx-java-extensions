package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.operators.OperatorSampleWithLastWithObservable;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class PostPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PostsDao postsDao;
    @Nonnull
    private final PublishSubject<String> nameSubject= PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> bodySubject= PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> sendSubject= PublishSubject.create();
    @Nonnull
    private final Observable<ResponseOrError<Response>> postSucces;

    public PostPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull PostsDao postsDao) {

        this.uiScheduler = uiScheduler;
        this.postsDao = postsDao;

        Observable.combineLatest(
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

        postSucces = postsDao.getPostSuccesObserver();
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
    public Observable<Response> postSuccesObservable(){
        return this.postSucces.compose(ResponseOrError.<Response>onlySuccess());
    }
    @Nonnull
    public Observable<Throwable> postErrorObservable(){
        return this.postSucces.compose(ResponseOrError.<Response>onlyError());
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(postSucces)))
                .distinctUntilChanged();

    }
}
