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

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.observables.NetworkObservableProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observers.TestObserver;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.BehaviorSubject;

import static com.google.common.truth.Truth.assert_;

public class MoreOperatorsRepeatOnErrorOrNetworkTest {

    private TestScheduler scheduler = Schedulers.test();
    private TestObserver<ResponseOrError<String>> observable = new TestObserver<>();
    private AtomicBoolean returnError = new AtomicBoolean(false);
    private AtomicInteger subscriptions = new AtomicInteger(0);
    private AtomicInteger unSubscriptions = new AtomicInteger(0);
    private IOException error = new IOException();
    private Observable<String> baseObservable = Observable
            .create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    if (returnError.get()) {
                        subscriber.onError(error);
                    } else {
                        subscriber.onNext("success");
                        subscriber.onCompleted();
                    }
                }
            })
            .doOnSubscribe(new Action0() {
                @Override
                public void call() {
                    subscriptions.incrementAndGet();
                }
            })
            .doOnUnsubscribe(new Action0() {
                @Override
                public void call() {
                    unSubscriptions.incrementAndGet();
                }
            });

    private BehaviorSubject<NetworkObservableProvider.NetworkStatus> networkObservable = BehaviorSubject.create();

    private NetworkObservableProvider networkObservableProvider = new NetworkObservableProvider() {
        @Nonnull
        @Override
        public Observable<NetworkStatus> networkObservable() {
            return networkObservable;
        }
    };

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWhenSubscribeToSuccess_getNextValue() throws Exception {
        returnError.set(false);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromData("success"));
    }

    @Test
    public void testWhenSubscribeToSuccess_callObserveOnlyOnce() throws Exception {
        returnError.set(false);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);

        assert_().that(subscriptions.get()).isEqualTo(1);
        assert_().that(unSubscriptions.get()).isEqualTo(1);
    }

    @Test
    public void testWhenObservableReturnFail_returnError() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error));
    }

    @Test
    public void testWhenObservableReturnFail_subscribeAgainAfter2Seconds() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        assert_().that(subscriptions.get()).isEqualTo(2);
        assert_().that(unSubscriptions.get()).isEqualTo(2);
    }

    @Test
    public void testWhenObservableReturnsFailAgain_propagateBooth() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error), ResponseOrError.fromError(error));
    }

    @Test
    public void testWhenObservableAfterErrorWillReturnSuccess_propagateSuccess() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        returnError.set(false);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error), ResponseOrError.fromData("success"));
    }

    @Test
    public void testErrorConstantlyReturnedThrough2Seconds_returnThreeTimes() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        // 0. at the begging
        // 1. after 1 second
        // 2. after 2 second
        // SUM: 3 seconds
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error));

    }

    @Test
    public void testErrorConstantlyReturnedThrough4Seconds_returnFourTimes() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        // 0. at the begging
        // 1. after 1 second
        // 2. after 2 second
        // 3. after 4 second
        // SUM: 7 seconds
        scheduler.advanceTimeBy(7, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error));
    }

    @Test
    public void testAfterNetworkReturn_retryRequest() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        networkObservable.onNext(NetworkObservableProvider.NetworkStatus.NO_NETWORK);

        success.subscribe(observable);
        networkObservable.onNext(NetworkObservableProvider.NetworkStatus.GOOD);

        assert_().that(subscriptions.get()).isEqualTo(2);
    }

    @Test
    public void testAfterNetworkBecomeUnavailable_dontRetry() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));

        success.subscribe(observable);
        networkObservable.onNext(NetworkObservableProvider.NetworkStatus.NO_NETWORK);

        assert_().that(subscriptions.get()).isEqualTo(1);
    }

    @Test
    public void testWhenNetworkUnavailable_dontRetry() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));


        networkObservable.onNext(NetworkObservableProvider.NetworkStatus.NO_NETWORK);

        success.subscribe(observable);

        assert_().that(subscriptions.get()).isEqualTo(1);
    }

    @Test
    public void testWhenNetworkIsAvailable_dontRetry() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnErrorOrNetwork(networkObservableProvider, scheduler));


        networkObservable.onNext(NetworkObservableProvider.NetworkStatus.GOOD);

        success.subscribe(observable);

        assert_().that(subscriptions.get()).isEqualTo(1);
    }
}
