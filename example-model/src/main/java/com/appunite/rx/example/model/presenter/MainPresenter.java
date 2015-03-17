package com.appunite.rx.example.model.presenter;

import com.appunite.rx.operators.FloatEvaluator;
import com.appunite.rx.operators.MoreOperators;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class MainPresenter {

    @Nonnull
    private final Scheduler networkScheduler;
    private final Observable<String> compose;

    public MainPresenter(@Nonnull Scheduler networkScheduler) {
        this.networkScheduler = networkScheduler;
        compose = Observable.just("Some title")
                .delay(2, TimeUnit.SECONDS)
                .compose(MoreOperators.<String>cacheWithTimeout(networkScheduler))
                .subscribeOn(networkScheduler);
    }

    public Observable<String> titleObservable() {
        return compose;
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
                .compose(MoreOperators.animatorCompose(networkScheduler, new FloatEvaluator()))
                .subscribeOn(networkScheduler);
    }
}
