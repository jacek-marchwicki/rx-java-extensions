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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.TestScheduler;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
public class OnSubscribeRedoWithNextTest {

    @Mock
    Observer<String> userObserver;
    @Mock
    Observer<? super Boolean> responseOnErrorObserver;
    private Observable<Boolean> response;

    private boolean returnError;
    private TestScheduler scheduler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        scheduler = new TestScheduler();

        final Observable<Boolean> apiResponse = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(returnError);
                subscriber.onCompleted();
            }
        });

        response = OnSubscribeRedoWithNext.repeatOn(apiResponse, new Func1<Notification<Boolean>, Observable<?>>() {
            @Override
            public Observable<?> call(final Notification<Boolean> notification) {
                if (notification.isOnNext()) {
                    final Boolean isError = notification.getValue();
                    return isError ? Observable.timer(1, TimeUnit.SECONDS, scheduler) : Observable.never();
                } else {
                    return Observable.never();
                }
            }
        });
    }

    @Test
    public void testErrorResponse_onNextWithError() throws Exception {
        returnError = true;

        response.subscribe(responseOnErrorObserver);

        verify(responseOnErrorObserver).onNext(true);
        verifyNoMoreInteractions(responseOnErrorObserver);
    }

    @Test
    public void testSuccessResponse_onNextWithSuccess() throws Exception {
        returnError = false;

        response.subscribe(responseOnErrorObserver);

        verify(responseOnErrorObserver).onNext(false);
        verifyNoMoreInteractions(responseOnErrorObserver);
    }

    @Test
    public void testFirstErrorAndAfter1SecondSuccess_callOnNextWithSuccess() throws Exception {
        returnError = true;
        response.subscribe(responseOnErrorObserver);
        verify(responseOnErrorObserver).onNext(true);
        reset(responseOnErrorObserver);
        returnError = false;

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        verify(responseOnErrorObserver).onNext(false);
        verifyNoMoreInteractions(responseOnErrorObserver);
    }

    @Test
    public void testSuccessResponse_onNextIsCalledOnlyOnce() throws Exception {
        returnError = false;
        response.subscribe(responseOnErrorObserver);
        verify(responseOnErrorObserver).onNext(false);

        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        verifyNoMoreInteractions(responseOnErrorObserver);
    }

    @Test
    public void testErrorAfterTwoSeconds_isCalledTwice() throws Exception {
        returnError = true;
        response.subscribe(responseOnErrorObserver);
        verify(responseOnErrorObserver).onNext(true);
        reset(responseOnErrorObserver);

        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);

        verify(responseOnErrorObserver, times(2)).onNext(true);
    }
}
