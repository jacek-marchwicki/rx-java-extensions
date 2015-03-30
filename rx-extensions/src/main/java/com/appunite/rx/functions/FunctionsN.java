package com.appunite.rx.functions;

import javax.annotation.Nonnull;

import rx.functions.FuncN;

public class FunctionsN {
    @Nonnull
    public static FuncN<Boolean> returnTrue() {
        return new FuncN<Boolean>() {
            @Override
            public Boolean call(final Object... args) {
                return true;
            }
        };
    }

    public static FuncN<Boolean> returnFalse() {
        return new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                return false;
            }
        };
    }

    @Nonnull
    public static FuncN<Throwable> combineFirstThrowable() {
        return new FuncN<Throwable>() {
            @Override
            public Throwable call(final Object... args) {
                for (Object arg : args) {
                    final Throwable throwable = (Throwable) arg;
                    if (throwable != null) {
                        return throwable;
                    }
                }
                return null;
            }
        };
    }
}
