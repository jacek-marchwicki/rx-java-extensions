package com.appunite.rx.android.widget;

import android.support.annotation.IdRes;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;

import com.appunite.rx.android.internal.MainThreadSubscription;
import com.appunite.rx.android.internal.Preconditions;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class RxPopupMore {
    @Nonnull
    public static Observable<PopupMenuEvent> popupMenuClick(@Nonnull PopupMenu popupMenu) {
        return Observable.create(new OnSubscribePopupMenuClick(popupMenu));
    }

    @Nonnull
    public static Func1<PopupMenuEvent, Boolean> filterMenuItem(@IdRes final int menuId) {
        return new Func1<PopupMenuEvent, Boolean>() {
            @Override
            public Boolean call(PopupMenuEvent popupMenuEvent) {
                return popupMenuEvent.menuItem().getItemId() == menuId;
            }
        };
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
}
