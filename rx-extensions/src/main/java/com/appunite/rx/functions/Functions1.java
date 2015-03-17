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
    public static Func1<? super Object, ?> toObject() {
        return new Func1<Object, Object>() {
            @Override
            public Object call(final Object o) {
                return new Object();
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

}
