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

package com.appunite.rx.subjects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

public class CacheSubject<T> extends Subject<T, T> {

    @Nonnull
    private final CacheCreator<T> cacheCreator;

    @Nonnull
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();

    @Nonnull
    public static <T> CacheSubject<T> create(@Nonnull CacheCreator<T> cacheCreator) {
        return new CacheSubject<>(cacheCreator, new DelegateOnSubscribe<T>());
    }


    private CacheSubject(@Nonnull final CacheCreator<T> cacheCreator, DelegateOnSubscribe<T> delegateOnSubscribe) {
        super(delegateOnSubscribe);
        delegateOnSubscribe.setDelegate(new OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> child) {
                final T t = cacheCreator.readFromCache();
                if (t != null) {
                    child.onNext(t);
                }
                add(child);
                child.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        remove(child);
                    }
                }));
            }
        });
        this.cacheCreator = cacheCreator;
    }

    private void add(Subscriber<? super T> child) {
        synchronized (subscribers) {
            subscribers.add(child);
        }
    }

    private void remove(Subscriber<? super T> child) {
        synchronized (subscribers) {
            subscribers.remove(child);
        }
    }

    @Override
    public boolean hasObservers() {
        return !getSubscribers().isEmpty();
    }

    @Override
    public void onCompleted() {
        final List<Subscriber<? super T>> subscribers = getSubscribers();
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        final List<Subscriber<? super T>> subscribers = getSubscribers();
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onError(e);
        }
    }

    /*
     * This is very dump solution not efficient but is straight forward so it is cool
     */
    private List<Subscriber<? super T>> getSubscribers() {
        synchronized (subscribers) {
            return new ArrayList<>(subscribers);
        }
    }

    @Override
    public void onNext(T t) {
        if (t != null) {
            final List<Subscriber<? super T>> subscribers = getSubscribers();
            for (Subscriber<? super T> subscriber : subscribers) {
                subscriber.onNext(t);
            }
        }
        cacheCreator.writeToCache(t);
    }


    public interface CacheCreator<T> {
        @Nullable
        T readFromCache();

        void writeToCache(@Nullable T data);
    }

    public static class InMemoryCache<T> implements CacheCreator<T> {

        @Nullable
        private T cache;

        public InMemoryCache(@Nullable T cache) {
            this.cache = cache;
        }

        @Nullable
        @Override
        public T readFromCache() {
            return cache;
        }

        @Override
        public void writeToCache(@Nullable T data) {
            cache = data;
        }
    }

    private static class DelegateOnSubscribe<T> implements OnSubscribe<T> {

        private OnSubscribe<T> delegate = null;

        @Override
        public void call(Subscriber<? super T> subscriber) {
            delegate.call(subscriber);
        }

        public void setDelegate(OnSubscribe<T> delegate) {
            this.delegate = delegate;
        }
    }
}
