package com.appunite.rx.android;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class MoreActivityActions {

    @Nonnull
    public static Action1<? super Object> startPostponedEnterTransition(@Nonnull final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ActivityCompat.startPostponedEnterTransition(activity);
            }
        };
    }
}
