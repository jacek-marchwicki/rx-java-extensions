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

package com.appunite.detector;

import java.lang.Override;import javax.annotation.Nonnull;


/**
 * Use: com.jacekmarchwicki.changesdetector.SimpleDetector from https://github.com/jacek-marchwicki/recyclerview-changes-detector
 */
@Deprecated
public class SimpleDetector<T extends SimpleDetector.Detectable<T>> implements ChangesDetector.Detector<T, T> {

    /**
     * Use: com.jacekmarchwicki.changesdetector.SimpleDetector#Detectable from https://github.com/jacek-marchwicki/recyclerview-changes-detector
     */
    @Deprecated
    public interface Detectable<T> {

        /**
         * If booth items are the same but can have different content
         *
         * <p>Usually it means booth items has same id</p>
         * @param item to compare
         * @return true if booth items matches
         */
        boolean matches(@Nonnull T item);

        /**
         * If booth items has exactly same content
         *
         * <p>Usually it means booth items has same id, name and other fields</p>
         * <p>If you implemented {@link Object#equals(Object)} you can call
         * {@code this.equals(item)}</p>
         * @param item to compare
         * @return true if booth items are the same
         */
        boolean same(@Nonnull T item);
    }

    /**
     * Create {@link SimpleDetector}
     */
    public SimpleDetector() {
    }

    @Nonnull
    @Override
    public T apply(@Nonnull T item) {
        return item;
    }

    @Override
    public boolean matches(@Nonnull T item, @Nonnull T newOne) {
        return item.matches(newOne);
    }

    @Override
    public boolean same(@Nonnull T item, @Nonnull T newOne) {
        return item.same(newOne);
    }
}
