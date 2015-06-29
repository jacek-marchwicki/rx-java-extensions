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

import org.junit.Test;

import rx.observers.TestObserver;
import rx.subjects.PublishSubject;

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
}