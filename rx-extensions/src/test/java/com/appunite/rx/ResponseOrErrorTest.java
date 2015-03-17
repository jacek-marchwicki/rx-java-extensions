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