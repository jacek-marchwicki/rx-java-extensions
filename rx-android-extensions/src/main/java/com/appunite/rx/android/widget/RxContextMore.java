package com.appunite.rx.android.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.widget.Toast;

import javax.annotation.Nonnull;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class RxContextMore {

    @Nonnull
    public static Action1<? super Object> startActivity(@Nonnull final Context context,
                                                        @Nonnull final Intent intent) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                context.startActivity(intent);
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> showToast(@Nonnull final Context context,
                                                    @Nonnull final String text) {
        checkNotNull(context);
        checkNotNull(text);
        return new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> showToast(@Nonnull final Context context,
                                                    @StringRes final int resId) {
        checkNotNull(context);
        return new Action1<Object>() {
            @Override
            public void call(Object ignore) {
                Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
