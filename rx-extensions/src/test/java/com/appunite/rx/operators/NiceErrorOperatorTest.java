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

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.observers.TestObserver;

import static com.google.common.truth.Truth.assert_;

public class NiceErrorOperatorTest {

    @Test
    public void testWhenErrorIsReturned_niceErrorIsCaptured() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();
        Observable.error(new IOException("error"))
                .lift(NiceErrorOperator.niceErrorOperator("Some error"))
                .subscribe(observer);

        final List<Throwable> errors = observer.getOnErrorEvents();
        assert_().that(errors)
                .hasSize(1);

        assert_().that(errors.get(0))
                .isInstanceOf(NiceErrorOperator.NiceRxError.class);
    }

    @Test
    public void testErrorWithMessage_messageIsPropagated() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();
        Observable.error(new IOException("error"))
                .lift(NiceErrorOperator.niceErrorOperator("Some error"))
                .subscribe(observer);

        //noinspection ThrowableResultOfMethodCallIgnored
        final NiceErrorOperator.NiceRxError error = (NiceErrorOperator.NiceRxError) observer
                .getOnErrorEvents().get(0);
        assert_().that(error.userMessage())
                .isEqualTo("Some error");
    }

    @Test
    public void testErrorWithoutMessage_hasStackTrace() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();
        Observable.error(new IOException("error"))
                .lift(NiceErrorOperator.niceErrorOperator())
                .subscribe(observer);

        //noinspection ThrowableResultOfMethodCallIgnored
        final NiceErrorOperator.NiceRxError error = (NiceErrorOperator.NiceRxError) observer
                .getOnErrorEvents().get(0);

        assert_().that(error.getMessage())
                .contains("NiceErrorOperatorTest");
        final StackTraceElement[] trace = error.stackTraceElements();
        assert_().that(trace)
                .isNotEmpty();
        assert_().that(trace[2].getClassName())
                .contains("NiceErrorOperatorTest");
    }

    @Test
    public void testErrorOccur_RootCauseIsPropagated() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();
        final IOException e = new IOException("error");

        Observable.error(e)
                .lift(NiceErrorOperator.niceErrorOperator("Some error"))
                .subscribe(observer);

        //noinspection ThrowableResultOfMethodCallIgnored
        assert_().that(observer.getOnErrorEvents().get(0).getCause())
                .isEqualTo(e);
    }

    @Test
    public void testOnNextIsPropagated() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();

        Observable.just("a")
                .lift(NiceErrorOperator.niceErrorOperator("Some error"))
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly("a");
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testOnCompletedIsPropagated() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();

        Observable.empty()
                .lift(NiceErrorOperator.niceErrorOperator("Some error"))
                .subscribe(observer);

        assert_().that(observer.getOnCompletedEvents())
                .hasSize(1);
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

}
