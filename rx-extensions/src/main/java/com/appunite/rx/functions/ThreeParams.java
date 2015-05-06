package com.appunite.rx.functions;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class ThreeParams<T1, T2, T3> extends BoothParams<T1, T2> {
    private final T3 param3;

    public ThreeParams(T1 param1, T2 param2, T3 param3) {
        super(param1, param2);
        this.param3 = param3;
    }

    public T3 param3() {
        return param3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreeParams)) return false;
        if (!super.equals(o)) return false;
        ThreeParams<?, ?, ?> that = (ThreeParams<?, ?, ?>) o;
        return Objects.equal(param3, that.param3);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), param3);
    }

    @Nonnull
    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("param3", param3);
    }
}
