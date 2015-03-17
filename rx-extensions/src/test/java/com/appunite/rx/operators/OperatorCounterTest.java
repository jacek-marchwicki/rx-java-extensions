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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observer;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
public class OperatorCounterTest {

    @Mock
    Observer<? super Integer> integerObserver;
    private PublishSubject<Object> subject;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        subject = PublishSubject.create();
        subject
                .lift(new OperatorCounter())
                .subscribe(integerObserver);

    }

    @Test
    public void testAfterSubscribe_returns0() throws Exception {
        verify(integerObserver).onNext(0);
        verifyNoMoreInteractions(integerObserver);
    }

    @Test
    public void testAfterNewValue_returns1() throws Exception {
        reset(integerObserver);
        subject.onNext(null);
        verify(integerObserver).onNext(1);
        verifyNoMoreInteractions(integerObserver);
    }

    @Test
    public void testAfterTwiceNewValue_returns1And2() throws Exception {
        reset(integerObserver);
        subject.onNext(null);
        subject.onNext(null);
        verify(integerObserver).onNext(1);
        verify(integerObserver).onNext(2);
        verifyNoMoreInteractions(integerObserver);
    }
}