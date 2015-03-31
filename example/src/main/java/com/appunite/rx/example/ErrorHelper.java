package com.appunite.rx.example;

import rx.functions.Func1;

public class ErrorHelper {
    public static Func1<Throwable, String> mapThrowableToStringError() {
        return new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                if (throwable == null) {
                    return null;
                }
                return "Some error: " + throwable.getMessage();
            }
        };
    }
}
