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