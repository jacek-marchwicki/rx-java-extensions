package com.appunite.rx.android;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.functions.Action0;

public class MoreViewObservables {

    @Nonnull
    public static Observable<Integer> onPageSelected(@Nonnull final ViewPager viewPager) {
        return Observable.create(new OnSubscribePageSelected(viewPager));
    }

    public static class RecyclerScrollEvent {
        @Nonnull
        private final RecyclerView recyclerView;
        private final int dx;
        private final int dy;

        public RecyclerScrollEvent(@Nonnull final RecyclerView recyclerView, final int dx, final int dy) {
            this.recyclerView = recyclerView;
            this.dx = dx;
            this.dy = dy;
        }

        @Nonnull
        public RecyclerView recyclerView() {
            return recyclerView;
        }

        public int dx() {
            return dx;
        }

        public int dy() {
            return dy;
        }
    }

    @Nonnull
    public static Observable<RecyclerScrollEvent> scroll(@Nonnull final RecyclerView recyclerView) {
        return Observable.create(new OnSubscribeRecyclerScroll(recyclerView));
    }

    private static class OnSubscribeRecyclerScroll implements Observable.OnSubscribe<RecyclerScrollEvent> {
        @Nonnull
        private final RecyclerView recyclerView;

        public OnSubscribeRecyclerScroll(@Nonnull final RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void call(final Subscriber<? super RecyclerScrollEvent> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(recyclerView);

            final RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                    subscriber.onNext(new RecyclerScrollEvent(recyclerView, dx, dy));
                }
            };

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    composite.removeOnScrollListener(listener);
                }
            });

            composite.addOnScrollListener(listener);
            subscriber.add(subscription);
        }



        private static class CompositeListener extends RecyclerView.OnScrollListener {
            private final List<RecyclerView.OnScrollListener> listeners = new ArrayList<>();

            public boolean addOnScrollListener(final RecyclerView.OnScrollListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnScrollListener(final RecyclerView.OnScrollListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                for (final RecyclerView.OnScrollListener listener : listeners) {
                    listener.onScrolled(recyclerView, dx, dy);
                }
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                for (final RecyclerView.OnScrollListener listener : listeners) {
                    listener.onScrollStateChanged(recyclerView, newState);
                }
            }
        }

        private static class CachedListeners {
            private static final Map<View, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final RecyclerView view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setOnScrollListener(listener);

                return listener;
            }
        }
    }

    private static class OnSubscribePageSelected implements Observable.OnSubscribe<Integer> {
        @Nonnull
        private final ViewPager viewPager;

        public OnSubscribePageSelected(@Nonnull final ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void call(final Subscriber<? super Integer> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(viewPager);

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
            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    composite.removeOnScrollListener(listener);
                }
            });

            composite.addOnScrollListener(listener);
            subscriber.add(subscription);
        }


        private static class CompositeListener implements ViewPager.OnPageChangeListener {
            private final List<ViewPager.OnPageChangeListener> listeners = new ArrayList<>();

            public boolean addOnScrollListener(final ViewPager.OnPageChangeListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnScrollListener(final ViewPager.OnPageChangeListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                for (final ViewPager.OnPageChangeListener listener : listeners) {
                    listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(final int position) {
                for (final ViewPager.OnPageChangeListener listener : listeners) {
                    listener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                for (final ViewPager.OnPageChangeListener listener : listeners) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        }

        private static class CachedListeners {
            private static final Map<View, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final ViewPager view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setOnPageChangeListener(listener);

                return listener;
            }
        }
    }
}
