/*
 * Copyright (C) 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.appunite.rx.util;

import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

public class LogTransformer {

    public interface Logger {

        Logger DEFAULT = new Logger() {
            @Override
            public void logOnSubscribe(@Nonnull final String tag, @Nonnull final String observableTag) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tag);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, observableTag + " -> onSubscribe");
                }
            }

            @Override
            public void logOnUnsubscribe(@Nonnull final String tag, @Nonnull final String observableTag) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tag);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, observableTag + " -> onUnsubscribe");
                }
            }

            @Override
            public void logOnNext(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Object object) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tag);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, observableTag + " -> onNext: " + object);
                }
            }

            @Override
            public void logOnError(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Throwable throwable) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tag);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, observableTag + " -> onError: " + throwable);
                }
            }

            @Override
            public void logOnCompleted(@Nonnull final String tag, @Nonnull final String observableTag) {
                final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(tag);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, observableTag + " -> onCompleted");
                }
            }
        };

        /**
         * Log onSubscribe
         *
         * @param tag           Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param observableTag Used to identify transformed observable.
         */
        void logOnSubscribe(@Nonnull final String tag, @Nonnull final String observableTag);

        /**
         * Log onUnsubscribe
         *
         * @param tag           Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param observableTag Used to identify transformed observable.
         */
        void logOnUnsubscribe(@Nonnull final String tag, @Nonnull final String observableTag);

        /**
         * Log onNext event.
         *
         * @param tag           Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param observableTag Used to identify transformed observable.
         * @param object        Object passed to onNext method.
         */
        void logOnNext(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Object object);

        /**
         * Log onError event.
         *
         * @param tag           Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param observableTag Used to identify transformed observable.
         * @param throwable     Throwable passed to onError method.
         */
        void logOnError(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Throwable throwable);

        /**
         * Log onCompleted event.
         *
         * @param tag           Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param observableTag Used to identify transformed observable.
         */
        void logOnCompleted(@Nonnull final String tag, @Nonnull final String observableTag);
    }

    /**
     * @param logTag        Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param observableTag Used to identify transformed observable.
     * @return Transformer adding logging onNext/onError/onCompleted events to observable.
     */
    @Nonnull
    public static <T> Observable.Transformer<T, T> transformer(@Nonnull final String logTag,
                                                               @Nonnull final String observableTag) {
        return transformer(logTag, observableTag, Logger.DEFAULT);
    }

    /**
     * @param logTag        Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param observableTag Used to identify transformed observable.
     * @param logger        Logger instance used to print messages.
     * @return Transformer adding logging onNext/onError/onCompleted events to observable.
     * @see Logger
     */
    @Nonnull
    public static <T> Observable.Transformer<T, T> transformer(@Nonnull final String logTag,
                                                               @Nonnull final String observableTag,
                                                               @Nonnull final Logger logger) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .doOnUnsubscribe(new Action0() {
                            @Override
                            public void call() {
                                logger.logOnUnsubscribe(logTag, observableTag);
                            }
                        })
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                logger.logOnSubscribe(logTag, observableTag);
                            }
                        })
                        .doOnNext(new Action1<T>() {
                            @Override
                            public void call(T value) {
                                logger.logOnNext(logTag, observableTag, value);
                            }
                        })
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                logger.logOnError(logTag, observableTag, throwable);
                            }
                        })
                        .doOnCompleted(new Action0() {
                            @Override
                            public void call() {
                                logger.logOnCompleted(logTag, observableTag);
                            }
                        });
            }
        };
    }
}
