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


import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;

/**
 * Sample with the help of another observable always getting latest value from observable.
 * 
 * @param <T> the source and result value type
 * @param <U> the element type of the sampler Observable
 */
public final class OperatorSampleWithLastWithObservable<T, U> implements Operator<T, T> {
    final Observable<U> sampler;
    /** Indicates that no value is available. */
    static final Object EMPTY_TOKEN = new Object();

    public static <T> OperatorSampleWithLastWithObservable<T, Object> create(Observable<Object> sample) {
        return new OperatorSampleWithLastWithObservable<>(sample);
    }

    public OperatorSampleWithLastWithObservable(Observable<U> sampler) {
        this.sampler = sampler;
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        final SerializedSubscriber<T> s = new SerializedSubscriber<>(child);
    
        final AtomicReference<Object> value = new AtomicReference<>(EMPTY_TOKEN);

        final Subscriber<U> samplerSub = new Subscriber<U>(child) {
            @Override
            public void onNext(U t) {
                final Object localValue = value.get();
                if (localValue != EMPTY_TOKEN) {
                    @SuppressWarnings("unchecked")
                    T v = (T)localValue;
                    s.onNext(v);
                }
            }

            @Override
            public void onError(Throwable e) {
                s.onError(e);
                unsubscribe();
            }

            @Override
            public void onCompleted() {
                s.onCompleted();
                unsubscribe();
            }
            
        };
        
        final Subscriber<T> result = new Subscriber<T>(child) {
            @Override
            public void onNext(T t) {
                value.set(t);
            }

            @Override
            public void onError(Throwable e) {
                s.onError(e);
                unsubscribe();
            }

            @Override
            public void onCompleted() {
                s.onCompleted();
                unsubscribe();
            }
        };

        sampler.unsafeSubscribe(samplerSub);
        
        return result;
    }
}
