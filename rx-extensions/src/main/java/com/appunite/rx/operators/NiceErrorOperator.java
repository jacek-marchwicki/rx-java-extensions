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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscriber;

/**
 * Nice error operator is used to print nice stacktrace for your code with some additional info
 *
 * Normally when you implement code like this:
 * <pre>
 *     class YourClass {
 *         void yourMethod() {
 *             Observable.error(new Exception())
 *                 .subscribe();
 *         }
 *     }
 * </pre>
 *
 * You will get stack trace like:
 *
 * <pre>
 *     Caused by rx.exceptions.OnErrorNotImplementedException
 *     rx.Observable$30.onError (Observable.java:7358)
 *     rx.observers.SafeSubscriber._onError (SafeSubscriber.java:154)
 *     rx.observers.SafeSubscriber.onError (SafeSubscriber.java:111)
 * </pre>
 *
 * This does not mean a lot, so here helps {@link NiceErrorOperator#niceErrorOperator()}, so type:
 *
 *  <pre>
 *     class YourClass {
 *         void yourMethod() {
 *             Observable.error(new Exception())
 *                 .lift(NiceErrorOperator.niceErrorOperator("Your message"))
 *                 .subscribe();
 *         }
 *     }
 * </pre>
 *
 * You will get error like:
 * <pre>
 *     Caused by NiceRxError Your message
 *     - YourClass.yourMethod (YourClass.java:5)
 *     - YourClassCaller.callerMethod (YourClassCaller.java:710)
 *     - ...
 *     Caused by rx.exceptions.OnErrorNotImplementedException
 *     rx.Observable$30.onError (Observable.java:7358)
 *     rx.observers.SafeSubscriber._onError (SafeSubscriber.java:154)
 *     rx.observers.SafeSubscriber.onError (SafeSubscriber.java:111)
 * </pre>
 *
 * niceErrorOperator leave information about stack trace to place where it was called, here
 * in line 5 of YourClass.java file.
 *
 * Info: com.appunite.rx.android.LifecycleMainObservable#bindLifecycle() already contains handler
 *
 */
public class NiceErrorOperator {

    /**
     * Attach to error stack trace your message and calling stack trace
     *
     * @param message message to add to stacktrace (optional)
     * @see NiceErrorOperator
     */
    @Nonnull
    public static <T> Observable.Operator<T, T> niceErrorOperator(@Nullable final String message) {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return niceErrorOperator(message, stackTraceElements);
    }

    /**
     * Attach to error stack trace and message
     *
     * @param message            message to attach (optional)
     * @param stackTraceElements stack trace to attach (optional)
     * @see NiceErrorOperator
     */
    @Nonnull
    public static <T> Observable.Operator<T, T> niceErrorOperator(
            @Nullable final String message,
            @Nullable final StackTraceElement[] stackTraceElements) {
        return new Observable.Operator<T, T>() {
            @Override
            public Subscriber<? super T> call(final Subscriber<? super T> c) {
                return new Subscriber<T>(c) {
                    @Override
                    public void onCompleted() {
                        c.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        c.onError(new NiceRxError(message, stackTraceElements, e));
                    }

                    @Override
                    public void onNext(T o) {
                        c.onNext(o);
                    }
                };
            }
        };
    }

    @Nonnull
    private static String traceToString(@Nonnull final StackTraceElement[] stackTraceElements) {
        final StringBuilder sb = new StringBuilder();
        for (StackTraceElement stackTraceEl : stackTraceElements) {
            sb.append("- ");
            sb.append(stackTraceEl);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Nonnull
    private static String getErrorMessage(@Nullable String userMessage,
                                          @Nullable StackTraceElement[] stackTraceElements) {
        if (stackTraceElements != null) {
            final String trace = traceToString(stackTraceElements);
            if (userMessage != null) {
                return userMessage + ": " + trace;
            } else {
                return trace;
            }
        } else {
            if (userMessage != null) {
                return userMessage;
            } else {
                return "Unknown message";
            }
        }
    }

    /**
     * Attach calling method stack trace to your rx java error
     * @see NiceErrorOperator
     */
    @Nonnull
    public static <T> Observable.Operator<T, T> niceErrorOperator() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return niceErrorOperator(null, stackTraceElements);
    }

    /**
     * Nicer rx error with attached additional info
     */
    public static class NiceRxError extends Exception {

        @Nullable
        private final String userMessage;
        @Nullable
        private final StackTraceElement[] stackTraceElements;

        NiceRxError(@Nullable String userMessage,
                    @Nullable StackTraceElement[] stackTraceElements,
                    @Nullable Throwable cause) {
            super(getErrorMessage(userMessage, stackTraceElements), cause);
            this.userMessage = userMessage;
            this.stackTraceElements = stackTraceElements;
        }

        /**
         * User message
         *
         * @return user message
         */
        @Nullable
        public String userMessage() {
            return userMessage;
        }

        /**
         * User stack trace
         *
         * @return user stack trace
         */
        @Nullable
        public StackTraceElement[] stackTraceElements() {
            return stackTraceElements;
        }
    }
}
