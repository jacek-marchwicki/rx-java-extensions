package com.appunite.rx.operators;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MoreOperatorsAnimatorComposeTest {

    private TestScheduler scheduler;
    @Mock
    Observer<? super Number> numberObserver;

    @Before
    public void setUp() throws Exception {
        scheduler = Schedulers.test();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOneValue_justReturnIt() throws Exception {
        final Observable<Number> compose = Observable.just(10.f)
                .compose(MoreOperators.animatorCompose(scheduler, 160, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        verify(numberObserver).onNext(10.f);
    }

    @Test
    public void testReturnTwoValues_finallyGotSecond() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 160, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        scheduler.advanceTimeBy(10, TimeUnit.MINUTES);
        verify(numberObserver).onNext(20.0f);
    }

    @Test
    public void testReturnTwoValues2_finallyGotSecond() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 159, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        scheduler.advanceTimeBy(10, TimeUnit.MINUTES);
        verify(numberObserver).onNext(20.0f);
    }

    @Test
    public void testReturnTwoValues3_finallyGotSecond() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 161, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        scheduler.advanceTimeBy(10, TimeUnit.MINUTES);
        verify(numberObserver).onNext(20.0f);
    }

    @Test
    public void testSubscribeTwoValues_atTheBegginingOnlyFirstGet() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 160, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        verify(numberObserver).onNext(10.f);
        verifyNoMoreInteractions(numberObserver);
    }

    @Test
    public void testAdvanceByOneFrame_getTheSecondValue() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 160, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        verify(numberObserver).onNext(10.f);
        scheduler.advanceTimeBy(16, TimeUnit.MILLISECONDS);
        verify(numberObserver).onNext(11.f);
        verifyNoMoreInteractions(numberObserver);
    }

    @Test
    public void testAdvanceByTwoFrames_getTheThirdValue() throws Exception {
        final Observable<Number> compose = Observable.just(10.f, 20.f)
                .compose(MoreOperators.animatorCompose(scheduler, 160, TimeUnit.MILLISECONDS, new FloatEvaluator()));

        compose.subscribe(numberObserver);
        verify(numberObserver).onNext(10.f);
        scheduler.advanceTimeBy(32, TimeUnit.MILLISECONDS);
        verify(numberObserver).onNext(11.f);
        verify(numberObserver).onNext(12.f);
        verifyNoMoreInteractions(numberObserver);
    }
}
