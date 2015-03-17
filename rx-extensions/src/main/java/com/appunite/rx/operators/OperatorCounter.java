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

package com.appunite.rx.operators;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;

public class OperatorCounter implements Observable.Operator<Integer, Object> {
    public OperatorCounter() {
    }

    public static OperatorCounter create() {
        return new OperatorCounter();
    }

    @Override
    public Subscriber<? super Object> call(final Subscriber<? super Integer> subscriber) {
        final AtomicInteger counter = new AtomicInteger(0);
        subscriber.onNext(counter.get());
        return new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(final Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(final Object o) {
                subscriber.onNext(counter.incrementAndGet());
            }
        };
    }
}
