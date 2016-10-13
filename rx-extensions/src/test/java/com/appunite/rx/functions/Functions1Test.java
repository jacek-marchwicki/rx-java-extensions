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

package com.appunite.rx.functions;

import org.junit.Test;

import java.util.LinkedHashMap;

import rx.Observable;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assert_;

public class Functions1Test {

    @Test
    public void testIsNullOrEmpty_null() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just((String) null)
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(true);
    }

    @Test
    public void testIsNullOrEmpty_emptyString() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just("")
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(true);
    }

    @Test
    public void testIsNullOrEmpty_nonEmptyString() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just("Super string!")
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(false);
    }

    @Test
    public void testToStringFunction_null() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just(null)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly((String) null);
    }

    @Test
    public void testToStringFunction_string() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just("Awesome string!")
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("Awesome string!");
    }

    @Test
    public void testToStringFunction_int() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just(1337)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("1337");
    }

    @Test
    public void testToStringFunction_hashMap() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();
        final LinkedHashMap<String, Integer> hashMap = new LinkedHashMap<>();
        hashMap.put("a", 1);
        hashMap.put("b", 4);
        hashMap.put("c", 7);

        Observable.just(hashMap)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("{a=1, b=4, c=7}");
    }

    @Test
    public void testInstanceOf_StringFiltered() throws Exception {
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();

        Observable.just(1, "hello", 3d)
                .filter(Functions1.instanceOf(String.class))
                .subscribe(testSubscriber);

        assert_().that(testSubscriber.getOnNextEvents()).containsExactly("hello");
    }
}