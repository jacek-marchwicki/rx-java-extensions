package com.appunite.rx.android.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.functions.Action1;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;

public class LogTransformerTest {

    @Mock
    LogTransformer.Logger logger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNextAndCompleted() throws Exception {
        Observable.just(1)
                .compose(LogTransformer.transformer("tag", "test", logger))
                .subscribe();
        verify(logger).logNext(matches("tag"), anyString(), eq(1));
        verify(logger).logCompleted(matches("tag"), anyString());
    }

    @Test
    public void testError() throws Exception {
        final RuntimeException exception = new RuntimeException();
        Observable.error(exception)
                .compose(LogTransformer.transformer("tag", "test", logger))
                .subscribe(
                        new Action1<Object>() {
                            @Override
                            public void call(Object o) {

                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
        verify(logger).logError(matches("tag"), anyString(), eq(exception));
    }
}