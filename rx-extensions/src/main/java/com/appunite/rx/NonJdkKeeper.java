package com.appunite.rx;

import javax.annotation.Nonnull;

import static com.appunite.rx.internal.Preconditions.checkNotNull;
import static com.appunite.rx.internal.Preconditions.checkState;

public class NonJdkKeeper {

    @Nonnull
    private Object element;

    public NonJdkKeeper(@Nonnull Object element) {
        this.element = checkNotNull(element);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T element(Class<T> type) {
        checkState(type.isInstance(element));
        return (T) element;
    }
}
