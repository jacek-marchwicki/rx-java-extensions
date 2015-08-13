package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.model.dao.PostsDao;

import javax.annotation.Nonnull;

import retrofit.client.Response;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;

public class PostPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PostsDao postsDao;

    private final Observable<ResponseOrError<Response>> postSucces;
    public PostPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull PostsDao postsDao) {

        this.uiScheduler = uiScheduler;
        this.postsDao = postsDao;


        postSucces= postsDao.getPostSuccesObserver()
                .compose(ObservableExtensions.<ResponseOrError<Response>>behaviorRefCount());
    }


    public Observer<String> bodyObservable() {
        return this.postsDao.addBodyObserver();
    }

    public Observer<String> nameObservable() {
        return this.postsDao.addNameObserver();
    }


    public Observer<Object> sendObservable() {
        return this.postsDao.sendObserver();
    }
    public Observable<Response> postSuccesObservable(){
        return this.postSucces.compose(ResponseOrError.<Response>onlySuccess());
    }
}
