package com.appunite.rx.example.model.presenter;

import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.model.dao.PostsDao;

import javax.annotation.Nonnull;

import rx.Observer;
import rx.Scheduler;

public class PostPresenter {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PostsDao postsDao;

    public PostPresenter(@Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull PostsDao postsDao) {

        this.uiScheduler = uiScheduler;
        this.postsDao = postsDao;
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
}
