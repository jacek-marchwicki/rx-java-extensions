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

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static com.google.common.truth.Truth.assert_;

public class ObservableExtensionsTest {

    @Nonnull
    private final PublishSubject<String> subject = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBehaviorRefCountWithTwoParallelSchedulers_returnsSameValue() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorRefCount());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();

        refCount.subscribe(stringSubscriber1);
        refCount.subscribe(stringSubscriber2);

        subject.onNext("test1");

        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber1.assertNoErrors();
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorConnectedWithTwoParallelSchedulers_returnsSameValue() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorConnected());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();

        refCount.subscribe(stringSubscriber1);
        refCount.subscribe(stringSubscriber2);

        subject.onNext("test1");

        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber1.assertNoErrors();
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorRefCountNextValueBeforeSubscription_isIgnoredAfterSubscribe() throws Exception {
        final Observable<String> connected = subject.compose(ObservableExtensions.<String>behaviorRefCount());
        final TestSubscriber<String> stringSubscriber = new TestSubscriber<>();

        subject.onNext("test1");

        connected.subscribe(stringSubscriber);

        assert_().that(stringSubscriber.getOnNextEvents()).isEqualTo(ImmutableList.of());
    }

    @Test
    public void testBehaviorConnectedNextValueBeforeSubscription_returnResultAfterSubscribe() throws Exception {
        final Observable<String> connected = subject.compose(ObservableExtensions.<String>behaviorConnected());
        final TestSubscriber<String> stringSubscriber = new TestSubscriber<>();

        subject.onNext("test1");

        connected.subscribe(stringSubscriber);

        assert_().that(stringSubscriber.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
    }

    @Test
    public void testBehaviorRefCountAfterUnsubscribinAllSubscribers_newSchedulerWillNotGetData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorRefCount());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();

        final Subscription s1 = refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        s1.unsubscribe();

        refCount.subscribe(stringSubscriber2);
        assert_().that(stringSubscriber2.getOnNextEvents()).isEmpty();

        stringSubscriber1.assertNoErrors();
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorConnectedAfterUnsubscribinAllSubscribers_newSchedulerWillGetData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorConnected());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();

        final Subscription s1 = refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        s1.unsubscribe();

        refCount.subscribe(stringSubscriber2);
        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));

        stringSubscriber1.assertNoErrors();
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorRefCountAfterSubsribingToAlreadySubscribedScheduler_getSameData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorRefCount());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();
        refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber1.assertNoErrors();

        refCount.subscribe(stringSubscriber2);

        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorConnectedAfterSubsribingToAlreadySubscribedScheduler_getSameData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorConnected());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();
        refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber1.assertNoErrors();

        refCount.subscribe(stringSubscriber2);

        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test1"));
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorRefCountAfterGettingTwoDataOnFirstSubscription_newWillOnlyGetLastData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorRefCount());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();
        refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        subject.onNext("test2");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1", "test2"));
        stringSubscriber1.assertNoErrors();

        refCount.subscribe(stringSubscriber2);

        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test2"));
        stringSubscriber2.assertNoErrors();
    }

    @Test
    public void testBehaviorConnectedAfterGettingTwoDataOnFirstSubscription_newWillOnlyGetLastData() throws Exception {
        final Observable<String> refCount = subject.compose(ObservableExtensions.<String>behaviorConnected());
        final TestSubscriber<String> stringSubscriber1 = new TestSubscriber<>();
        final TestSubscriber<String> stringSubscriber2 = new TestSubscriber<>();
        refCount.subscribe(stringSubscriber1);
        subject.onNext("test1");
        subject.onNext("test2");
        assert_().that(stringSubscriber1.getOnNextEvents()).isEqualTo(ImmutableList.of("test1", "test2"));
        stringSubscriber1.assertNoErrors();

        refCount.subscribe(stringSubscriber2);

        assert_().that(stringSubscriber2.getOnNextEvents()).isEqualTo(ImmutableList.of("test2"));
        stringSubscriber2.assertNoErrors();
    }
}