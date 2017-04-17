package com.appunite.rx.android;

import android.support.v7.widget.RecyclerView;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.functions.Action0;

public class AdapterChangesObservable {

    public abstract static class AdapterEvent<T extends RecyclerView.Adapter<?>> {
        @Nonnull
        private final T adapter;

        public AdapterEvent(@Nonnull T adapter) {
            this.adapter = adapter;
        }


        @SuppressWarnings("unused")
        @Nonnull
        public T getAdapter() {
            return adapter;
        }
    }

    public static class AdapterChangeEvent<T extends RecyclerView.Adapter<?>> extends AdapterEvent<T> {
        public AdapterChangeEvent(@Nonnull T adapter) {
            super(adapter);
        }
    }

    public abstract static class AdapterRangeEvent<T extends RecyclerView.Adapter<?>> extends AdapterEvent<T> {
        private final int positionStart;
        private final int itemCount;

        public AdapterRangeEvent(@Nonnull T adapter,
                                      int positionStart,
                                      int itemCount) {
            super(adapter);
            this.positionStart = positionStart;
            this.itemCount = itemCount;
        }

        @SuppressWarnings("unused")
        public int getPositionStart() {
            return positionStart;
        }

        @SuppressWarnings("unused")
        public int getItemCount() {
            return itemCount;
        }
    }

    public static class AdapterItemRangeChangeEvent<T extends RecyclerView.Adapter<?>> extends AdapterRangeEvent<T> {

        public AdapterItemRangeChangeEvent(@Nonnull T adapter,
                                      int positionStart,
                                      int itemCount) {
            super(adapter, positionStart, itemCount);
        }
    }

    public static class AdapterItemRangeInsertedEvent<T extends RecyclerView.Adapter<?>> extends AdapterRangeEvent<T> {

        public AdapterItemRangeInsertedEvent(@Nonnull T adapter,
                                             int positionStart,
                                             int itemCount) {
            super(adapter, positionStart, itemCount);
        }
    }

    public static class AdapterItemRangeRemovedEvent<T extends RecyclerView.Adapter<?>> extends AdapterRangeEvent<T> {

        public AdapterItemRangeRemovedEvent(@Nonnull T adapter,
                                           int positionStart,
                                           int itemCount) {
            super(adapter, positionStart, itemCount);
        }
    }

    public static class AdapterItemRangeMovedEvent<T extends RecyclerView.Adapter<?>> extends AdapterEvent<T> {

        private final int fromPosition;
        private final int toPosition;
        private final int itemCount;

        public AdapterItemRangeMovedEvent(@Nonnull T adapter,
                                          int fromPosition,
                                          int toPosition,
                                          int itemCount) {
            super(adapter);
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
            this.itemCount = itemCount;
        }

        @SuppressWarnings("unused")
        public int getFromPosition() {
            return fromPosition;
        }

        @SuppressWarnings("unused")
        public int getToPosition() {
            return toPosition;
        }

        @SuppressWarnings("unused")
        public int getItemCount() {
            return itemCount;
        }
    }

    @Nonnull
    public static <T extends RecyclerView.Adapter<?>> Observable<AdapterEvent<T>> onAdapterChanged(@Nonnull T adapter) {
        return Observable.create(new OnSubscribeAdapterChange<>(adapter));
    }

    public static class OnSubscribeAdapterChange<T extends RecyclerView.Adapter<?>>
            implements Observable.OnSubscribe<AdapterEvent<T>> {

        private T adapter;

        public OnSubscribeAdapterChange(@Nonnull T adapter) {

            this.adapter = adapter;
        }

        @Override
        public void call(final Subscriber<? super AdapterEvent<T>> subscriber) {
            Assertions.assertUiThread();
            final RecyclerView.AdapterDataObserver listener = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    subscriber.onNext(new AdapterChangeEvent<>(adapter));
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    subscriber.onNext(new AdapterItemRangeChangeEvent<>(adapter, positionStart, itemCount));
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    subscriber.onNext(new AdapterItemRangeInsertedEvent<>(adapter, positionStart, itemCount));
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    subscriber.onNext(new AdapterItemRangeRemovedEvent<>(adapter, positionStart, itemCount));
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    subscriber.onNext(new AdapterItemRangeMovedEvent<>(adapter, fromPosition, toPosition, itemCount));
                }
            };

            final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                @Override
                public void call() {
                    adapter.unregisterAdapterDataObserver(listener);
                }
            });
            adapter.registerAdapterDataObserver(listener);
            subscriber.add(subscription);
        }
    }

}
