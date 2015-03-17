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

import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;
import rx.functions.Func2;

public class OperatorMergeNextToken<T, K> implements Observable.Operator<T, K> {

    private final T initialValue;
    private final Func2<T, K, Observable<T>> merge;

    private OperatorMergeNextToken(final T initialValue, final Func2<T, K, Observable<T>> merge) {
        this.initialValue = initialValue;
        this.merge = merge;
    }

    public static <T, K> OperatorMergeNextToken<T, K> create(final T initialValue, final Func2<T, K, Observable<T>> merge) {
        return new OperatorMergeNextToken<>(initialValue, merge);
    }

    public static <T, K> OperatorMergeNextToken<T, K> create(final Func2<T, K, Observable<T>> merge) {
        return new OperatorMergeNextToken<>(null, merge);
    }

    public static <T> OperatorMergeNextToken<T, Object> create(final Func1<T, Observable<T>> merge) {
        return new OperatorMergeNextToken<>(null, new IgnoreSourceParameter<>(merge));
    }

    public static <T> OperatorMergeNextToken<T, Object> create(T initialValue, final Func1<T, Observable<T>> merge) {
        return new OperatorMergeNextToken<>(initialValue, new IgnoreSourceParameter<>(merge));
    }

    @Override
    public Subscriber<? super K> call(final Subscriber<? super T> child) {
        return new Subscriber<K>() {
            private final ReentrantLock lock = new ReentrantLock();
            private T previous = initialValue;

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(final Throwable e) {
                child.onError(e);
            }

            @Override
            public void onNext(final K k) {
                final Observer<T> observer = new Observer<T>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(final Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onNext(final T t) {
                        lock.lock();
                        try {
                            previous = t;
                            child.onNext(t);
                        } finally {
                            lock.unlock();
                        }
                    }
                };

                try {
                    final Observable<T> nextObservable = merge.call(previous, k);
                    child.add(nextObservable.subscribe(observer));
                } catch (Throwable e) {
                    child.onError(OnErrorThrowable.addValueAsLastCause(e, k));
                }
            }
        };
    }

    private static class IgnoreSourceParameter<T> implements Func2<T, Object, Observable<T>> {
        private final Func1<T, Observable<T>> merge;

        public IgnoreSourceParameter(final Func1<T, Observable<T>> merge) {
            this.merge = merge;
        }

        @Override
        public Observable<T> call(final T t, final Object object) {
            return merge.call(t);
        }
    }
}
