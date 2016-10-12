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
