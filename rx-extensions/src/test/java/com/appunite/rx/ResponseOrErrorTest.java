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

package com.appunite.rx;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import rx.Observable;
import rx.Observer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ResponseOrErrorTest {

    @Mock
    Observer<? super ResponseOrError<String>> responseOrErrorObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testObservableReturnsIOException_onNextErrorResponse() throws Exception {
        final IOException exception = new IOException();
        Observable.<String>error(exception)
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .subscribe(responseOrErrorObserver);

        verify(responseOrErrorObserver).onNext(ResponseOrError.<String>fromError(exception));
        verify(responseOrErrorObserver).onCompleted();
        verifyNoMoreInteractions(responseOrErrorObserver);
    }

    @Test
    public void testObservableReturnsValid_onNextValid() throws Exception {
        Observable.just("test")
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .subscribe(responseOrErrorObserver);

        verify(responseOrErrorObserver).onNext(ResponseOrError.fromData("test"));
        verify(responseOrErrorObserver).onCompleted();
        verifyNoMoreInteractions(responseOrErrorObserver);
    }

    @Test
    public void testObservableReturnsException_onNextErrorResponse() throws Exception {
        final Exception exception = new Exception();
        Observable.<String>error(exception)
                .compose(ResponseOrError.<String>toResponseOrErrorObservable())
                .subscribe(responseOrErrorObserver);

        verify(responseOrErrorObserver).onNext(ResponseOrError.<String>fromError(exception));
        verify(responseOrErrorObserver).onCompleted();
        verifyNoMoreInteractions(responseOrErrorObserver);
    }
}
