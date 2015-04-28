/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.android;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

    @Nonnull
    public static Action1<? super String> setTitle(@Nonnull final Toolbar toolbar) {
        checkNotNull(toolbar);
        return new Action1<String>() {
            @Override
            public void call(String title) {
                toolbar.setTitle(title);
            }
        };
    }

    @Nonnull
    public static Action1<? super Boolean> setSwipeViewRefreshing(@Nonnull final SwipeRefreshLayout swipeRefreshLayout) {
        checkNotNull(swipeRefreshLayout);
        return new Action1<Boolean>() {
            @Override
            public void call(final Boolean refreshing) {
                swipeRefreshLayout.setRefreshing(refreshing);
            }
        };
    }

    @Nonnull
    public static Action1<? super Boolean> setDisableSwipeRefreshView(@Nonnull final SwipeRefreshLayout swipeRefreshLayout) {
        checkNotNull(swipeRefreshLayout);
        return new Action1<Boolean>() {
            @Override
            public void call(final Boolean isEnable) {
                swipeRefreshLayout.setEnabled(!isEnable);
            }
        };
    }
}
