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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import javax.annotation.Nonnull;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ChangesDetectorTest {
    private ChangesDetector<Cat, Cat> mDetector;
    private ChangesDetector.ChangesAdapter mAdapter;

    public static class Cat {
        private final int mId;
        private final String mName;

        public Cat(int id, String name) {
            mId = id;
            mName = name;
        }

        public int getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return "Cat{" +
                    "id=" + mId +
                    ", name='" + mName + '\'' +
                    '}';
        }
    }

    @Before
    public void setUp() throws Exception {
        mDetector = new ChangesDetector<>(new ChangesDetector.Detector<Cat, Cat>() {

            @Nonnull
            @Override
            public Cat apply(@Nonnull Cat item) {
                return item;
            }

            @Override
            public boolean matches(@Nonnull Cat item, @Nonnull Cat newOne) {
                return item.mId == newOne.mId;
            }

            @Override
            public boolean same(@Nonnull Cat item, @Nonnull Cat newOne) {
                return item.mId == newOne.mId && Objects.equal(item.mName, newOne.mName);
            }
        });
        mAdapter = mock(ChangesDetector.ChangesAdapter.class);
    }

    @Test
    public void testStart() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), false);
        verify(mAdapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified1() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(0, "zero")), false);
        verify(mAdapter).notifyItemMoved(1, 0);
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified2() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one"), new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(0, "zero"), new Cat(2, "two")), false);
        verify(mAdapter).notifyItemMoved(1, 0);
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified3() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one"), new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two"), new Cat(0, "zero")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemMoved(1, 0); // one, zero, two
        inOrder.verify(mAdapter).notifyItemMoved(2, 1); // one, two, zero
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrderAndModifyItem_orderAndChangeAreNotified() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(0, "zero_")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemMoved(1, 0); // one, zero
        inOrder.verify(mAdapter).notifyItemRangeChanged(1, 1); // one, zero_
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified1() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one"), new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(0, "zero")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemMoved(1, 0);
        inOrder.verify(mAdapter).notifyItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified2() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one"), new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two")), false);
        verify(mAdapter).notifyItemRangeRemoved(0, 1);
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified3() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one"), new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(2, "two"), new Cat(0, "zero")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemMoved(2, 0); // two, one, zero
        inOrder.verify(mAdapter).notifyItemRangeRemoved(1, 1); //  two, zero
        verifyNoMoreInteractions(mAdapter);
    }


    @Test
    public void testAfterInsertAtFirstPlace_firstPlaceIsMarkedAsInserted() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(0, "zero"), new Cat(1, "one")), true);
        verify(mAdapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testForce() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), true);
        verify(mAdapter).notifyItemRangeChanged(0, 1);
    }

    @Test
    public void testForce2() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two")), true);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemRangeChanged(0, 1);
        inOrder.verify(mAdapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testAddItemAtTheEnd() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two")), false);
        verify(mAdapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testAddItemAtTheBegining() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(2, "two")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two")), false);
        verify(mAdapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testAddItemInTheMiddle() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(3, "tree")), false);
        reset(mAdapter);
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two"), new Cat(3, "tree")), false);
        verify(mAdapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testItemChanged() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(3, "tree")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one1"), new Cat(3, "tree")), false);
        verify(mAdapter).notifyItemRangeChanged(0, 1);
    }

    @Test
    public void testItemDeleted1() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two"), new Cat(3, "tree")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(2, "two"), new Cat(3, "tree")), false);
        verify(mAdapter).notifyItemRangeRemoved(0, 1);
    }

    @Test
    public void testItemDeleted2() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two"), new Cat(3, "tree")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(3, "tree")), false);
        verify(mAdapter).notifyItemRangeRemoved(1, 1);
    }

    @Test
    public void testItemDeleted3() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two"), new Cat(3, "tree")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(2, "two")), false);
        verify(mAdapter).notifyItemRangeRemoved(2, 1);
    }

    @Test
    public void testItemSwapped() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(3, "tree")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(2, "two"), new Cat(3, "tree")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemRangeInserted(0, 1); // two, one, tree
        inOrder.verify(mAdapter).notifyItemRangeRemoved(1, 1); // two, tree
    }

    @Test
    public void testItemRemovedAndAdded() throws Exception {
        mDetector.newData(mAdapter, ImmutableList.of(new Cat(1, "one"), new Cat(4, "four")), false);
        reset(mAdapter);

        mDetector.newData(mAdapter, ImmutableList.of(new Cat(2, "two"), new Cat(3, "tree"), new Cat(4, "four")), false);
        final InOrder inOrder = inOrder(mAdapter);
        inOrder.verify(mAdapter).notifyItemRangeInserted(0, 1); // two, one, four
        inOrder.verify(mAdapter).notifyItemRangeInserted(1, 1); // two, tree, one, four
        inOrder.verify(mAdapter).notifyItemRangeRemoved(2, 1); // tow, tree, four
    }
}