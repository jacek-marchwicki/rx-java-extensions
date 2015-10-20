package com.appunite.rx.functions;

import javax.annotation.Nonnull;

public class ThreeParams<T1, T2, T3> extends BothParams<T1, T2> {
    private final T3 param3;

    public ThreeParams(T1 param1, T2 param2, T3 param3) {
        super(param1, param2);
        this.param3 = param3;
    }

    @Nonnull
    public static <T1, T2, T3> ThreeParams<T1, T2, T3> of(T1 first, T2 second, T3 third) {
        return new ThreeParams<>(first, second, third);
    }

    public T3 param3() {
        return param3;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreeParams)) return false;
        //noinspection EqualsBetweenInconvertibleTypes
        if (!super.equals(o)) return false;

        ThreeParams<?, ?, ?> that = (ThreeParams<?, ?, ?>) o;

        return !(param3 != null ? !param3.equals(that.param3) : that.param3 != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (param3 != null ? param3.hashCode() : 0);
        return result;
    }
}
