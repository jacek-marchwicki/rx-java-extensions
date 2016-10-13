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

package com.appunite.rx.example.ui.internal;

import rx.functions.Func1;

public class ErrorHelper {
    public static Func1<Throwable, String> mapThrowableToStringError() {
        return new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                if (throwable == null) {
                    return null;
                }
                return "Some error: " + throwable.getMessage();
            }
        };
    }
}
