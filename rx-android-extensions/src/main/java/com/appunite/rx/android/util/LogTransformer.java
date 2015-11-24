package com.appunite.rx.android.util;

import android.support.annotation.NonNull;
import android.util.Log;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

public class LogTransformer {

    public interface Logger {

        Logger DEFAULT = new Logger() {
            @Override
            public void logNext(@NonNull String tag, @Nonnull String message) {
                Log.i(tag, message);
            }

            @Override
            public void logError(@NonNull String tag, @Nonnull String message) {
                Log.i(tag, message);
            }

            @Override
            public void logCompleted(@NonNull String tag, @Nonnull String message) {
                Log.i(tag, message);
            }
        };

        void logNext(@NonNull String tag, @Nonnull String message);

        void logError(@NonNull String tag, @Nonnull String message);

        void logCompleted(@NonNull String tag, @Nonnull String message);
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> transformer(@Nonnull final String logTag,
                                                               @Nonnull final String observableTag) {
        return transformer(logTag, observableTag, Logger.DEFAULT);
    }

    @Nonnull
    public static <T> Observable.Transformer<T, T> transformer(@Nonnull final String logTag,
                                                               @Nonnull final String observableTag,
                                                               @Nonnull final Logger logger) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .doOnNext(new Action1<T>() {
                            @Override
                            public void call(T value) {
                                logger.logNext(logTag, observableTag + " -> onNext : " + value);
                            }
                        })
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                logger.logError(logTag, observableTag + " -> onError : " + throwable);
                            }
                        })
                        .doOnCompleted(new Action0() {
                            @Override
                            public void call() {
                                logger.logCompleted(logTag, observableTag + " -> onCompleted");
                            }
                        });
            }
        };
    }
}
