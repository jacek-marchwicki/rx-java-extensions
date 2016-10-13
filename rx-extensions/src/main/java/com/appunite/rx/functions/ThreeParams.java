/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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

    @Override
    public String toString() {
        return "ThreeParams{" +
                "param1=" + param1() +
                ", param2=" + param2() +
                ", param3=" + param3 +
                "} ";
    }
}
