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

package com.appunite.rx.operators;

import com.google.common.base.Function;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.internal.util.RxRingBuffer;
import rx.subjects.BehaviorSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;


@SuppressWarnings("unchecked")
public class MoreOperatorsCombineAllTest {

    @Mock
    Observer<? super List<String>> stringListObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCombineTwoObservables_returnsList() throws Exception {
        final ImmutableList<Observable<String>> of = ImmutableList.of(Observable.just("krowa"), Observable.just("pies"));
        final Observable<List<String>> listObservable = MoreOperators.newCombineAll(of);

        listObservable.subscribe(stringListObserver);

        verify(stringListObserver).onNext(ImmutableList.of("krowa", "pies"));
        verify(stringListObserver).onCompleted();
        verifyNoMoreInteractions(stringListObserver);
    }

    @Test
    public void testCombineNeverObservable_neverReturns() throws Exception {

        final ImmutableList<Observable<String>> of = ImmutableList.of(Observable.just("krowa"), Observable.<String>never());
        final Observable<List<String>> listObservable = MoreOperators.newCombineAll(of);

        listObservable.subscribe(stringListObserver);

        verifyZeroInteractions(stringListObserver);
    }

    @Test
    public void testOneObservableChange_notifyChangeBooth() throws Exception {
        final BehaviorSubject<String> first = BehaviorSubject.create("krowa");
        final ImmutableList<Observable<String>> of = ImmutableList.of(first, Observable.just("pies"));
        final Observable<List<String>> listObservable = MoreOperators.newCombineAll(of);
        listObservable.subscribe(stringListObserver);
        verify(stringListObserver).onNext(ImmutableList.of("krowa", "pies"));
        reset(stringListObserver);

        first.onNext("kot");

        verify(stringListObserver).onNext(ImmutableList.of("kot", "pies"));
        verifyNoMoreInteractions(stringListObserver);
    }

    @Test
    public void testVeryLargeSubscriptionsSet_notifyAll() throws Exception {
        // range have to be grater than 128 to find potential an issue
        checkForSize(1000);
        checkForSize(2 * RxRingBuffer.SIZE);
        checkForSize(2 * RxRingBuffer.SIZE+1);
        checkForSize(2 * RxRingBuffer.SIZE-1);
        checkForSize(RxRingBuffer.SIZE * RxRingBuffer.SIZE);
        checkForSize(RxRingBuffer.SIZE * RxRingBuffer.SIZE+1);
        checkForSize(RxRingBuffer.SIZE * RxRingBuffer.SIZE-1);
    }

    private void checkForSize(int size) {
        final ContiguousSet<Integer> elements = ContiguousSet.create(Range.closed(0, size), DiscreteDomain.integers());
        final Observer<? super List<Integer>> integerListObserver = mock(Observer.class);

        // range have to be grater than 128 to find potential an issue
        final ImmutableList<Integer> largeItemsSet = ImmutableList.copyOf(elements);
        final ImmutableList<Observable<Integer>> of = FluentIterable
                .from(largeItemsSet)
                .transform(new Function<Integer, Observable<Integer>>() {
                    @Nullable
                    @Override
                    public Observable<Integer> apply(@Nullable final Integer input) {
                        return Observable.just(input);
                    }
                })
                .toList();
        final Observable<List<Integer>> observable = MoreOperators.newCombineAll(of);

        observable.subscribe(integerListObserver);

        verify(integerListObserver).onNext(largeItemsSet);
        verify(integerListObserver).onCompleted();
        verifyNoMoreInteractions(integerListObserver);
    }

    @Test
    public void testSubscribeToEmpty_notifyEmpty() throws Exception {
        final Observable<List<String>> observable = MoreOperators.newCombineAll(ImmutableList.<Observable<String>>of());

        observable.subscribe(stringListObserver);

        verify(stringListObserver).onNext(ImmutableList.<String>of());
        verify(stringListObserver).onCompleted();
        verifyNoMoreInteractions(stringListObserver);
    }
}
