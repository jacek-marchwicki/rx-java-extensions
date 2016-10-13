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

package com.appunite.login;

import com.appunite.rx.ResponseOrError;

import javax.annotation.Nonnull;

import rx.Observable;

/**
 * Dao that contain currently logged in user
 */
public interface CurrentLoggedInUserDao {

    class NotAuthorizedException extends Exception {

        public NotAuthorizedException() {
            super();
        }

        public NotAuthorizedException(String s) {
            super(s);
        }

        public NotAuthorizedException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public NotAuthorizedException(Throwable throwable) {
            super(throwable);
        }

    }

    /**
     * Dao that represent logged in user
     *
     * This dao should have method equals that will be true for the same user id
     */
    interface LoggedInUserDao {

        /**
         * User id
         *
         * @return user id
         */
        @Nonnull
        String userId();

        /**
         * Return observable that will return auth token or onError if fetching token fail
         *
         * This observable is one shot, so it onComplete after fetching auth token
         *
         * @param forceRefresh we send information to client that we need to refresh data
         * @return observable with auth token or error
         */
        @Nonnull
        Observable<String> authTokenObservable(boolean forceRefresh);

    }

    /**
     * Observable returns currently logged in user
     *
     * This observable will never ends
     *
     * @return LoggedInUserDao or error observable
     */
    @Nonnull
    Observable<ResponseOrError<LoggedInUserDao>> currentLoggedInUserObservable();
}
