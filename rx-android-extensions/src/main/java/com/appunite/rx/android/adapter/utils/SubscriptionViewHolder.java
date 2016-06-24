package com.appunite.rx.android.adapter.utils;

import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

import rx.Subscription;

public abstract class SubscriptionViewHolder<T extends BaseAdapterItem> extends ViewHolderManager.BaseViewHolder<T> {

    private Subscription subscription;

    public SubscriptionViewHolder(@Nonnull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@Nonnull T t) {
        unsubscribe();
        subscription = bindItem(t);
    }

    protected abstract Subscription bindItem(T t);

    @Override
    public void onViewRecycled() {
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}