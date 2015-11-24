package com.appunite.rx.android.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.functions.Action1;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.verify;

public class LogTransformerTest {

    @Mock
    LogTransformer.Logger mLogger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNextAndCompleted() throws Exception {
        Observable.just(1)
                .compose(LogTransformer.transformer("tag", "test", mLogger))
                .subscribe();
        verify(mLogger).logNext(matches("tag"), anyString());
        verify(mLogger).logCompleted(matches("tag"), anyString());
    }

    @Test
    public void testError() throws Exception {
        Observable.error(new RuntimeException())
                .compose(LogTransformer.transformer("tag", "test", mLogger))
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
        verify(mLogger).logError(matches("tag"), anyString());
    }
}