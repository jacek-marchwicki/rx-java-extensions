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
import com.appunite.rx.android.internal.MainThreadSubscription;
import com.appunite.rx.android.internal.Preconditions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

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
            Preconditions.checkUiThread();

            final RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                    subscriber.onNext(new RecyclerScrollEvent(recyclerView, dx, dy));
                }
            };

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    recyclerView.removeOnScrollListener(listener);
                }
            });

            recyclerView.addOnScrollListener(listener);
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
            Preconditions.checkUiThread();
            sendSizeIfValid(subscriber);

            final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    sendSizeIfValid(subscriber);
                    return true;
                }
            };
            view.getViewTreeObserver().addOnPreDrawListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    view.getViewTreeObserver().removeOnPreDrawListener(listener);
                }
            });
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
            Preconditions.checkUiThread();

            final View.OnClickListener listener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    subscriber.onNext(v);
                }
            };

            toolbar.setNavigationOnClickListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    toolbar.setNavigationOnClickListener(null);
                }
            });
        }

    }

    @Nonnull
    public static Observable<View> navigationClick(@Nonnull Toolbar toolbar) {
        return Observable.create(new OnNavigationClick(toolbar));
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
            Preconditions.checkUiThread();

            final PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    subscriber.onNext(new PopupMenuEvent(popupMenu, menuItem));
                    return true;
                }
            };

            popupMenu.setOnMenuItemClickListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override protected void onUnsubscribe() {
                    popupMenu.setOnMenuItemClickListener(null);
                }
            });
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
            Preconditions.checkUiThread();

            final SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    subscriber.onNext(new SwipeRefreshLayoutRefreshEvent(swipeRefreshLayout));
                }

            };

            swipeRefreshLayout.setOnRefreshListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    swipeRefreshLayout.setOnRefreshListener(null);
                }
            });
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
            Preconditions.checkUiThread();
            final CompositeListener composite = CachedListeners.getFromViewOrCreate(toolbar);

            final Toolbar.OnMenuItemClickListener listener = new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    subscriber.onNext(new ToolbarMenuEvent(toolbar, item));
                    return true;
                }
            };

            toolbar.setOnMenuItemClickListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    toolbar.setOnMenuItemClickListener(null);
                }
            });
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

}
