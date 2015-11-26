/*
 * Copyright 2015 Paweł Schmidt <paw3l.schmidt@gmail.com>
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

package com.appunite.rx.android.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.widget.Toast;

import javax.annotation.Nonnull;

import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class RxContextMore {

    /**
     * Launch a new activity.
     * @param context The context to use. Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     * @param intent The description of the activity to start.
     * @return Action in which launches a new activity.
     */
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

    /**
     * Make a standard toast that just contains a text view and show the view
     * for the Toast.LENGTH_SHORT duration.
     * @param context The context to use. Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     * @param text The text to show. Can be formatted text.
     * @return Action in which shows a toast.
     */
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

    /**
     * Make a standard toast that just contains a text view and show the view
     * for the Toast.LENGTH_SHORT duration.
     * @param context The context to use. Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     * @param resId The resource id of the string resource to use. Can be formatted text
     * @return Action in which shows a toast.
     */
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

    /**
     * Make a standard toast that just contains a text view and show the view
     * for the Toast.LENGTH_SHORT duration.
     * @param context The context to use. Usually your {@link android.app.Application}
     *                or {@link android.app.Activity} object.
     * @return Action in which shows a toast with text passed in call method.
     */
    @Nonnull
    public static Action1<? super CharSequence> showToast(@Nonnull final Context context) {
        checkNotNull(context);
        return new Action1<CharSequence>() {
            @Override
            public void call(CharSequence text) {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
