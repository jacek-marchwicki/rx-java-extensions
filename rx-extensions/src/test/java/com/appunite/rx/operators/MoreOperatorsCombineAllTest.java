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

import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;


@SuppressWarnings("unchecked")
public class MoreOperatorsCombineAllTest {

    @Mock
    Observer<? super ImmutableList<String>> stringListObserver;
    @Mock
    Observer<? super ImmutableList<Integer>> integerListObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCombineTwoObservables_returnsList() throws Exception {
        final ImmutableList<Observable<String>> of = ImmutableList.of(Observable.just("krowa"), Observable.just("pies"));
        final Observable<ImmutableList<String>> immutableListObservable = MoreOperators.combineAll(of);

        immutableListObservable.subscribe(stringListObserver);

        verify(stringListObserver).onNext(ImmutableList.of("krowa", "pies"));
        verify(stringListObserver).onCompleted();
        verifyNoMoreInteractions(stringListObserver);
    }

    @Test
    public void testCombineNeverObservable_neverReturns() throws Exception {

        final ImmutableList<Observable<String>> of = ImmutableList.of(Observable.just("krowa"), Observable.<String>never());
        final Observable<ImmutableList<String>> immutableListObservable = MoreOperators.combineAll(of);

        immutableListObservable.subscribe(stringListObserver);

        verifyZeroInteractions(stringListObserver);
    }

    @Test
    public void testOneObservableChange_notifyChangeBooth() throws Exception {
        final BehaviorSubject<String> first = BehaviorSubject.create("krowa");
        final ImmutableList<Observable<String>> of = ImmutableList.of(first, Observable.just("pies"));
        final Observable<ImmutableList<String>> immutableListObservable = MoreOperators.combineAll(of);
        immutableListObservable.subscribe(stringListObserver);
        verify(stringListObserver).onNext(ImmutableList.of("krowa", "pies"));
        reset(stringListObserver);

        first.onNext("kot");

        verify(stringListObserver).onNext(ImmutableList.of("kot", "pies"));
        verifyNoMoreInteractions(stringListObserver);
    }

    @Test
    public void testVeryLargeSubscriptionsSet_notifyAll() throws Exception {
        // range have to be grater than 128 to find potential an issue
        final ImmutableList<Integer> largeItemsSet = ImmutableList.copyOf(ContiguousSet.create(Range.closed(0, 1000), DiscreteDomain.integers()));
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
        final Observable<ImmutableList<Integer>> observable = MoreOperators.combineAll(of);

        observable.subscribe(integerListObserver);

        verify(integerListObserver).onNext(largeItemsSet);
        verify(integerListObserver).onCompleted();
        verifyNoMoreInteractions(integerListObserver);
    }

    @Test
    public void testSubscribeToEmpty_notifyEmpty() throws Exception {
        final Observable<ImmutableList<String>> observable = MoreOperators.combineAll(ImmutableList.<Observable<String>>of());

        observable.subscribe(stringListObserver);

        verify(stringListObserver).onNext(ImmutableList.<String>of());
        verify(stringListObserver).onCompleted();
        verifyNoMoreInteractions(stringListObserver);
    }
}
