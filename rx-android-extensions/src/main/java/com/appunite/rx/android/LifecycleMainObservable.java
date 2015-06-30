/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.android;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.appunite.rx.operators.NiceErrorOperator;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class LifecycleMainObservable {

    @Nonnull
    private final LifecycleProvider lifecycleProvider;

    public static interface LifecycleProvider {
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
        public <T> Observable<T> bindLifecycle(@Nonnull Observable<T> observable) {
            final Observable<T> autoUnsubscribeObservable = LifecycleObservable
                    .bindFragmentLifecycle(lifecycle, observable)
                    .observeOn(MyAndroidSchedulers.mainThread());
            return AppObservable.bindFragment(fragment, autoUnsubscribeObservable);
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
        public <T> Observable<T> bindLifecycle(@Nonnull Observable<T> observable) {
            final Observable<T> autoUnsubscribeObservable = LifecycleObservable
                    .bindActivityLifecycle(lifecycle, observable)
                    .observeOn(MyAndroidSchedulers.mainThread());
            return AppObservable.bindActivity(activity, autoUnsubscribeObservable);
        }
    }

    @Inject
    public LifecycleMainObservable(@Nonnull LifecycleProvider lifecycleProvider) {
        this.lifecycleProvider = checkNotNull(lifecycleProvider);
    }

    @Nonnull
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> source) {
                checkNotNull(source);
                return lifecycleProvider.bindLifecycle(source)
                        .lift(NiceErrorOperator.<T>niceErrorOperator(null, stackTraceElements));
            }
        };
    }
}
