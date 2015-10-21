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

import com.appunite.rx.functions.Functions1;

import org.junit.Test;

import rx.Observable;
import rx.Subscription;
import rx.observers.TestObserver;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import rx.subjects.TestSubject;

import static com.google.common.truth.Truth.assert_;

public class OperatorSampleWithLastWithObservableTest {

    @Test
    public void testWhenNameChange_propagateValueOnlyIfButtonClick() throws Exception {
        final PublishSubject<String> name = PublishSubject.create();
        final PublishSubject<Object> buttonClick = PublishSubject.create();

        final TestObserver<String> observer = new TestObserver<>();

        name
                .lift(OperatorSampleWithLastWithObservable.<String>create(buttonClick))
                .subscribe(observer);

        name.onNext("ja");
        name.onNext("jacek");

        buttonClick.onNext(new Object());

        assert_().that(observer.getOnNextEvents()).containsExactly("jacek");
    }

    @Test
    public void testWhenNameIsNotChanged_propagateOldValue() throws Exception {
        final PublishSubject<String> name = PublishSubject.create();
        final PublishSubject<Object> buttonClick = PublishSubject.create();

        final TestObserver<String> observer = new TestObserver<>();

        name
                .lift(OperatorSampleWithLastWithObservable.<String>create(buttonClick))
                .subscribe(observer);

        name.onNext("ja");

        buttonClick.onNext(new Object());

        assert_().that(observer.getOnNextEvents()).containsExactly("ja");

        buttonClick.onNext(new Object());

        assert_().that(observer.getOnNextEvents()).containsExactly("ja", "ja");
    }


    @Test
    public void testWhenNameChange_propagateNewValue() throws Exception {
        final PublishSubject<String> name = PublishSubject.create();
        final PublishSubject<Object> buttonClick = PublishSubject.create();
        final TestObserver<String> observer = new TestObserver<>();

        name
                .lift(OperatorSampleWithLastWithObservable.<String>create(buttonClick))
                .subscribe(observer);

        name.onNext("ja");
        buttonClick.onNext(new Object());

        assert_().that(observer.getOnNextEvents()).containsExactly("ja");

        name.onNext("jacek");
        buttonClick.onNext(new Object());

        assert_().that(observer.getOnNextEvents()).containsExactly("ja", "jacek");
    }

    @Test
    public void testAfterUnsubscribe_sourceIsUnsubscribed() throws Exception {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<String> name = TestSubject.create(scheduler);
        final TestSubject<Object> buttonClick = TestSubject.create(scheduler);

        final Subscription subscription = name
                .lift(OperatorSampleWithLastWithObservable.<String>create(buttonClick))
                .subscribe(new TestSubscriber<String>());

        assert_().that(name.hasObservers()).isTrue();
        assert_().that(buttonClick.hasObservers()).isTrue();

        subscription.unsubscribe();

        assert_().that(name.hasObservers()).isFalse();
        assert_().that(buttonClick.hasObservers()).isFalse();
    }


    @Test
    public void testPullFromSource_whenBackpressure() throws Exception {
        final TestSubscriber<Integer> result = new TestSubscriber<>();
        result.requestMore(0);

        Observable.range(1, 100)
                .lift(OperatorSampleWithLastWithObservable.<Integer>create(
                        Observable.range(1, 2).map(Functions1.toObject())))
                .subscribe(result);

        result.assertNoErrors();
        assert_().that(result.getOnNextEvents()).isEmpty();

        result.requestMore(1);

        result.assertNoErrors();
        assert_().that(result.getOnNextEvents()).containsExactly(100);

        result.requestMore(1);

        result.assertNoErrors();
        assert_().that(result.getOnNextEvents()).containsExactly(100, 100);
    }

    @Test
    public void testIfClickComplete_returnOnCompleted() throws Exception {
        final PublishSubject<String> name = PublishSubject.create();
        final PublishSubject<Object> buttonClick = PublishSubject.create();

        final TestSubscriber<String> result = new TestSubscriber<>();

        name
                .lift(OperatorSampleWithLastWithObservable.<String>create(buttonClick))
                .subscribe(result);

        assert_().that(result.getOnCompletedEvents()).isEmpty();

        name.onCompleted();

        assert_().that(result.getOnCompletedEvents()).isEmpty();

        buttonClick.onCompleted();

        assert_().that(result.getOnCompletedEvents()).hasSize(1);

    }
}