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

package com.appunite.rx.operators;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

public class MoreOperatorsObservableSwitchTest {

    @Nonnull
    private Observable<String> postBody(@Nonnull final String post, @Nonnull Scheduler scheduler) {
        // example observable that produces some changes (that never ends) of post body
        return Observable.interval(1, TimeUnit.SECONDS, scheduler)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return aLong + 1;
                    }
                })
                .startWith(0L)
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long value) {
                        return "" + post + "_" + value;
                    }
                });
    }

    @Test
    public void testObservableSwitch_returnsCorrectElements() throws Exception {
        final TestScheduler scheduler = Schedulers.test();
        final TestSubscriber<String> subscriber = new TestSubscriber<>();

        PublishSubject<List<String>> postIds = PublishSubject.create();

        postIds
                .compose(MoreOperators.observableSwitch(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String postId) {
                        return postBody(postId, scheduler);
                    }
                }))
                .map(new Func1<List<String>, String>() {
                    @Override
                    public String call(List<String> listPostBody) {
                        return Joiner.on("; ").join(listPostBody);
                    }
                })
                .subscribe(subscriber);

        postIds.onNext(ImmutableList.of("post1", "post2"));
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);

        subscriber.assertReceivedOnNext(ImmutableList.of("post1_0; post2_0",
                "post1_1; post2_0",
                "post1_1; post2_1",
                "post1_2; post2_1",
                "post1_2; post2_2"));

        postIds.onNext(ImmutableList.of("post1", "post3"));

        subscriber.assertReceivedOnNext(ImmutableList.of("post1_0; post2_0",
                "post1_1; post2_0",
                "post1_1; post2_1",
                "post1_2; post2_1",
                "post1_2; post2_2",
                "post1_2; post3_0"));

        postIds.onNext(ImmutableList.of("post1", "post2"));

        subscriber.assertReceivedOnNext(ImmutableList.of("post1_0; post2_0",
                "post1_1; post2_0",
                "post1_1; post2_1",
                "post1_2; post2_1",
                "post1_2; post2_2",
                "post1_2; post3_0",
                "post1_2; post2_0"));

    }
}
