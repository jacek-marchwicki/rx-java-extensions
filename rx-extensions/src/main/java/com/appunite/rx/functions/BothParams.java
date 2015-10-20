package com.appunite.rx.functions;

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

        return !(param1 != null ? !param1.equals(that.param1) : that.param1 != null)
                && !(param2 != null ? !param2.equals(that.param2) : that.param2 != null);

    }

    @Override
    public int hashCode() {
        int result = param1 != null ? param1.hashCode() : 0;
        result = 31 * result + (param2 != null ? param2.hashCode() : 0);
        return result;
    }
}
