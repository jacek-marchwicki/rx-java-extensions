/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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

package com.appunite.rx.android.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.Subscription;
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
    public void testSubscribeAndUnsubscribe() throws Exception {
        final Subscription subscription = Observable.just(1)
                .compose(LogTransformer.transformer("tag", "test", logger))
                .subscribe();

        subscription.unsubscribe();

        verify(logger).logSubscribe(matches("tag"), anyString());
        verify(logger).logUnsubscribe(matches("tag"), anyString());
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