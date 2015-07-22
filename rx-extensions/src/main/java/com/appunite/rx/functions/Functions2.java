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

import rx.functions.Func2;

public class Functions2 {

    private Functions2() {
    }

    @Nonnull
    public static <T> Func2<T, Object, T> firstParam() {
        return new Func2<T, Object, T>() {
            @Override
            public T call(T first, Object second) {
                return first;
            }
        };
    }

    @Nonnull
    public static <T> Func2<Object, T, T> secondParam() {
        return new Func2<Object, T, T>() {
            @Override
            public T call(Object first, T second) {
                return second;
            }
        };
    }

    @Nonnull
    public static <T, K> Func2<T, K, BothParams<T, K>> bothParams() {
        return new Func2<T, K, BothParams<T, K>>() {
            @Override
            public BothParams<T, K> call(T t, K k) {
                return new BothParams<>(t, k);
            }
        };
    }
}
