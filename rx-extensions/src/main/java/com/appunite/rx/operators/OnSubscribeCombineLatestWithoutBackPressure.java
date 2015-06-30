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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.functions.Func5;
import rx.functions.Func6;
import rx.functions.Func7;
import rx.functions.Func8;
import rx.functions.Func9;
import rx.functions.FuncN;
import rx.functions.Functions;

/**
 * This implementation is very similar to {@link rx.internal.operators.OnSubscribeCombineLatest}
 * but it does not implement back-pressure behavior
 * <p>
 * <img width="640" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/combineLatest.png" alt="">
 * </p>
 *
 * @param <T>
 *            the common basetype of the source values
 * @param <R>
 *            the result type of the combinator function
 */
public class OnSubscribeCombineLatestWithoutBackPressure<T, R> implements Observable.OnSubscribe<R> {
    private final List<? extends Observable<? extends T>> sources;
    private final FuncN<? extends R> combinator;

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Func2<? super T1, ? super T2, ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2), Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Func3<? super T1, ? super T2, ? super T3, ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3), Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Func4<? super T1, ? super T2, ? super T3, ? super T4,
                    ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4), Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, T5, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Observable<? extends T5> o5,
            @Nonnull Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5,
                    ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4, o5), Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, T5, T6, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Observable<? extends T5> o5,
            @Nonnull Observable<? extends T6> o6,
            @Nonnull Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,
                    ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4, o5, o6),
                Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Observable<? extends T5> o5,
            @Nonnull Observable<? extends T6> o6,
            @Nonnull Observable<? extends T7> o7,
            @Nonnull Func7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,
                    ? super T7, ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4, o5, o6, o7),
                Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Observable<? extends T5> o5,
            @Nonnull Observable<? extends T6> o6,
            @Nonnull Observable<? extends T7> o7,
            @Nonnull Observable<? extends T8> o8,
            @Nonnull Func8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,
                    ? super T7, ? super T8, ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8),
                Functions.fromFunc(combineFunction));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> combineLatest(
            @Nonnull Observable<? extends T1> o1,
            @Nonnull Observable<? extends T2> o2,
            @Nonnull Observable<? extends T3> o3,
            @Nonnull Observable<? extends T4> o4,
            @Nonnull Observable<? extends T5> o5,
            @Nonnull Observable<? extends T6> o6,
            @Nonnull Observable<? extends T7> o7,
            @Nonnull Observable<? extends T8> o8,
            @Nonnull Observable<? extends T9> o9,
            @Nonnull Func9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,
                    ? super T7, ? super T8, ? super T9, ? extends R> combineFunction) {
        return combineLatest(Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8, o9),
                Functions.fromFunc(combineFunction));
    }

    @Nonnull
    public static <T, R> Observable<R> combineLatest(
            @Nonnull List<? extends Observable<? extends T>> sources,
            @Nonnull FuncN<? extends R> combineFunction) {
        return Observable.create(new OnSubscribeCombineLatestWithoutBackPressure<>(sources,
                combineFunction));
    }

    public OnSubscribeCombineLatestWithoutBackPressure(
            @Nonnull List<? extends Observable<? extends T>> sources,
            @Nonnull FuncN<? extends R> combinator) {
        this.sources = sources;
        this.combinator = combinator;
    }

    private static final Object NO_OBJECT_RETURNED = new Object();

    @Override
    public void call(final Subscriber<? super R> subscriber) {
        final Object[] items = new Object[sources.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = NO_OBJECT_RETURNED;
        }

        final boolean[] completed = new boolean[items.length];
        for (int i = 0; i < completed.length; i++) {
            completed[i] = false;
        }

        final Object lock = new Object();
        for (int i = 0; i < sources.size(); i++) {
            final Observable<? extends T> source = sources.get(i);
            subscriber.add(source.subscribe(new MyObserver<T, R>(subscriber, lock, items, completed,
                    i, combinator)));
        }
    }

    private static class MyObserver<T, R> implements Observer<T> {
        @Nonnull
        private final Subscriber<? super R> subscriber;
        @Nonnull
        private final Object lock;
        @Nonnull
        private final Object[] items;
        @Nonnull
        private final boolean[] completed;
        @Nonnull
        private final FuncN<? extends R> combinator;

        private final int current;

        public MyObserver(@Nonnull Subscriber<? super R> subscriber,
                          @Nonnull Object lock,
                          @Nonnull Object[] items,
                          @Nonnull boolean[] completed,
                          int current,
                          @Nonnull FuncN<? extends R> combinator) {
            this.subscriber = subscriber;
            this.lock = lock;
            this.items = items;
            this.completed = completed;
            this.current = current;
            this.combinator = combinator;

        }

        @Override
        public void onCompleted() {
            synchronized (lock) {
                completed[current] = true;
                for (Boolean c : completed) {
                    if (!c) {
                        return;
                    }
                }
            }
            subscriber.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
        }

        @Override
        public void onNext(T t) {
            final R calculate;
            synchronized (lock) {
                items[current] = t;
                for (Object item : items) {
                    if (item == NO_OBJECT_RETURNED) {
                        return;
                    }
                }
                calculate = combinator.call(items);
            }
            subscriber.onNext(calculate);
        }
    }
}
