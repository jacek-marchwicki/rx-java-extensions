package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class DetailsPresenters {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Observable<ResponseOrError<String>> nameObservable;
    @Nonnull
    private final Observable<ResponseOrError<String>> bodyObservable;

    public DetailsPresenters(@Nonnull @UiScheduler Scheduler uiScheduler,
                             @Nonnull PostsDao postsDao,
                             /* for dagger: @Named("post_id") */@Nonnull final String postId) {

        this.uiScheduler = uiScheduler;
        final PostsDao.PostDao postDao = postsDao.postDao(postId);

        final Observable<ResponseOrError<PostWithBody>> postWithBodyObservable = postDao.postWithBodyObservable()
                .observeOn(uiScheduler)
                .replay(1)
                .refCount();
        nameObservable = postWithBodyObservable
                .compose(ResponseOrError.map(new Func1<PostWithBody, String>() {
                    @Override
                    public String call(PostWithBody item) {
                        return Strings.nullToEmpty(item.name());
                    }
                }))
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        bodyObservable = postWithBodyObservable
                .compose(ResponseOrError.map(new Func1<PostWithBody, String>() {
                    @Override
                    public String call(PostWithBody item) {
                        return Strings.nullToEmpty(item.body());
                    }
                }))
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());
    }

    @Nonnull
    public Observable<String> bodyObservable() {
        return bodyObservable
                .compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    public Observable<String> titleObservable() {
        return nameObservable
                .compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    public Observable<Boolean> progressObservable() {
        return ResponseOrError.combineProgressObservable(ImmutableList.of(
                ResponseOrError.transform(nameObservable),
                ResponseOrError.transform(bodyObservable)));
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(nameObservable),
                ResponseOrError.transform(bodyObservable)));
    }

    @Nonnull
    public Observable<Object> startPostponedEnterTransitionObservable() {
        final Observable<Boolean> filter = progressObservable().filter(Functions1.isFalse());
        final Observable<Throwable> error = errorObservable().filter(Functions1.isNotNull());
        final Observable<String> timeout = Observable.just("").delay(500, TimeUnit.MILLISECONDS, uiScheduler);
        return Observable.<Object>amb(filter, error, timeout).first();
    }

}
