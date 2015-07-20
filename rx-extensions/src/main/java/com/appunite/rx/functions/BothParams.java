package com.appunite.rx.functions;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class BothParams<T1, T2> {
    private final T1 param1;
    private final T2 param2;

    public BothParams(T1 param1, T2 param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    @Nonnull
    public static <T1, T2> BothParams<T1, T2> of(T1 first, T2 second) {
        return new BothParams<>(first, second);
    }

    public T1 param1() {
        return param1;
    }

    public T2 param2() {
        return param2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BothParams)) return false;
        BothParams<?, ?> that = (BothParams<?, ?>) o;
        return Objects.equal(param1, that.param1) &&
                Objects.equal(param2, that.param2);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(param1, param2);
    }

    @Override
    public String toString() {
        return toStringHelper()
                .toString();
    }

    @Nonnull
    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("param1", param1)
                .add("param2", param2);
    }
}
