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

import static org.mockito.Mockito.verify;

public class OperatorSumTes {

    private PublishSubject<Long> subject;
    @Mock
    Observer<? super Long> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        subject = PublishSubject.create();
        subject.lift(OperatorSum.create()).subscribe(observer);
    }

    @Test
    public void testAtStart_sumIsZero() throws Exception {
        verify(observer).onNext(0L);
    }

    @Test
    public void testAfter10_valueIs10() throws Exception {
        subject.onNext(10L);

        verify(observer).onNext(10L);
    }

    @Test
    public void testAfterAdding2And3_valueIs5() throws Exception {
        subject.onNext(2L);
        subject.onNext(3L);

        verify(observer).onNext(5L);
    }
}