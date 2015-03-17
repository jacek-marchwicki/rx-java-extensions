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
package com.appunite.rx;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ObservableExtensionsTest {

    private PublishSubject<String> subject;
    private Observable<String> behaviorCountObservable;

    @Mock
    Observer<? super String> stringObserver1;
    @Mock
    Observer<? super String> stringObserver2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        subject = PublishSubject.create();

        behaviorCountObservable = ObservableExtensions.behavior(subject).refCount();

    }

    @Test
    public void testBehaviorWithTwoParallelSchedulers_returnsSameValue() throws Exception {
        behaviorCountObservable.subscribe(stringObserver1);
        behaviorCountObservable.subscribe(stringObserver2);

        subject.onNext("test1");

        verify(stringObserver1).onNext("test1");
        verify(stringObserver2).onNext("test1");
        verifyZeroInteractions(stringObserver1);
        verifyZeroInteractions(stringObserver2);
    }

    @Test
    public void testBehaviorAfterUnsubscribinAllSubscribers_newSchedulerWillNotGetData() throws Exception {
        final Subscription s1 = behaviorCountObservable.subscribe(stringObserver1);
        subject.onNext("test1");
        verify(stringObserver1).onNext("test1");
        s1.unsubscribe();

        behaviorCountObservable.subscribe(stringObserver2);

        verifyZeroInteractions(stringObserver1);
        verifyZeroInteractions(stringObserver2);
    }

    @Test
    public void testBehaviorAfterSubsribingToAlreadySubscribedScheduler_getSameData() throws Exception {
        behaviorCountObservable.subscribe(stringObserver1);
        subject.onNext("test1");
        verify(stringObserver1).onNext("test1");
        verifyZeroInteractions(stringObserver1);

        behaviorCountObservable.subscribe(stringObserver2);

        verify(stringObserver2).onNext("test1");
        verifyZeroInteractions(stringObserver2);
    }

    @Test
    public void testBehaviorAfterGettingTwoDataOnFirstSubscription_newWillOnlyGetLastData() throws Exception {
        behaviorCountObservable.subscribe(stringObserver1);
        subject.onNext("test1");
        subject.onNext("test2");
        verify(stringObserver1).onNext("test1");
        verify(stringObserver1).onNext("test2");
        verifyZeroInteractions(stringObserver1);

        behaviorCountObservable.subscribe(stringObserver2);

        verify(stringObserver2).onNext("test2");
        verifyZeroInteractions(stringObserver2);
    }
}