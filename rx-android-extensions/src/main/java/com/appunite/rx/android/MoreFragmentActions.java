package com.appunite.rx.android;

import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class MoreFragmentActions {

    @Nonnull
    public static Action1<? super Object> finishActivity(@Nonnull final Fragment fragment) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                fragment.getActivity().finish();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> popBackStack(@Nonnull final Fragment fragment) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                fragment.getFragmentManager().popBackStack();
            }
        };
    }
}
