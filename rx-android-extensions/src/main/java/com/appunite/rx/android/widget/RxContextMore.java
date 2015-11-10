package com.appunite.rx.android.widget;

import android.content.Context;
import android.content.Intent;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class RxContextMore {

    @Nonnull
    public static Action1<? super Object> startActivity(@Nonnull final Context context,
                                                        @Nonnull final Intent intent) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                context.startActivity(intent);
            }
        };
    }
}
