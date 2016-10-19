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
    private ChangesDetector<Cat, Cat> detector;
    private ChangesDetector.ChangesAdapter adapter;

    private static class Cat {
        private final int id;
        private final String name;

        private Cat(int id, String name) {
            this.id = id;
            this.name = name;
        }

        Cat(int id) {
            this.id = id;
            name = generateName(id);
        }

        Cat withName(String name) {
            return new Cat(id, name);
        }

        @Nonnull
        private String generateName(int id) {
            switch (id) {
                case 0:
                    return "zero";
                case 1:
                    return "one";
                case 2:
                    return "two";
                case 3:
                    return "tree";
                case 4:
                    return "four";
                case 5:
                    return "five";
                default:
                    throw new RuntimeException("Unknown id: " + id);
            }
        }

        @Override
        public String toString() {
            return "Cat{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @Before
    public void setUp() throws Exception {
        detector = new ChangesDetector<>(new ChangesDetector.Detector<Cat, Cat>() {

            @Nonnull
            @Override
            public Cat apply(@Nonnull Cat item) {
                return item;
            }

            @Override
            public boolean matches(@Nonnull Cat item, @Nonnull Cat newOne) {
                return item.id == newOne.id;
            }

            @Override
            public boolean same(@Nonnull Cat item, @Nonnull Cat newOne) {
                return item.id == newOne.id && Objects.equal(item.name, newOne.name);
            }
        });
        adapter = mock(ChangesDetector.ChangesAdapter.class);
    }

    @Test
    public void testStart() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        verify(adapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified1() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(0)), false);
        verify(adapter).notifyItemMoved(1, 0);
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified2() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(0), new Cat(2)), false);
        verify(adapter).notifyItemMoved(1, 0);
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrder_orderIsChangeIsNotified3() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(0)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemMoved(1, 0); // 1, 0, 2
        inOrder.verify(adapter).notifyItemMoved(2, 1); // 1, 2, 0
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrderAndModifyItem_orderAndChangeAreNotified() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(0).withName("zero_")), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemMoved(1, 0); // one, zero
        inOrder.verify(adapter).notifyItemRangeChanged(1, 1); // one, zero_
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified1() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(0)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemMoved(1, 0);
        inOrder.verify(adapter).notifyItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified2() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), false);
        verify(adapter).notifyItemRangeRemoved(0, 1);
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified3() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(2), new Cat(0)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemMoved(2, 0); // 2, 0, 1
        inOrder.verify(adapter).notifyItemRangeRemoved(2, 1); //  2, 0
        verifyNoMoreInteractions(adapter);
    }

    @Test
    public void testAfterChangeOrderAndDeleteSomeItem_orderAndDeleteAreNotified4() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1), new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(2), new Cat(1), new Cat(0)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemMoved(2, 0); // 2, 0, 1
        inOrder.verify(adapter).notifyItemMoved(2, 1); // 2, 1, 0
        verifyNoMoreInteractions(adapter);
    }


    @Test
    public void testAfterInsertAtFirstPlace_firstPlaceIsMarkedAsInserted() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(0), new Cat(1)), true);
        verify(adapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testForce() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1)), true);
        verify(adapter).notifyItemRangeChanged(0, 1);
    }

    @Test
    public void testForce2() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), true);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemRangeChanged(0, 1);
        inOrder.verify(adapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testAddItemAtTheEnd() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), false);
        verify(adapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testAddTwoItemsAtTheEnd() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        verify(adapter).notifyItemRangeInserted(1, 2);
    }

    @Test
    public void testAddItemAtTheBegining() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(2)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), false);
        verify(adapter).notifyItemRangeInserted(0, 1);
    }

    @Test
    public void testAddTwoItemsAtTheBegining() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(3)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        verify(adapter).notifyItemRangeInserted(0, 2);
    }

    @Test
    public void testAddItemInTheMiddle() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(3)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        verify(adapter).notifyItemRangeInserted(1, 1);
    }

    @Test
    public void testAddTwoItemsInTheMiddle() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(4)), false);
        reset(adapter);
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3), new Cat(4)), false);
        verify(adapter).notifyItemRangeInserted(1, 2);
    }

    @Test
    public void testItemChanged() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1).withName("one1"), new Cat(3)), false);
        verify(adapter).notifyItemRangeChanged(0, 1);
    }

    @Test
    public void testTwoItemsChangedAtStart() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1).withName("one1"), new Cat(2).withName("two1"), new Cat(3)), false);
        verify(adapter).notifyItemRangeChanged(0, 2);
    }

    @Test
    public void testTwoItemsChangedInMiddle() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3), new Cat(4)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2).withName("two1"), new Cat(3).withName("tree1"), new Cat(4)), false);
        verify(adapter).notifyItemRangeChanged(1, 2);
    }

    @Test
    public void testTwoItemsChangedAtTheEnd() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2).withName("two1"), new Cat(3).withName("tree1")), false);
        verify(adapter).notifyItemRangeChanged(1, 2);
    }

    @Test
    public void testItemDeleted1() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(2), new Cat(3)), false);
        verify(adapter).notifyItemRangeRemoved(0, 1);
    }

    @Test
    public void testItemDeleted2() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(3)), false);
        verify(adapter).notifyItemRangeRemoved(1, 1);
    }

    @Test
    public void testItemDeleted4() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3), new Cat(4)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(3), new Cat(4)), false);
        verify(adapter).notifyItemRangeRemoved(0, 2);
    }

    @Test
    public void testItemDeleted5() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3), new Cat(4)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(4)), false);
        verify(adapter).notifyItemRangeRemoved(1, 2);
    }

    @Test
    public void testItemDeleted6() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3), new Cat(4)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), false);
        verify(adapter).notifyItemRangeRemoved(2, 2);
    }

    @Test
    public void testItemDeleted3() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2)), false);
        verify(adapter).notifyItemRangeRemoved(2, 1);
    }

    @Test
    public void testItemSwapped() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(3)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(2), new Cat(3)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemRangeInserted(0, 1); // 2, 1, 3
        inOrder.verify(adapter).notifyItemRangeRemoved(1, 1); // 2, 3
    }

    @Test
    public void testItemSwappedTwo() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(2), new Cat(5)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(3), new Cat(4), new Cat(5)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemRangeInserted(0, 2); // 3, 4, 1, 2, 5
        inOrder.verify(adapter).notifyItemRangeRemoved(2, 2); // 3, 4, 5
    }

    @Test
    public void testItemRemovedAndAdded() throws Exception {
        detector.newData(adapter, ImmutableList.of(new Cat(1), new Cat(4)), false);
        reset(adapter);

        detector.newData(adapter, ImmutableList.of(new Cat(2), new Cat(3), new Cat(4)), false);
        final InOrder inOrder = inOrder(adapter);
        inOrder.verify(adapter).notifyItemRangeInserted(0, 2); // 2, 1, 4, than // 2, 3, 1, 4
        inOrder.verify(adapter).notifyItemRangeRemoved(2, 1); // 2, 3, 4
    }
}