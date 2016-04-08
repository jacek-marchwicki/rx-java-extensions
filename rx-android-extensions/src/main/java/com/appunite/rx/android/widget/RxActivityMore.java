/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *                Paweł Schmidt <paw3l.schmidt@gmail.com>
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

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import javax.annotation.Nonnull;

import rx.functions.Action1;

public class RxActivityMore {

    @Nonnull
    public static Action1<? super Object> finish(@Nonnull final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                activity.finish();
            }
        };
    }

    @Nonnull
    public static Action1<? super Object> finishAfterTransition(@Nonnull final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {
                ActivityCompat.finishAfterTransition(activity);
            }
        };
    }

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
