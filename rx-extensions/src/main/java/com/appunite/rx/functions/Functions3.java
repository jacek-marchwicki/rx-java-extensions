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
import rx.functions.Func3;

public class Functions3 {

    @Nonnull
    public static <T1, T2, T3> Func3<T1, T2, T3, ThreeParams<T1, T2, T3>> threeParams() {
        return new Func3<T1, T2, T3, ThreeParams<T1, T2, T3>>() {
            @Override
            public ThreeParams<T1, T2, T3> call(T1 t1, T2 t2, T3 t3) {
                return new ThreeParams<>(t1, t2, t3);
            }
        };
    }
}
