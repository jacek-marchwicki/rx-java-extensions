package com.appunite.rx.operators;

import com.appunite.rx.ResponseOrError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observers.TestObserver;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static com.google.common.truth.Truth.assert_;

public class MoreOperatorsRepeatOnErrorTest {

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWhenSubscribeToSuccess_getNextValue() throws Exception {
        returnError.set(false);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromData("success"));
    }

    @Test
    public void testWhenSubscribeToSuccess_callObserveOnlyOnce() throws Exception {
        returnError.set(false);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);

        assert_().that(subscriptions.get()).isEqualTo(1);
        assert_().that(unSubscriptions.get()).isEqualTo(1);
    }

    @Test
    public void testWhenObservableReturnFail_returnError() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error));
    }

    @Test
    public void testWhenObservableReturnFail_subscribeAgainAfter10Seconds() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assert_().that(subscriptions.get()).isEqualTo(2);
        assert_().that(unSubscriptions.get()).isEqualTo(2);
    }

    @Test
    public void testWhenObservableReturnsFailAgain_propagateBooth() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error), ResponseOrError.fromError(error));
    }

    @Test
    public void testWhenObservableAfterErrorWillReturnSuccess_propagateSuccess() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);
        returnError.set(false);
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error), ResponseOrError.fromData("success"));
    }

    @Test
    public void testErrorConstantlyReturnedThrough30Seconds_returnFourTimes() throws Exception {
        returnError.set(true);
        final Observable<ResponseOrError<String>> success = baseObservable
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .compose(MoreOperators.<String>repeatOnError(scheduler));

        success.subscribe(observable);
        scheduler.advanceTimeBy(30, TimeUnit.SECONDS);

        assert_().that(observable.getOnNextEvents())
                .containsExactly(ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error),
                        ResponseOrError.fromError(error));

    }
}
