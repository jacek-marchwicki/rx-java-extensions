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

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MoreOperatorsRefreshTest {

    private String returnValue;
    private Observable<String> response;
    private PublishSubject<Object> refresh;

    @Mock
    Observer<? super String> stringObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        refresh = PublishSubject.create();

        final Observable<String> apiResponse = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                subscriber.onNext(returnValue);
                subscriber.onCompleted(); // Optional
            }
        });

        response = apiResponse.compose(MoreOperators.<String>refresh(refresh));
    }

    @Test
    public void testSubscribe_onNextValue() throws Exception {
        returnValue = "test1";
        response.subscribe(stringObserver);

        verify(stringObserver).onNext("test1");
        verifyNoMoreInteractions(stringObserver);
    }

    @Test
    public void testSubscribeAndRefresh_getNewValue() throws Exception {
        returnValue = "test1";
        response.subscribe(stringObserver);
        verify(stringObserver).onNext("test1");
        reset(stringObserver);

        returnValue = "test2";
        refresh.onNext(null);

        verify(stringObserver).onNext("test2");
        verifyNoMoreInteractions(stringObserver);
    }
}
