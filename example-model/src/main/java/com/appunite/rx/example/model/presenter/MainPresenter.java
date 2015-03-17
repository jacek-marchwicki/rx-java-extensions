package com.appunite.rx.example.model.presenter;

import com.appunite.rx.operators.FloatEvaluator;
import com.appunite.rx.operators.MoreOperators;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainPresenter {

    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Observable<String> compose;
    @Nonnull
    private final Scheduler uiScheduler;

    public MainPresenter(@Nonnull Scheduler networkScheduler,
                         @Nonnull Scheduler uiScheduler) {
        this.networkScheduler = networkScheduler;
        this.uiScheduler = uiScheduler;
        compose = Observable.just("Some title")
                .delay(2, TimeUnit.SECONDS)
                .compose(MoreOperators.<String>cacheWithTimeout(networkScheduler));
    }

    public Observable<String> titleObservable() {
        return compose
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler);
    }

    public Observable<Number> titleAlpha() {
        return compose
                .map(new Func1<String, Number>() {
                    @Override
                    public Number call(String s) {
                        return 1.f;
                    }
                })
                .startWith(0f)
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(MoreOperators.animatorCompose(networkScheduler, 1, TimeUnit.SECONDS, new FloatEvaluator()));
    }
}
