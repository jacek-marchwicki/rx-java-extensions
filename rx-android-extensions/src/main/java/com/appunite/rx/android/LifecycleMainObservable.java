package com.appunite.rx.android;

import android.app.Activity;
import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.android.schedulers.AndroidSchedulers;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class LifecycleMainObservable {

    @Nonnull
    private final LifecycleProvider lifecycleProvider;

    public static interface LifecycleProvider {
        @Nonnull
        Observable<LifecycleEvent> lifecycle();

        @Nonnull <T> Observable<T> bindLifecycle(@Nonnull Observable<T> observable);
    }

    public static class LifecycleProviderFragment implements LifecycleProvider {

        @Nonnull
        private final Observable<LifecycleEvent> lifecycle;
        @Nonnull
        private final Fragment fragment;

        public LifecycleProviderFragment(final @Nonnull Observable<LifecycleEvent> lifecycle,
                                         final @Nonnull Fragment fragment) {
            this.lifecycle = checkNotNull(lifecycle);
            this.fragment = checkNotNull(fragment);
        }

        @Nonnull
        @Override
        public Observable<LifecycleEvent> lifecycle() {
            return lifecycle;
        }

        @Nonnull
        @Override
        public <T> Observable<T> bindLifecycle(@Nonnull Observable<T> observable) {
            return AppObservable.bindFragment(fragment, observable);
        }
    }

    public static class LifecycleProviderActivity implements LifecycleProvider {

        @Nonnull
        private final Observable<LifecycleEvent> lifecycle;
        @Nonnull
        private final Activity activity;

        public LifecycleProviderActivity(final @Nonnull Observable<LifecycleEvent> lifecycle,
                                         final @Nonnull Activity activity) {
            this.lifecycle = checkNotNull(lifecycle);
            this.activity = checkNotNull(activity);
        }

        @Nonnull
        @Override
        public Observable<LifecycleEvent> lifecycle() {
            return lifecycle;
        }

        @Nonnull
        @Override
        public <T> Observable<T> bindLifecycle(@Nonnull Observable<T> observable) {
            return AppObservable.bindActivity(activity, observable);
        }
    }

    @Inject
    public LifecycleMainObservable(@Nonnull LifecycleProvider lifecycleProvider) {
        this.lifecycleProvider = checkNotNull(lifecycleProvider);
    }

    @Nonnull
    @Deprecated
    public <T> Observable<T> bindLifecycle(@Nonnull Observable<T> source) {
        return source.compose(this.<T>bindLifecycle());
    }

    @Nonnull
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> source) {
                checkNotNull(source);
                final Observable<LifecycleEvent> lifecycle = lifecycleProvider.lifecycle();
                final Observable<T> autoUnsubscribeObservable = LifecycleObservable
                        .bindFragmentLifecycle(lifecycle, source)
                        .observeOn(MyAndroidSchedulers.mainThread());
                return lifecycleProvider.bindLifecycle(autoUnsubscribeObservable);
            }
        };
    }
}
