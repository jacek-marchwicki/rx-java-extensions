package com.appunite.rx.functions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import rx.Observer;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.verify;

public class Functions1Test {

    private PublishSubject<Object> subject;
    @Mock
    Observer<String> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        subject = PublishSubject.create();
        subject.map(Functions1.toStringFunction()).subscribe(observer);
    }

    @Test
    public void testToStringFunction_null() throws Exception {
        subject.onNext(null);

        verify(observer).onNext(null);
    }

    @Test
    public void testToStringFunction_string() throws Exception {
        subject.onNext("Awesome string!");

        verify(observer).onNext("Awesome string!");
    }

    @Test
    public void testToStringFunction_int() throws Exception {
        subject.onNext(1337);

        verify(observer).onNext("1337");
    }

    @Test
    public void testToStringFunction_hashMap() throws Exception {
        final Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 4);
        map.put("c", 7);

        subject.onNext(map);

        verify(observer).onNext("{b=4, c=7, a=1}");
    }
}