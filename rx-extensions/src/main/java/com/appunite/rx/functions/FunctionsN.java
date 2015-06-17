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

import rx.functions.FuncN;

public class FunctionsN {

    private FunctionsN() {
    }

    @Nonnull
    public static FuncN<Boolean> returnTrue() {
        return new FuncN<Boolean>() {
            @Override
            public Boolean call(final Object... args) {
                return true;
            }
        };
    }

    @Nonnull
    public static FuncN<Boolean> returnFalse() {
        return new FuncN<Boolean>() {
            @Override
            public Boolean call(Object... args) {
                return false;
            }
        };
    }

    @Nonnull
    public static FuncN<Throwable> combineFirstThrowable() {
        return new FuncN<Throwable>() {
            @Override
            public Throwable call(final Object... args) {
                for (Object arg : args) {
                    final Throwable throwable = (Throwable) arg;
                    if (throwable != null) {
                        return throwable;
                    }
                }
                return null;
            }
        };
    }
}
