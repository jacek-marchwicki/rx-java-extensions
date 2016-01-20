package com.appunite.rx.functions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.Observer;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.verify;

public class Functions1Test {

    private PublishSubject<CharSequence> subject;

    @Mock
    Observer<? super Boolean> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        subject = PublishSubject.create();
        subject.map(Functions1.isNullOrEmpty()).subscribe(observer);
    }

    @Test
    public void testIsNullOrEmpty_null() throws Exception {
        subject.onNext(null);

        verify(observer).onNext(true);
    }

    @Test
    public void testIsNullOrEmpty_emptyString() throws Exception {
        subject.onNext("");

        verify(observer).onNext(true);
    }

    @Test
    public void testIsNullOrEmpty_nonEmptyString() throws Exception {
        subject.onNext("Super string!");

        verify(observer).onNext(false);
    }
}