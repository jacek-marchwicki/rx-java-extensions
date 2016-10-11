/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.functions;

import javax.annotation.Nonnull;

import rx.functions.Func1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;
import static com.appunite.rx.internal.Preconditions.checkState;

import com.appunite.rx.internal.Objects;

public class Functions1 {

    @Nonnull
    public static Func1<? super Boolean, Boolean> isTrue() {
        return new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(final Boolean aBoolean) {
                return aBoolean;
            }
        };
    }

    @Nonnull
    public static Func1<? super Boolean, Boolean> isFalse() {
        return new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean aBoolean) {
                return !aBoolean;
            }
        };
    }

    @Nonnull
    public static <T> Func1<T, Boolean> neg(@Nonnull final Func1<T, Boolean> func1) {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(final T t) {
                return !func1.call(t);
            }
        };
    }

    @Nonnull
    public static Func1<? super Object, Boolean> isNull() {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(final Object object) {
                return object == null;
            }
        };
    }

    @Nonnull
    public static Func1<? super Object, Boolean> isNotNull() {
        return neg(isNull());
    }

    @Nonnull
    public static Func1<? super CharSequence, Boolean> isNullOrEmpty() {
        return new Func1<CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence charSequence) {
                return charSequence == null || charSequence.length() == 0;
            }
        };
    }

    @Nonnull
    public static Func1<? super Object, Boolean> instanceOf(@Nonnull final Class clazz) {
        checkNotNull(clazz);
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return clazz.isInstance(o);
            }
        };
    }

    @Nonnull
    public static Func1<? super Object, ?> toObject() {
        return new Func1<Object, Object>() {
            @Override
            public Object call(final Object o) {
                return new Object();
            }
        };
    }

    @Nonnull
    public static Func1<? super Object, Void> toVoid() {
        return new Func1<Object, Void>() {
            @Override
            public Void call(final Object ignore) {
                return null;
            }
        };
    }

    @Nonnull
    public static Func1<? super Boolean, ? extends Boolean> neg() {
        return new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(final Boolean aBoolean) {
                return !aBoolean;
            }
        };
    }

    @Nonnull
    public static Func1<Object, Boolean> returnFalse() {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return false;
            }
        };
    }

    @Nonnull
    public static Func1<Object, Boolean> returnTrue() {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return true;
            }
        };
    }

    @Nonnull
    public static <T> Func1<Object, T> returnJust(final T value) {
        return new Func1<Object, T>() {
            @Override
            public T call(Object o) {
                return value;
            }
        };
    }

    /**
     * Use {@link #toStringFunction()}
     */
    @Deprecated
    @Nonnull
    public static Func1<? super CharSequence, String> charSequenceToString() {
        return toStringFunction();
    }

    /**
     * Converts propagated value to string.
     * @return propagated value as string converted by its toString() method. If propagated
     * value is null, null is returned.
     */
    @Nonnull
    public static Func1<Object, String> toStringFunction() {
        return new Func1<Object, String>() {
            @Override
            public String call(Object o) {
                if (o == null) {
                    return null;
                }
                return o.toString();
            }
        };
    }

    /**
     * Checks if propagated value is equal to any of passed values.
     *
     * @param values values which are compared to propagated value.
     * @return true if propagated value is equal to at least one of passed values, false otherwise.
     * If values length is equal to 0 or passed null reference this method returns false.
     */
    @Nonnull
    public static Func1<? super Object, Boolean> isEqualTo(@Nonnull final Object... values) {
        checkNotNull(values, "Values cannot be null");
        checkState(values.length > 0, "You need to specify at least one object.");
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(final Object o) {
                for (final Object value : values) {
                    if (Objects.equal(o, value)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
