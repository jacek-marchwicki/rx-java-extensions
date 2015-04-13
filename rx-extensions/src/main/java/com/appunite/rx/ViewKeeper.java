package com.appunite.rx;

import javax.annotation.Nonnull;

import static com.appunite.rx.internal.Preconditions.checkNotNull;
import static com.appunite.rx.internal.Preconditions.checkState;

public class ViewKeeper {

    @Nonnull
    private Object view;

    public ViewKeeper(@Nonnull Object view) {
        this.view = checkNotNull(view);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T getView(Class<T> type) {
        checkState(type.isInstance(view));
        return (T) view;
    }
}
