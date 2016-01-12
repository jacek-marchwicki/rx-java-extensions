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

package com.appunite.rx;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Func0;
import rx.internal.operators.OperatorMulticast;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class ObservableExtensions {

    @Nonnull
    public static <T> ConnectableObservable<T> behavior(@Nonnull Observable<T> observable) {
        return new OperatorMulticast<>(observable, new Func0<Subject<? super T, ? extends T>>() {

            @Override
            public Subject<? super T, ? extends T> call() {
                return BehaviorSubject.<T>create();
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> behaviorRefCount() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> tObservable) {
                return behavior(tObservable).refCount();
            }
        };
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> behaviorConnected() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                final ConnectableObservable<T> behavior = behavior(tObservable);
                behavior.connect();
                return behavior;
            }
        };
    }
}
