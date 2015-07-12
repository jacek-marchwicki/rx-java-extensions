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

import android.support.annotation.IdRes;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.appunite.rx.Size;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;

public class MoreViewObservables {

    @Nonnull
    public static Observable<Integer> viewWidth(@Nonnull final View view) {
        return viewSize(view)
                .map(new Func1<ViewSize, Integer>() {
                    @Override
                    public Integer call(ViewSize viewSize) {
                        return viewSize.width();
                    }
                })
                .distinctUntilChanged();
    }

    @Nonnull
    public static Observable<Integer> viewHeight(@Nonnull final View view) {
        return viewSize(view)
                .map(new Func1<ViewSize, Integer>() {
                    @Override
                    public Integer call(ViewSize viewSize) {
                        return viewSize.height();
                    }
                })
                .distinctUntilChanged();
    }

    @Nonnull
    public static Observable<ViewSize> viewSize(@Nonnull final View view) {
        return Observable.create(new OnViewSize(view))
                .distinctUntilChanged();
    }

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

    public static class ViewSize extends Size {
        @Nonnull
        private final View view;

        public ViewSize(@Nonnull View view, int width, int height) {
            super(width, height);
            this.view = view;
        }

        @Nonnull
        public View view() {
            return view;
        }

        @Override
        protected MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("view", view);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ViewSize)) return false;
            if (!super.equals(o)) return false;
            ViewSize viewSize = (ViewSize) o;
            return Objects.equal(view, viewSize.view);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), view);
        }
    }

    private static class OnViewSize implements Observable.OnSubscribe<ViewSize> {
        private View view;

        public OnViewSize(@Nonnull View view) {
            this.view = view;
        }

        @Override
        public void call(final Subscriber<? super ViewSize> subscriber) {
            Assertions.assertUiThread();
            sendSizeIfValid(subscriber);

            final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    sendSizeIfValid(subscriber);
                    return true;
                }
            };
            view.getViewTreeObserver().addOnPreDrawListener(listener);

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    view.getViewTreeObserver().removeOnPreDrawListener(listener);
                }
            });
            subscriber.add(subscription);
        }

        protected void sendSizeIfValid(Subscriber<? super ViewSize> subscriber) {
            final int width = view.getWidth();
            final int height = view.getHeight();
            if (width > 0 && height > 0 && !view.isLayoutRequested()) {
                subscriber.onNext(new ViewSize(view, width, height));
            }
        }

    }

    private static class OnNavigationClick implements Observable.OnSubscribe<View> {
        @Nonnull
        private final Toolbar toolbar;

        public OnNavigationClick(@Nonnull final Toolbar toolbar) {
            this.toolbar = toolbar;
        }

        @Override
        public void call(final Subscriber<? super View> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(toolbar);

            final View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    subscriber.onNext(v);
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



        private static class CompositeListener implements View.OnClickListener {
            private final List<View.OnClickListener> listeners = new ArrayList<>();

            public boolean addOnScrollListener(final View.OnClickListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnScrollListener(final View.OnClickListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public void onClick(@Nonnull View v) {
                for (final View.OnClickListener listener : listeners) {
                    listener.onClick(v);
                }
            }
        }

        private static class CachedListeners {
            private static final Map<View, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final Toolbar view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setNavigationOnClickListener(listener);

                return listener;
            }
        }
    }

    @Nonnull
    public static Observable<View> navigationClick(@Nonnull Toolbar toolbar) {
        return Observable.create(new OnNavigationClick(toolbar))
                .distinctUntilChanged();
    }

    public static class PopupMenuEvent {

        @Nonnull
        private final PopupMenu popupMenu;
        @Nonnull
        private final MenuItem menuItem;

        public PopupMenuEvent(@Nonnull PopupMenu popupMenu, @Nonnull MenuItem menuItem) {

            this.popupMenu = popupMenu;
            this.menuItem = menuItem;
        }

        @Nonnull
        public PopupMenu popupMenu() {
            return popupMenu;
        }

        @Nonnull
        public MenuItem menuItem() {
            return menuItem;
        }
    }

    @Nonnull
    public static Observable<PopupMenuEvent> popupMenuClick(@Nonnull PopupMenu popupMenu) {
        return Observable.create(new OnSubscribePopupMenuClick(popupMenu));
    }

    @Nonnull
    public static Observable<PopupMenuEvent> popupMenuClick(@Nonnull PopupMenu popupMenu, @IdRes final int menuId) {
        return popupMenuClick(popupMenu)
                .filter(new Func1<PopupMenuEvent, Boolean>() {
                    @Override
                    public Boolean call(PopupMenuEvent popupMenuEvent) {
                        return popupMenuEvent.menuItem().getItemId() == menuId;
                    }
                });
    }

    private static class OnSubscribePopupMenuClick implements Observable.OnSubscribe<PopupMenuEvent> {
        @Nonnull
        private final PopupMenu popupMenu;

        public OnSubscribePopupMenuClick(@Nonnull final PopupMenu popupMenu) {
            this.popupMenu = popupMenu;
        }

        @Override
        public void call(final Subscriber<? super PopupMenuEvent> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(popupMenu);

            final PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    subscriber.onNext(new PopupMenuEvent(popupMenu, menuItem));
                    return true;
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


        private static class CompositeListener implements PopupMenu.OnMenuItemClickListener  {
            private final List<PopupMenu.OnMenuItemClickListener> listeners = new ArrayList<>();

            public boolean addOnScrollListener(final PopupMenu.OnMenuItemClickListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnScrollListener(final PopupMenu.OnMenuItemClickListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean ret = false;
                for (final PopupMenu.OnMenuItemClickListener listener : listeners) {
                    ret |= listener.onMenuItemClick(menuItem);
                }
                return ret;
            }
        }

        private static class CachedListeners {
            private static final Map<PopupMenu, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final PopupMenu view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setOnMenuItemClickListener(listener);

                return listener;
            }
        }
    }

    public static class SwipeRefreshLayoutRefreshEvent {

        @Nonnull
        private final SwipeRefreshLayout swipeRefreshLayout;

        public SwipeRefreshLayoutRefreshEvent(@Nonnull SwipeRefreshLayout swipeRefreshLayout) {

            this.swipeRefreshLayout = swipeRefreshLayout;
        }

        @Nonnull
        public SwipeRefreshLayout swipeRefreshLayout() {
            return swipeRefreshLayout;
        }

    }


    @Nonnull
    public static Observable<SwipeRefreshLayoutRefreshEvent> swipeRefreshLayoutRefresh(@Nonnull SwipeRefreshLayout swipeRefreshLayout) {
        return Observable.create(new OnSubscribeSwipeRefreshLayoutRefresh(swipeRefreshLayout));
    }

    private static class OnSubscribeSwipeRefreshLayoutRefresh implements Observable.OnSubscribe<SwipeRefreshLayoutRefreshEvent> {
        @Nonnull
        private final SwipeRefreshLayout swipeRefreshLayout;

        public OnSubscribeSwipeRefreshLayoutRefresh(@Nonnull final SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayout = swipeRefreshLayout;
        }

        @Override
        public void call(final Subscriber<? super SwipeRefreshLayoutRefreshEvent> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(swipeRefreshLayout);

            final SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    subscriber.onNext(new SwipeRefreshLayoutRefreshEvent(swipeRefreshLayout));
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


        private static class CompositeListener implements SwipeRefreshLayout.OnRefreshListener  {
            private final List<SwipeRefreshLayout.OnRefreshListener> listeners = new ArrayList<>();

            public boolean addOnScrollListener(final SwipeRefreshLayout.OnRefreshListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnScrollListener(final SwipeRefreshLayout.OnRefreshListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public void onRefresh() {
                for (final SwipeRefreshLayout.OnRefreshListener listener : listeners) {
                    listener.onRefresh();
                }
            }
        }

        private static class CachedListeners {
            private static final Map<SwipeRefreshLayout, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final SwipeRefreshLayout view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setOnRefreshListener(listener);

                return listener;
            }
        }
    }

    @Nonnull
    public static Observable<ToolbarMenuEvent> toolbarMenuClick(@Nonnull Toolbar toolbar) {
        return Observable.create(new OnSubscribeToolbarMenuClick(toolbar));
    }

    @Nonnull
    public static Observable<ToolbarMenuEvent> toolbarMenuClick(@Nonnull Toolbar toolbar, @IdRes final int menuId) {
        return toolbarMenuClick(toolbar)
                .filter(new Func1<ToolbarMenuEvent, Boolean>() {
                    @Override
                    public Boolean call(ToolbarMenuEvent toolbarMenuEvent) {
                        return toolbarMenuEvent.menuItem().getItemId() == menuId;
                    }
                });
    }

    private static class OnSubscribeToolbarMenuClick implements Observable.OnSubscribe<ToolbarMenuEvent> {
        @Nonnull
        private final Toolbar toolbar;

        public OnSubscribeToolbarMenuClick(@Nonnull final Toolbar toolbar) {
            this.toolbar = toolbar;
        }

        @Override
        public void call(final Subscriber<? super ToolbarMenuEvent> subscriber) {
            Assertions.assertUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(toolbar);


            final Toolbar.OnMenuItemClickListener listener = new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    subscriber.onNext(new ToolbarMenuEvent(toolbar, item));
                    return true;
                }
            };

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    composite.removeOnMenuItemClickListener(listener);
                }
            });

            composite.addOnMenuItemClickListener(listener);
            subscriber.add(subscription);
        }


        private static class CompositeListener implements Toolbar.OnMenuItemClickListener  {
            private final List<Toolbar.OnMenuItemClickListener> listeners = new ArrayList<>();

            public boolean addOnMenuItemClickListener(final Toolbar.OnMenuItemClickListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnMenuItemClickListener(final Toolbar.OnMenuItemClickListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean ret = false;
                for (final Toolbar.OnMenuItemClickListener listener : listeners) {
                    ret |= listener.onMenuItemClick(menuItem);
                }
                return ret;
            }
        }

        private static class CachedListeners {
            private static final Map<Toolbar, CompositeListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeListener getFromViewOrCreate(final Toolbar view) {
                final CompositeListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeListener listener = new CompositeListener();

                sCachedListeners.put(view, listener);
                view.setOnMenuItemClickListener(listener);

                return listener;
            }
        }
    }

    public static class ToolbarMenuEvent {

        @Nonnull
        private final Toolbar toolbar;
        @Nonnull
        private final MenuItem menuItem;

        public ToolbarMenuEvent(@Nonnull Toolbar toolbar, @Nonnull MenuItem menuItem) {
            this.toolbar = toolbar;
            this.menuItem = menuItem;
        }

        @Nonnull
        public Toolbar toolbarMenu() {
            return toolbar;
        }

        @Nonnull
        public MenuItem menuItem() {
            return menuItem;
        }
    }

    @Nonnull
    public static Observable<OnLongClickEvent> longClicks(final View view) {
        return Observable.create(new OnSubscribeViewLongClick(view));
    }

    private static class OnSubscribeViewLongClick implements Observable.OnSubscribe<OnLongClickEvent> {

        private final View view;

        public OnSubscribeViewLongClick(final View view) {
            this.view = view;
        }

        @Override
        public void call(final Subscriber<? super OnLongClickEvent> observer) {
            Assertions.assertUiThread();
            final CompositeOnLongClickListener composite = CachedListeners.getFromViewOrCreate(view);

            final View.OnLongClickListener listener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    observer.onNext(new OnLongClickEvent(view));
                    return true;
                }
            };

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    composite.removeOnClickListener(listener);
                }
            });

            composite.addOnClickListener(listener);
            observer.add(subscription);
        }

        private static class CompositeOnLongClickListener implements View.OnLongClickListener {
            private final List<View.OnLongClickListener> listeners = new ArrayList<>();

            public boolean addOnClickListener(final View.OnLongClickListener listener) {
                return listeners.add(listener);
            }

            public boolean removeOnClickListener(final View.OnLongClickListener listener) {
                return listeners.remove(listener);
            }

            @Override
            public boolean onLongClick(View view) {
                for (final View.OnLongClickListener listener : listeners) {
                    listener.onLongClick(view);
                }
                return true;
            }
        }

        private static class CachedListeners {
            private static final Map<View, CompositeOnLongClickListener> sCachedListeners = new WeakHashMap<>();

            public static CompositeOnLongClickListener getFromViewOrCreate(final View view) {
                final CompositeOnLongClickListener cached = sCachedListeners.get(view);

                if (cached != null) {
                    return cached;
                }

                final CompositeOnLongClickListener listener = new CompositeOnLongClickListener();

                sCachedListeners.put(view, listener);
                view.setOnLongClickListener(listener);

                return listener;
            }
        }
    }

    public static class OnLongClickEvent {
        private final View view;

        public OnLongClickEvent(View view) {
            checkNotNull(view != null, "View is null!");
            this.view = view;
        }

        public View view() {
            return view;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final OnLongClickEvent that = (OnLongClickEvent) o;

            return view.equals(that.view);

        }

        @Override
        public int hashCode() {
            return view.hashCode();
        }

        @Override
        public String toString() {
            return "OnLongClickEvent{" +
                    "view=" + view +
                    '}';
        }
    }
}
