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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.appunite.rx.internal.Preconditions.checkNotNull;


/**
 * Detector for adapter items that can find what was changed and call recycler adapter methods
 * @param <T> type of items to detect
 */
public class ChangesDetector<T, H> {

    /**
     * Interface that is already implemented by RecyclerView
     */
    public interface ChangesAdapter {

        void notifyItemRangeInserted(int start, int count);

        void notifyItemRangeChanged(int start, int count);

        void notifyItemRangeRemoved(int start, int count);

        void notifyItemMoved(int fromPosition, int toPosition);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public H[] mItems = (H[])new Object[0];
    @Nonnull
    public final Detector<T, H> mDetector;

    /**
     * Created {@link ChangesDetector} with {@link ChangesDetector.Detector}
     * @param detector detector
     */
    public ChangesDetector(@Nonnull Detector<T, H> detector) {
        mDetector = checkNotNull(detector);
    }

    public interface Detector<T, H> {

        @Nonnull
        H apply(@Nonnull T item);

        boolean matches(@Nonnull H item, @Nonnull H newOne);

        boolean same(@Nonnull H item, @Nonnull H newOne);
    }

    private int indexOfItem(@Nonnull List<H> list,
                            @Nonnull H search) {
        int counter = 0;
        for (H item : list) {
            if (mDetector.matches(item, search)) {
                return counter;
            }
            counter += 1;
        }
        return -1;
    }

    /**
     * Inform adapter about new data
     * @param adapter adapter to be informed about changes
     * @param values items for adapter
     * @param force true if you need to force all data reload
     */
    public void newData(@Nonnull ChangesAdapter adapter,
                        @Nonnull List<T> values,
                        boolean force) {
        checkNotNull(adapter);
        checkNotNull(values);

        final H[] list = apply(values);

        final LinkedList<H> objects = new LinkedList<>();
        Collections.addAll(objects, mItems);

        int successPosition = 0;
        for (; successPosition < list.length;) {
            final H item = list[successPosition];
            final int i = indexOfItem(objects, item);
            if (i < 0) {
                adapter.notifyItemRangeInserted(successPosition, 1);
                successPosition += 1;
            } else if (i == 0) {
                if (force || !mDetector.same(item, objects.get(0))) {
                    adapter.notifyItemRangeChanged(successPosition, 1);
                }
                objects.remove(0);
                successPosition += 1;
            } else {
                final H first = objects.get(0);
                if (existInList(list, successPosition + 1, first)) {
                    // changed order
                    adapter.notifyItemMoved(i + successPosition, successPosition);

                    if (force || !mDetector.same(item, objects.get(i))) {
                        adapter.notifyItemRangeChanged(successPosition, 1);
                    }
                    objects.remove(i);
                    successPosition += 1;
                } else {
                    adapter.notifyItemRangeRemoved(successPosition, 1);
                    objects.remove(0);
                }
            }
        }
        if (objects.size() > 0) {
            adapter.notifyItemRangeRemoved(successPosition, objects.size());
            objects.clear();
        }

        mItems = list;
    }

    private boolean existInList(H[] list, int start, H search) {
        for (int i = start, listLength = list.length; i < listLength; i++) {
            final H item = list[i];
            if (mDetector.matches(item, search)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    private H[] apply(@Nonnull List<T> values) {
        @SuppressWarnings("unchecked")
        final H[] result = (H[])new Object[values.size()];
        for (int i = 0; i < values.size(); i++) {
            T value = values.get(i);
            result[i] = mDetector.apply(value);
        }

        return result;
    }

}