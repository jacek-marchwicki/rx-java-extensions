/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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

import android.support.v4.view.ViewPager;

import com.appunite.rx.android.internal.MainThreadSubscription;
import com.appunite.rx.android.internal.Preconditions;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;

public class RxViewPagerMore {
    @Nonnull
    public static Observable<Integer> pageSelected(@Nonnull final ViewPager viewPager) {
        return Observable.create(new OnSubscribePageSelected(viewPager));
    }

    private static class OnSubscribePageSelected implements Observable.OnSubscribe<Integer> {
        @Nonnull
        private final ViewPager viewPager;

        public OnSubscribePageSelected(@Nonnull final ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void call(final Subscriber<? super Integer> subscriber) {
            Preconditions.checkUiThread();

            final ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(final int position) {
                    subscriber.onNext(position);
                }

                @Override
                public void onPageScrollStateChanged(final int state) {

                }
            };
            viewPager.addOnPageChangeListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    viewPager.removeOnPageChangeListener(listener);
                }
            });
        }

    }
}
