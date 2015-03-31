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

package com.appunite.rx.simpletests;

import com.appunite.rx.functions.Functions2;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class RepeatObservableTest {


    @Test
    public void testName() throws Exception {
        Observable.error(new Throwable())
                .retryWhen(getNotificationHandler(1, 10, TimeUnit.MICROSECONDS))
                .forEach(new Action1<Object>() {
                    @Override
                    public void call(final Object o) {

                    }
                });
    }

    private Func1<Observable<? extends Throwable>, Observable<?>> getNotificationHandler(
            final int retries, final long retryTime, final TimeUnit retryUnit) {
        return new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(final Observable<? extends Throwable> observable) {
                return observable
                        .zipWith(Observable.range(1, retries), Functions2.<Integer>secondParam())
                        .flatMap(new Func1<Integer, Observable<?>>() {
                            @Override
                            public Observable<?> call(final Integer o) {
                                return Observable.timer(retryTime, retryUnit);
                            }
                        });
            }
        };
    }
}
