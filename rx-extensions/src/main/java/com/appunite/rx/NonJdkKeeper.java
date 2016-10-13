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

package com.appunite.rx;

import javax.annotation.Nonnull;

import static com.appunite.rx.internal.Preconditions.checkNotNull;
import static com.appunite.rx.internal.Preconditions.checkState;

public class NonJdkKeeper {

    @Nonnull
    private Object element;

    public NonJdkKeeper(@Nonnull Object element) {
        this.element = checkNotNull(element);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> T element(Class<T> type) {
        checkState(type.isInstance(element));
        return (T) element;
    }
}
