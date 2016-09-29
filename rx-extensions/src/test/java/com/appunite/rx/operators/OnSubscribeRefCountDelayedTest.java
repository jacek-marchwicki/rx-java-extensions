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

import com.appunite.rx.ObservableExtensions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class OnSubscribeRefCountDelayedTest {

    @Mock
    Observer<String> userObserver1;
    @Mock
    Observer<String> userObserver2;

    private TestScheduler scheduler;
    private PublishSubject<String> apiResponse;
    private Observable<String> userObservable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        scheduler = new TestScheduler();
        apiResponse = PublishSubject.create();
        userObservable = OnSubscribeRefCountDelayed.create(apiResponse.replay(1), 5, TimeUnit.SECONDS, scheduler);
    }

    @Test
    public void testSubscribeBeforeTimeout_dataIsReturned() throws Exception {
        final Subscription subscribe = userObservable.subscribe(userObserver1);
        apiResponse.onNext("franek");
        subscribe.unsubscribe();

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        userObservable.subscribe(userObserver2);
        verify(userObserver2).onNext("franek");
        verifyNoMoreInteractions(userObserver2);
    }

    @Test
    public void testSubscribeAfterTimeout_dataIsNotReturned() throws Exception {
        final Subscription subscribe = userObservable.subscribe(userObserver1);
        apiResponse.onNext("franek");
        subscribe.unsubscribe();

        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        userObservable.subscribe(userObserver2);

        verifyZeroInteractions(userObserver2);
    }

    @Test
    public void testSubscribeAfterTimeoutAndNewData_newDataIsReturned() throws Exception {
        final Subscription subscribe = userObservable.subscribe(userObserver1);
        apiResponse.onNext("franek");
        subscribe.unsubscribe();

        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        userObservable.subscribe(userObserver2);
        apiResponse.onNext("zygmnut");

        verify(userObserver2).onNext("zygmnut");
        verifyNoMoreInteractions(userObserver2);
    }
}