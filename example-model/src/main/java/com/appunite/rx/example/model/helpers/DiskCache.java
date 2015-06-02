package com.appunite.rx.example.model.helpers;

import com.appunite.rx.subjects.CacheSubject;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Func0;
import rx.internal.operators.OperatorMulticast;
import rx.observables.ConnectableObservable;
import rx.subjects.Subject;

public class DiskCache {
    @Nonnull
    public static <T> ConnectableObservable<T> behavior(final @Nonnull Observable<T> observable,
                                                        final @Nonnull CacheSubject.CacheCreator<T> cacheCreator) {
        return new OperatorMulticast<>(observable, new Func0<Subject<? super T, ? extends T>>() {

            @Override
            public Subject<? super T, ? extends T> call() {
                return CacheSubject.create(cacheCreator);
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> behaviorRefCount(
            final @Nonnull CacheSubject.CacheCreator<T> cacheCreator) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> tObservable) {
                return behavior(tObservable, cacheCreator).refCount();
            }
        };
    }
}
