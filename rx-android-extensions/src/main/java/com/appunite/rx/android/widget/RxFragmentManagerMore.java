package com.appunite.rx.android.widget;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class RxFragmentManagerMore {

    @Nonnull
    public static Action1<? super Object> popBackStack(@Nonnull final FragmentManager fragmentManager) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.popBackStack();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> add(@Nonnull final FragmentManager fragmentManager,
                                              @Nonnull final Fragment fragment,
                                              @Nonnull final String tag) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.beginTransaction()
                        .add(fragment, tag)
                        .commit();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> add(@Nonnull final FragmentManager fragmentManager,
                                              @IdRes final int containerViewId,
                                              @Nonnull final Fragment fragment,
                                              @Nonnull final String tag) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.beginTransaction()
                        .add(containerViewId, fragment, tag)
                        .commit();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> add(@Nonnull final FragmentManager fragmentManager,
                                              @IdRes final int containerViewId,
                                              @Nonnull final Fragment fragment) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.beginTransaction()
                        .add(containerViewId, fragment)
                        .commit();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> replace(@Nonnull final FragmentManager fragmentManager,
                                                  @IdRes final int containerViewId,
                                                  @Nonnull final Fragment fragment,
                                                  @Nonnull final String tag) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.beginTransaction()
                        .replace(containerViewId, fragment, tag)
                        .commit();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> replace(@Nonnull final FragmentManager fragmentManager,
                                                  @IdRes final int containerViewId,
                                                  @Nonnull final Fragment fragment) {
        return new Action1<Object>() {
            @Override
            public void call(Object ignored) {
                fragmentManager.beginTransaction()
                        .replace(containerViewId, fragment)
                        .commit();
            }
        };
    }
}
