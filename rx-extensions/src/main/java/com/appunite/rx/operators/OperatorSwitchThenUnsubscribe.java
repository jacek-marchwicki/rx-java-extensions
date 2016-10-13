/*
 * Copyright 2014 Netflix, Inc.
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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import rx.*;
import rx.Observable;
import rx.Observable.Operator;
import rx.exceptions.CompositeException;
import rx.functions.Action0;
import rx.internal.operators.NotificationLite;
import rx.internal.util.RxRingBuffer;
import rx.internal.util.atomic.SpscLinkedArrayQueue;
import rx.plugins.RxJavaHooks;
import rx.subscriptions.*;

/**
 * Operator differ from {@link rx.internal.operators.OperatorSwitch} only that it first subscribe to for new values,
 * then unsubscribe from previous. Code changes are marked as "Was: xyz, Now: xyz"
 *
 * Transforms an Observable that emits Observables into a single Observable that
 * emits the items emitted by the most recently published of those Observables.
 * <p>
 * <img width="640" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/switchDo.png" alt="">
 *
 * @param <T> the value type
 */
public final class OperatorSwitchThenUnsubscribe<T> implements Operator<T, Observable<? extends T>> {
    final boolean delayError;
    /** Lazy initialization via inner-class holder. */
    static final class Holder {
        /** A singleton instance. */
        static final OperatorSwitchThenUnsubscribe<Object> INSTANCE = new OperatorSwitchThenUnsubscribe<Object>(false);
    }
    /** Lazy initialization via inner-class holder. */
    static final class HolderDelayError {
        /** A singleton instance. */
        static final OperatorSwitchThenUnsubscribe<Object> INSTANCE = new OperatorSwitchThenUnsubscribe<Object>(true);
    }
    /**
     * Returns a singleton instance of the operator for non delayed.
     * @param <T> the value type
     * @return a singleton instance of this stateless operator.
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> OperatorSwitchThenUnsubscribe<T> instance() {
        return (OperatorSwitchThenUnsubscribe<T>)Holder.INSTANCE;
    }
    /**
     * Returns a singleton instance of the operator based on the delayError parameter.
     * @param <T> the value type
     * @param delayError should the errors of the inner sources delayed until the main sequence completes?
     * @return a singleton instance of this stateless operator.
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> OperatorSwitchThenUnsubscribe<T> instance(boolean delayError) {
        if (delayError) {
            return (OperatorSwitchThenUnsubscribe<T>)HolderDelayError.INSTANCE;
        }
        return (OperatorSwitchThenUnsubscribe<T>)Holder.INSTANCE;
    }

    OperatorSwitchThenUnsubscribe(boolean delayError) {
        this.delayError = delayError;
    }

    @Override
    public Subscriber<? super Observable<? extends T>> call(final Subscriber<? super T> child) {
        SwitchSubscriber<T> sws = new SwitchSubscriber<T>(child, delayError);
        child.add(sws);
        sws.init();
        return sws;
    }

    static final class SwitchSubscriber<T> extends Subscriber<Observable<? extends T>> {
        final Subscriber<? super T> child;
        final SerialSubscription serial;
        final boolean delayError;
        final AtomicLong index;
        final SpscLinkedArrayQueue<Object> queue;
        final NotificationLite<T> nl;

        boolean emitting;

        boolean missed;

        long requested;

        Producer producer;

        volatile boolean mainDone;

        Throwable error;

        boolean innerActive;

        static final Throwable TERMINAL_ERROR = new Throwable("Terminal error");

        SwitchSubscriber(Subscriber<? super T> child, boolean delayError) {
            this.child = child;
            this.serial = new SerialSubscription();
            this.delayError = delayError;
            this.index = new AtomicLong();
            this.queue = new SpscLinkedArrayQueue<Object>(RxRingBuffer.SIZE);
            this.nl = NotificationLite.instance();
        }

        void init() {
            child.add(serial);
            child.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    clearProducer();
                }
            }));
            child.setProducer(new Producer(){

                @Override
                public void request(long n) {
                    if (n > 0L) {
                        childRequested(n);
                    } else
                    if (n < 0L) {
                        throw new IllegalArgumentException("n >= 0 expected but it was " + n);
                    }
                }
            });
        }

        void clearProducer() {
            synchronized (this) {
                producer = null;
            }
        }

        @Override
        public void onNext(Observable<? extends T> t) {
            long id = index.incrementAndGet();

            // Was:
            //  Subscription s = serial.get();
            //  if (s != null) {
            //    s.unsubscribe();
            //  }

            InnerSubscriber<T> inner;

            synchronized (this) {
                inner = new InnerSubscriber<T>(id, this);

                innerActive = true;
                producer = null;
            }
            // Was:
            //  t.unsafeSubscribe(inner);
            //  serial.set(inner);
            // Now:
            t.unsafeSubscribe(inner);

            serial.set(inner);


        }

        @Override
        public void onError(Throwable e) {
            boolean success;

            synchronized (this) {
                success = updateError(e);
            }
            if (success) {
                mainDone = true;
                drain();
            } else {
                pluginError(e);
            }
        }

        boolean updateError(Throwable next) {
            Throwable e = error;
            if (e == TERMINAL_ERROR) {
                return false;
            } else
            if (e == null) {
                error = next;
            } else
            if (e instanceof CompositeException) {
                List<Throwable> list = new ArrayList<Throwable>(((CompositeException)e).getExceptions());
                list.add(next);
                error = new CompositeException(list);
            } else {
                error = new CompositeException(e, next);
            }
            return true;
        }

        @Override
        public void onCompleted() {
            mainDone = true;
            drain();
        }

        void emit(T value, InnerSubscriber<T> inner) {
            synchronized (this) {
                if (index.get() != inner.id) {
                    return;
                }

                queue.offer(inner, nl.next(value));
            }
            drain();
        }

        void error(Throwable e, long id) {
            boolean success;
            synchronized (this) {
                if (index.get() == id) {
                    success = updateError(e);
                    innerActive = false;
                    producer = null;
                } else {
                    success = true;
                }
            }
            if (success) {
                drain();
            } else {
                pluginError(e);
            }
        }

        void complete(long id) {
            synchronized (this) {
                if (index.get() != id) {
                    return;
                }
                innerActive = false;
                producer = null;
            }
            drain();
        }

        void pluginError(Throwable e) {
            RxJavaHooks.onError(e);
        }

        void innerProducer(Producer p, long id) {
            long n;
            synchronized (this) {
                if (index.get() != id) {
                    return;
                }
                n = requested;
                producer = p;
            }

            p.request(n);
        }

        void childRequested(long n) {
            Producer p;
            synchronized (this) {
                p = producer;
                requested = addCap(requested, n);
            }
            if (p != null) {
                p.request(n);
            }
            drain();
        }

        void drain() {
            boolean localInnerActive;
            long localRequested;
            Throwable localError;
            synchronized (this) {
                if (emitting) {
                    missed = true;
                    return;
                }
                emitting = true;
                localInnerActive = innerActive;
                localRequested = requested;
                localError = error;
                if (localError != null && localError != TERMINAL_ERROR && !delayError) {
                    error = TERMINAL_ERROR;
                }
            }

            final SpscLinkedArrayQueue<Object> localQueue = queue;
            final AtomicLong localIndex = index;
            final Subscriber<? super T> localChild = child;
            boolean localMainDone = mainDone;

            for (;;) {

                long localEmission = 0L;

                while (localEmission != localRequested) {
                    if (localChild.isUnsubscribed()) {
                        return;
                    }

                    boolean empty = localQueue.isEmpty();

                    if (checkTerminated(localMainDone, localInnerActive, localError,
                            localQueue, localChild, empty)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    @SuppressWarnings("unchecked")
                    InnerSubscriber<T> inner = (InnerSubscriber<T>)localQueue.poll();
                    T value = nl.getValue(localQueue.poll());

                    if (localIndex.get() == inner.id) {
                        localChild.onNext(value);
                        localEmission++;
                    }
                }

                if (localEmission == localRequested) {
                    if (localChild.isUnsubscribed()) {
                        return;
                    }

                    if (checkTerminated(mainDone, localInnerActive, localError, localQueue,
                            localChild, localQueue.isEmpty())) {
                        return;
                    }
                }


                synchronized (this) {

                    localRequested = requested;
                    if (localRequested != Long.MAX_VALUE) {
                        localRequested -= localEmission;
                        requested = localRequested;
                    }

                    if (!missed) {
                        emitting = false;
                        return;
                    }
                    missed = false;

                    localMainDone = mainDone;
                    localInnerActive = innerActive;
                    localError = error;
                    if (localError != null && localError != TERMINAL_ERROR && !delayError) {
                        error = TERMINAL_ERROR;
                    }
                }
            }
        }

        protected boolean checkTerminated(boolean localMainDone, boolean localInnerActive, Throwable localError,
                                          final SpscLinkedArrayQueue<Object> localQueue, final Subscriber<? super T> localChild, boolean empty) {
            if (delayError) {
                if (localMainDone && !localInnerActive && empty) {
                    if (localError != null) {
                        localChild.onError(localError);
                    } else {
                        localChild.onCompleted();
                    }
                    return true;
                }
            } else {
                if (localError != null) {
                    localQueue.clear();
                    localChild.onError(localError);
                    return true;
                } else
                if (localMainDone && !localInnerActive && empty) {
                    localChild.onCompleted();
                    return true;
                }
            }
            return false;
        }
    }

    static final class InnerSubscriber<T> extends Subscriber<T> {

        private final long id;

        private final SwitchSubscriber<T> parent;

        InnerSubscriber(long id, SwitchSubscriber<T> parent) {
            this.id = id;
            this.parent = parent;
        }

        @Override
        public void setProducer(Producer p) {
            parent.innerProducer(p, id);
        }

        @Override
        public void onNext(T t) {
            parent.emit(t, this);
        }

        @Override
        public void onError(Throwable e) {
            parent.error(e, id);
        }

        @Override
        public void onCompleted() {
            parent.complete(id);
        }
    }

    /**
     * From BackpressureUtils
     * Adds two positive longs and caps the result at Long.MAX_VALUE.
     * @param a the first value
     * @param b the second value
     * @return the capped sum of a and b
     */
    private static long addCap(long a, long b) {
        long u = a + b;
        if (u < 0L) {
            u = Long.MAX_VALUE;
        }
        return u;
    }

}