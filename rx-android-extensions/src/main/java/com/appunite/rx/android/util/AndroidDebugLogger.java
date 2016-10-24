package com.appunite.rx.android.util;

import android.util.Log;

import com.appunite.rx.util.LogTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AndroidDebugLogger implements LogTransformer.Logger {

    @Override
    public void logOnSubscribe(@Nonnull final String tag, @Nonnull final String observableTag) {
        Log.d(tag, observableTag + " -> onSubscribe");
    }

    @Override
    public void logOnUnsubscribe(@Nonnull final String tag, @Nonnull final String observableTag) {
        Log.d(tag, observableTag + " -> onUnsubscribe");
    }

    @Override
    public void logOnNext(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Object object) {
        Log.d(tag, observableTag + " -> onNext: " + object);
    }

    @Override
    public void logOnError(@Nonnull final String tag, @Nonnull final String observableTag, @Nullable final Throwable throwable) {
        Log.d(tag, observableTag + " -> onError: " + throwable);
    }

    @Override
    public void logOnCompleted(@Nonnull final String tag, @Nonnull final String observableTag) {
        Log.d(tag, observableTag + " -> onCompleted");
    }
}
