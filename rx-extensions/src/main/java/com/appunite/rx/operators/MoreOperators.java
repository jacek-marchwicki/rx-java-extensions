/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appunite.rx.operators;

import com.appunite.rx.ResponseOrError;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;
import rx.subscriptions.Subscriptions;

import static com.appunite.rx.ObservableExtensions.behavior;
import static com.appunite.rx.operators.OnSubscribeRedoWithNext.repeatOn;
import static com.google.common.base.Preconditions.checkNotNull;

public class MoreOperators {

    @Nonnull
    public static <T> Observable.Transformer<T, T> refresh(
            @Nonnull final Observable<Object> refreshSubject) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {
                return refresh(refreshSubject, observable);
            }
        };
    }

    @Nonnull
    private static <T> Observable<T> refresh(@Nonnull Observable<Object> refreshSubject,
                                             @Nonnull final Observable<T> toRefresh) {
        return refreshSubject.startWith((Object)null).flatMap(new Func1<Object, Observable<T>>() {
            @Override
            public Observable<T> call(final Object o) {
                return toRefresh;
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> cacheWithTimeout(
            @Nonnull final Scheduler scheduler) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {
                return cacheWithTimeout(observable, scheduler);
            }
        };
    }

    @Nonnull
    private static <T> Observable<T> cacheWithTimeout(
            @Nonnull Observable<T> observable,
            @Nonnull Scheduler scheduler) {
        return OnSubscribeRefCountDelayed.create(
                behavior(observable), 5, TimeUnit.SECONDS, scheduler);
    }

    @Nonnull
    public static <T> Observable.Transformer<ResponseOrError<T>, ResponseOrError<T>> repeatOnError(
            @Nonnull final Scheduler scheduler) {
        return new Observable.Transformer<ResponseOrError<T>, ResponseOrError<T>>() {
            @Override
            public Observable<ResponseOrError<T>> call(final Observable<ResponseOrError<T>> responseOrErrorObservable) {
                return repeatOnError(responseOrErrorObservable, scheduler);
            }
        };
    }

    @Nonnull
    private static <T> Observable<ResponseOrError<T>> repeatOnError(
            @Nonnull final Observable<ResponseOrError<T>> from,
            @Nonnull Scheduler scheduler) {
        return repeatOn(from, new RepeatOnError<T>(scheduler));
    }

    private static class RepeatOnError<T> implements Func1<Notification<ResponseOrError<T>>, Observable<?>> {
        @Nonnull
        private final Scheduler scheduler;

        public RepeatOnError(@Nonnull final Scheduler scheduler) {
            this.scheduler = checkNotNull(scheduler);
        }

        @Override
        public Observable<?> call(final Notification<ResponseOrError<T>> notification) {
            if (notification.isOnNext()) {
                return notification.getValue().isError()
                        ? Observable.timer(10, TimeUnit.SECONDS, scheduler)
                        : Observable.never();
            } else {
                return Observable.never();
            }
        }
    }

    public static <T> Observable<ImmutableList<T>> combineAll(ImmutableList<Observable<T>> observables) {
        if (observables.isEmpty()) {
            return Observable.just(ImmutableList.<T>of());
        }
        if (observables.size() > 10) {
            // RxJava has some limitation that can only handle up to 128 arguments in combineLast
            // Additionally on android there is a bug so this limit is cut to 16 arguments - on
            // android we will not get any throw so rxjava fail sailent
            final int size = observables.size();
            final int left = size / 2;
            return Observable.combineLatest(
                    combineAll(observables.subList(0, left)),
                    combineAll(observables.subList(left, size)),
                    new Func2<ImmutableList<T>, ImmutableList<T>, ImmutableList<T>>() {
                        @Override
                        public ImmutableList<T> call(final ImmutableList<T> ts, final ImmutableList<T> ts2) {
                            return ImmutableList.<T>builder().addAll(ts).addAll(ts2).build();
                        }
                    });
        }
        return Observable.combineLatest(observables, new FuncN<ImmutableList<T>>() {
            @Override
            public ImmutableList<T> call(final Object... args) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                for (Object arg : args) {
                    //noinspection unchecked
                    builder.add((T) arg);
                }
                return builder.build();
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> animatorCompose(
            @Nonnull final Scheduler scheduler,
            @Nonnull final TypeEvaluator<T> evaluator) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> integerObservable) {
                return Observable.create(new Observable.OnSubscribe<T>() {
                    T prevValue;
                    @Override
                    public void call(final Subscriber<? super T> child) {
                        final Scheduler.Worker worker = scheduler.createWorker();
                        child.add(worker);
                        final Subscription sub = integerObservable.subscribe(new Observer<T>() {

                            private Subscription subscription;

                            @Override
                            public void onCompleted() {
                                child.onCompleted();
                            }

                            @Override
                            public void onError(final Throwable e) {
                                child.onError(e);
                            }

                            @Override
                            public void onNext(final T endValue) {
                                if (subscription != null) {
                                    subscription.unsubscribe();
                                    subscription = null;
                                }
                                if (prevValue == null) {
                                    prevValue = endValue;
                                    child.onNext(endValue);
                                } else {
                                    subscription = worker.schedulePeriodically(new Action0() {
                                        int i = 0;
                                        final T startValue = prevValue;

                                        @Override
                                        public void call() {
                                            i++;
                                            prevValue = evaluator.evaluate((float) i / 10f, startValue, endValue);
                                            child.onNext(prevValue);
                                            if (i >= 10) {
                                                if (subscription != null) {
                                                    subscription.unsubscribe();
                                                    subscription = null;
                                                }
                                            }
                                        }
                                    }, 0, 16, TimeUnit.MILLISECONDS);
                                }
                            }
                        });

                        child.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                sub.unsubscribe();
                            }
                        }));
                    }
                });
            }
        };
    }
}
