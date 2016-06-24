package com.appunite.rx.android.adapter.utils;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

public class BaseViewHolderManager<T extends BaseAdapterItem> implements ViewHolderManager {

    public interface ViewHolderFactory<TT extends BaseAdapterItem> {
        BaseViewHolder<TT> createViewHolder(@NonNull View view);
    }

    @NonNull
    private final Class<T> clazz;
    @LayoutRes
    private final int mLayoutRes;
    @NonNull
    private final ViewHolderFactory mViewHolderFactory;

    public BaseViewHolderManager(@LayoutRes int layoutRes, @NonNull ViewHolderFactory viewHolderFactory, @NonNull Class<T> clazz) {
        this.clazz = clazz;
        mLayoutRes = layoutRes;
        mViewHolderFactory = viewHolderFactory;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return clazz.isInstance(baseAdapterItem);
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        final View itemView = layoutInflater.inflate(mLayoutRes, viewGroup, false);
        return mViewHolderFactory.createViewHolder(itemView);
    }
}
