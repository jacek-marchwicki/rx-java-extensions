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

package com.appunite.rx.example.dao.internal.helpers;

import com.appunite.login.CurrentLoggedInUserDao;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.functions.Func1;

public class RequestHelper {

    @Nonnull
    public static <T> Single<T> request(@Nonnull CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao,
                                        @Nonnull final Scheduler networkScheduler,
                                        @Nonnull final Func1<String, Single<T>> request) {
        return loggedInUserDao.authTokenObservable(false)
                .flatMap(new Func1<String, Single<T>>() {
                    @Override
                    public Single<T> call(String s) {
                        return request.call(s)
                                .subscribeOn(networkScheduler);
                    }
                });

    }
}
