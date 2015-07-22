/*
 * Copyright (C) 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.appunite.rx.operators;

import javax.annotation.Nonnull;

import rx.Observable.Operator;
import rx.Observer;
import rx.Subscriber;

public class OperatorCallOnNext<T> implements Operator<T, T> {
    private final Observer<? super T> doOnNextObserver;

    public OperatorCallOnNext(@Nonnull Observer<? super T> doOnNextObserver) {
        this.doOnNextObserver = doOnNextObserver;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> observer) {
        return new Subscriber<T>(observer) {

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onNext(T value) {
                observer.onNext(value);
                doOnNextObserver.onNext(value);
            }
        };
    }
}
