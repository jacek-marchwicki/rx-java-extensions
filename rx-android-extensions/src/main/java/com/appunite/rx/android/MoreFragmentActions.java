package com.appunite.rx.android;

import android.support.v4.app.FragmentManager;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class MoreFragmentActions {

    @Nonnull
    public static Action1<? super Object> popBackStack(@Nonnull final FragmentManager fragmentManager) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                fragmentManager.popBackStack();
            }
        };
    }
}
