package com.appunite.rx.android.widget;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.android.internal.MainThreadSubscription;
import com.appunite.rx.android.internal.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class RxToolbarMore {
    @Nonnull
    public static Action1<? super String> title(@Nonnull final Toolbar toolbar) {
        checkNotNull(toolbar);
        return new Action1<String>() {
            @Override
            public void call(String title) {
                toolbar.setTitle(title);
            }
        };
    }

    @Nonnull
    public static Observable<ToolbarMenuEvent> toolbarMenuClick(@Nonnull Toolbar toolbar) {
        return Observable.create(new OnSubscribeToolbarMenuClick(toolbar));
    }

    @Nonnull
    public static Func1<ToolbarMenuEvent, Boolean> filterMenuClick(@IdRes final int menuId) {
        return new Func1<ToolbarMenuEvent, Boolean>() {
            @Override
            public Boolean call(ToolbarMenuEvent toolbarMenuEvent) {
                return toolbarMenuEvent.menuItem().getItemId() == menuId;
            }
        };
    }

    @Nonnull
    public static Observable<View> navigationClick(@Nonnull Toolbar toolbar) {
        return Observable.create(new OnNavigationClick(toolbar));
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
}
