package com.appunite.rx.example.model.helpers;

import com.appunite.login.CurrentLoggedInUserDao;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class RequestHelper {

    @Nonnull
    public static <T> Observable<T> request(@Nonnull CurrentLoggedInUserDao.LoggedInUserDao loggedInUserDao,
                                            @Nonnull final Scheduler networkScheduler,
                                            @Nonnull final Func1<String, Observable<T>> request) {
        return loggedInUserDao.authTokenObservable(false)
                .flatMap(new Func1<String, Observable<T>>() {
                    @Override
                    public Observable<T> call(String s) {
                        return request.call(s)
                                .subscribeOn(networkScheduler)
                                .unsubscribeOn(networkScheduler);
                    }
                });

    }
}
