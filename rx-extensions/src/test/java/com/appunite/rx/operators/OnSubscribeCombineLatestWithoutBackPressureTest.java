/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appunite.rx.operators;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.functions.Func5;
import rx.functions.Func6;
import rx.functions.Func7;
import rx.functions.Func8;
import rx.functions.Func9;
import rx.observers.TestObserver;

import static com.google.common.truth.Truth.assert_;

public class OnSubscribeCombineLatestWithoutBackPressureTest {

    @Test
    public void testCombineLatest2() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        new Func2<String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2) {
                                return ImmutableList.of(s, s2);
                            }
                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest3() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        new Func3<String, String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3) {
                                return ImmutableList.of(s, s2, s3);
                            }
                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest4() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        new Func4<String, String, String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4) {
                                return ImmutableList.of(s, s2, s3, s4);
                            }
                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest5() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        Observable.just("e"),
                        new Func5<String, String, String, String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4,
                                                     String s5) {
                                return ImmutableList.of(s, s2, s3, s4, s5);
                            }

                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d", "e"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest6() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        Observable.just("e"),
                        Observable.just("f"),
                        new Func6<String, String, String, String, String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4,
                                                     String s5, String s6) {
                                return ImmutableList.of(s, s2, s3, s4, s5, s6);
                            }

                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d", "e", "f"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest7() throws Exception {

        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        Observable.just("e"),
                        Observable.just("f"),
                        Observable.just("g"),
                        new Func7<String, String, String, String, String, String, String,
                                List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4,
                                                     String s5, String s6, String s7) {
                                return ImmutableList.of(s, s2, s3, s4, s5, s6, s7);
                            }

                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d", "e", "f", "g"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest8() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        Observable.just("e"),
                        Observable.just("f"),
                        Observable.just("g"),
                        Observable.just("h"),
                        new Func8<String, String, String, String, String, String, String, String,
                                List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4,
                                                     String s5, String s6, String s7, String s8) {
                                return ImmutableList.of(s, s2, s3, s4, s5, s6, s7, s8);
                            }

                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d", "e", "f", "g", "h"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testCombineLatest9() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.just("b"),
                        Observable.just("c"),
                        Observable.just("d"),
                        Observable.just("e"),
                        Observable.just("f"),
                        Observable.just("g"),
                        Observable.just("h"),
                        Observable.just("i"),
                        new Func9<String, String, String, String, String, String, String, String,
                                String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2, String s3, String s4,
                                                     String s5, String s6, String s7, String s8,
                                                     String s9) {
                                return ImmutableList.of(s, s2, s3, s4, s5, s6, s7, s8, s9);
                            }

                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b", "c", "d", "e", "f", "g", "h", "i"));
        assert_().that(observer.getOnCompletedEvents())
                .containsExactly(Notification.createOnCompleted());
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }

    @Test
    public void testErrorOccur_propagateError() throws Exception {
        final TestObserver<Object> observer = new TestObserver<>();
        final IOException e = new IOException("e");
        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(Observable.just("a"),
                        Observable.error(e),
                        new Func2<String, Object, Object>() {
                            @Override
                            public Object call(String s, Object o) {
                                return null;
                            }
                        })
                .subscribe(observer);

        assert_().that(observer.getOnErrorEvents())
                .containsExactly(e);

    }

    @Test
    public void testWhenObservableIsNotCompleted_dataIsAlsoPropagated() throws Exception {
        final TestObserver<List<String>> observer = new TestObserver<>();
        final Observable<String> observableWithAButNeverCompleted = Observable.just("a")
                .mergeWith(Observable.<String>never());

        OnSubscribeCombineLatestWithoutBackPressure
                .combineLatest(observableWithAButNeverCompleted,
                        Observable.just("b"),
                        new Func2<String, String, List<String>>() {
                            @Override
                            public List<String> call(String s, String s2) {
                                return ImmutableList.of(s, s2);
                            }
                        })
                .subscribe(observer);

        assert_().that(observer.getOnNextEvents())
                .containsExactly(ImmutableList.of("a", "b"));
        assert_().that(observer.getOnCompletedEvents())
                .isEmpty();
        assert_().that(observer.getOnErrorEvents())
                .isEmpty();
    }
}