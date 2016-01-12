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
