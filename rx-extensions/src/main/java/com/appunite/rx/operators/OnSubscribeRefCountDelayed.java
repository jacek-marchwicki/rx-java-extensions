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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Returns an observable sequence that stays connected to the source as long as
 * there is at least one subscription to the observable sequence + delay.
 * 
 * @param <T>
 *            the value type
 */
public final class OnSubscribeRefCountDelayed<T> implements OnSubscribe<T> {

    private final ConnectableObservable<? extends T> source;
    private final long delay;
    private final TimeUnit unit;
    private final Scheduler scheduler;
    private volatile CompositeSubscription baseSubscription = new CompositeSubscription();
    private final AtomicInteger subscriptionCount = new AtomicInteger(0);

    /**
     * Use this lock for every subscription and disconnect action.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructor.
     *
     * @param source
     *            observable to apply ref count to
     * @param delay
     *            the delay before unsubscribing
     * @param unit
     *            the delay unit of {@code delay}
     * @param scheduler
     *            the {@link Scheduler} to use for delaying
     */
    private OnSubscribeRefCountDelayed(ConnectableObservable<? extends T> source, long delay, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    /**
     * Returns an {@code Observable} that stays connected to this {@code ConnectableObservable} as long as there
     * is at least one subscription to this {@code ConnectableObservable} + delay.
     *
     * @param source
     *            observable to apply ref count to
     * @param delay
     *            the delay before unsubscribing
     * @param unit
     *            the delay unit of {@code delay}
     * @param scheduler
     *            the {@link Scheduler} to use for delaying
     *
     * @return a {@link Observable}
     */
    public static <T> Observable<T> create(ConnectableObservable<? extends T> source, long delay, TimeUnit unit, Scheduler scheduler) {
        return Observable.create(new OnSubscribeRefCountDelayed<>(source, delay, unit, scheduler));
    }
    /**
     * Returns an {@code Observable} that stays connected to this {@code ConnectableObservable} as long as there
     * is at least one subscription to this {@code ConnectableObservable} + delay.
     *
     * @param source
     *            observable to apply ref count to
     * @param delay
     *            the delay before unsubscribing
     * @param unit
     *            the delay unit of {@code delay}
     *
     * @return a {@link Observable}
     */
    public static <T> Observable<T> create(ConnectableObservable<? extends T> source, long delay, TimeUnit unit) {
        return Observable.create(new OnSubscribeRefCountDelayed<>(source, delay, unit, Schedulers.computation()));
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {

        lock.lock();
        if (subscriptionCount.incrementAndGet() == 1) {

            final AtomicBoolean writeLocked = new AtomicBoolean(true);

            try {
                // need to use this overload of connect to ensure that
                // baseSubscription is set in the case that source is a
                // synchronous Observable
                source.connect(onSubscribe(subscriber, writeLocked));
            } finally {
                // need to cover the case where the source is subscribed to
                // outside of this class thus preventing the above Action1
                // being called
                if (writeLocked.get()) {
                    // Action1 was not called
                    lock.unlock();
                }
            }
        } else {
            try {
                // handle unsubscribing from the base subscription
                subscriber.add(disconnect());

                // ready to subscribe to source so do it
                source.unsafeSubscribe(subscriber);
            } finally {
                // release the read lock
                lock.unlock();
            }
        }

    }

    private Action1<Subscription> onSubscribe(final Subscriber<? super T> subscriber,
            final AtomicBoolean writeLocked) {
        return new Action1<Subscription>() {
            @Override
            public void call(Subscription subscription) {

                try {
                    baseSubscription.add(subscription);

                    // handle unsubscribing from the base subscription
                    subscriber.add(disconnect());

                    // ready to subscribe to source so do it
                    source.unsafeSubscribe(subscriber);
                } finally {
                    // release the write lock
                    lock.unlock();
                    writeLocked.set(false);
                }
            }
        };
    }

    private Subscription disconnect() {
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                disconnectDelayed();
            }
        });
    }

    private void disconnectDelayed() {
        final Scheduler.Worker worker = scheduler.createWorker();
        baseSubscription.add(worker);

        if (subscriptionCount.decrementAndGet() == 0) {
            worker.schedule(new Action0() {
                @Override
                public void call() {
                    disconnectNow();
                }
            }, delay, unit);
        }
    }

    private void disconnectNow() {
        lock.lock();
        try {
            if (subscriptionCount.get() == 0) {
                baseSubscription.unsubscribe();
                // need a new baseSubscription because once
                // unsubscribed stays that way
                baseSubscription = new CompositeSubscription();
            }
        } finally {
            lock.unlock();
        }
    }
}