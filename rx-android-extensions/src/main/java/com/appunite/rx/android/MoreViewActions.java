package com.appunite.rx.android;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Checkable;

import javax.annotation.Nonnull;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class MoreViewActions {

    @Nonnull
    public static Action1<? super Boolean> setChecked(@Nonnull final Checkable view) {
        checkNotNull(view);
        return new Action1<Boolean>() {
            @Override
            public void call(final Boolean checked) {
                view.setChecked(checked);
            }
        };
    }

    @Nonnull
    public static Action1<? super Number> translateX(@Nonnull final View view) {
        checkNotNull(view);
        return new Action1<Number>() {
            @Override
            public void call(final Number number) {
                ViewCompat.setTranslationX(view, number.floatValue());
            }
        };
    }

    @Nonnull
    public static Action1<? super Number> translateY(@Nonnull final View view) {
        checkNotNull(view);
        return new Action1<Number>() {
            @Override
            public void call(final Number number) {
                ViewCompat.setTranslationY(view, number.floatValue());
            }
        };
    }

    @Nonnull
    public static Action1<? super Number> setAlpha(@Nonnull final View view) {
        checkNotNull(view);
        return new Action1<Number>() {
            @Override
            public void call(final Number number) {
                ViewCompat.setAlpha(view, number.floatValue());
            }
        };
    }

    public static Action1<? super String> setTitle(@Nonnull final Toolbar toolbar) {
        checkNotNull(toolbar);
        return new Action1<String>() {
            @Override
            public void call(String title) {
                toolbar.setTitle(title);
            }
        };
    }
}
