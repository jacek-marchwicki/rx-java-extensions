/*
 * Copyright 2014 Netflix, Inc.
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

import static rx.Observable.create;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import rx.Notification;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observable.Operator;
import rx.Producer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.SerialSubscription;

public final class OnSubscribeRedoWithNext<T> implements OnSubscribe<T> {

    public static <T> Observable<T> repeatOn(Observable<T> source,
                                             final Func1<Notification<T>, Observable<?>> on) {
        final Func1<Observable<? extends Notification<T>>, Observable<?>> func1
                = new Func1<Observable<? extends Notification<T>>, Observable<?>>() {
            @Override
            public Observable<?> call(final Observable<? extends Notification<T>> observable) {
                return observable.flatMap(on);
            }
        };
        return create(new OnSubscribeRedoWithNext<>(source, func1, Schedulers.trampoline()));
    }

    public static <T> Observable<T> repeat(
            Observable<T> source,
            Func1<? super Observable<? extends Notification<T>>,
                    ? extends Observable<?>> notificationHandler) {
        return create(new OnSubscribeRedoWithNext<>(
                source,
                notificationHandler,
                Schedulers.trampoline()));
    }


    private Observable<T> source;
    private final Func1<? super Observable<? extends Notification<T>>, ? extends Observable<?>>
            controlHandlerFunction;
    private final Scheduler scheduler;

    private OnSubscribeRedoWithNext(Observable<T> source,
                                    Func1<? super Observable<? extends Notification<T>>,
                                            ? extends Observable<?>> f,
                                    Scheduler scheduler) {
        this.source = source;
        this.controlHandlerFunction = f;
        this.scheduler = scheduler;
    }

    @Override
    public void call(final Subscriber<? super T> child) {
        final AtomicBoolean isLocked = new AtomicBoolean(true);
        final AtomicBoolean resumeBoundary = new AtomicBoolean(true);
        // incremented when requests are made, decremented when requests are fulfilled
        final AtomicLong consumerCapacity = new AtomicLong(0L);
        final AtomicReference<Producer> currentProducer = new AtomicReference<>();

        final Scheduler.Worker worker = scheduler.createWorker();
        child.add(worker);

        final SerialSubscription sourceSubscriptions = new SerialSubscription();
        child.add(sourceSubscriptions);

        final PublishSubject<Notification<T>> terminals = PublishSubject.create();

        final Action0 subscribeToSource = new Action0() {
            @Override
            public void call() {
                if (child.isUnsubscribed()) {
                    return;
                }

                Subscriber<T> terminalDelegatingSubscriber = new Subscriber<T>() {
                    @Override
                    public void onCompleted() {
                        unsubscribe();
                        terminals.onNext(Notification.<T>createOnCompleted());
                    }

                    @Override
                    public void onError(Throwable e) {
                        unsubscribe();
                        terminals.onNext(Notification.<T>createOnError(e));
                    }

                    @Override
                    public void onNext(T v) {
                        if (consumerCapacity.get() != Long.MAX_VALUE) {
                            consumerCapacity.decrementAndGet();
                        }
                        child.onNext(v);
                        terminals.onNext(Notification.createOnNext(v));
                    }

                    @Override
                    public void setProducer(Producer producer) {
                        currentProducer.set(producer);
                        long c = consumerCapacity.get();
                        if (c > 0) {
                            producer.request(c);
                        }
                    }
                };
                // new subscription each time so if it unsubscribes itself it does not prevent
                // retries by unsubscribing the child subscription
                sourceSubscriptions.set(terminalDelegatingSubscriber);
                source.unsafeSubscribe(terminalDelegatingSubscriber);
            }
        };

        // the observable received by the control handler function will receive notifications of
        // onCompleted in the case of 'repeat' type operators or notifications of onError for
        // 'retry' this is done by lifting in a custom operator to selectively divert the
        // retry/repeat relevant values to the control handler
        final Observable<?> restarts = controlHandlerFunction.call(
                terminals.lift(new Operator<Notification<T>, Notification<T>>() {
                    @Override
                    public Subscriber<? super Notification<T>> call(
                            final Subscriber<? super Notification<T>> filteredTerminals) {
                        return new Subscriber<Notification<T>>(filteredTerminals) {
                            @Override
                            public void onCompleted() {
                                filteredTerminals.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                filteredTerminals.onError(e);
                            }

                            @Override
                            public void onNext(Notification<T> t) {
                                isLocked.set(false);
                                filteredTerminals.onNext(t);
                            }

                            @Override
                            public void setProducer(Producer producer) {
                                producer.request(Long.MAX_VALUE);
                            }
                        };
                    }
                }));

        // subscribe to the restarts observable to know when to schedule the next redo.
        worker.schedule(new Action0() {
            @Override
            public void call() {
                restarts.unsafeSubscribe(new Subscriber<Object>(child) {
                    @Override
                    public void onCompleted() {
                        child.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onNext(Object t) {
                        if (!isLocked.get() && !child.isUnsubscribed()) {
                            if (consumerCapacity.get() > 0) {
                                worker.schedule(subscribeToSource);
                            } else {
                                resumeBoundary.compareAndSet(false, true);
                            }
                        }
                    }

                    @Override
                    public void setProducer(Producer producer) {
                        producer.request(Long.MAX_VALUE);
                    }
                });
            }
        });

        child.setProducer(new Producer() {

            @Override
            public void request(final long n) {
                long c = consumerCapacity.getAndAdd(n);
                Producer producer = currentProducer.get();
                if (producer != null) {
                    producer.request(n);
                } else
                if (c == 0 && resumeBoundary.compareAndSet(true, false)) {
                    worker.schedule(subscribeToSource);
                }
            }
        });

    }
}
