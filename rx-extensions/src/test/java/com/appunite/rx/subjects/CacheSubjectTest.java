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

package com.appunite.rx.subjects;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import rx.Observer;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CacheSubjectTest {

    @Mock
    Observer<String> observer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubscribeToEmptyCache_doNotCallNext() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));

        subject.subscribe(observer);

        verify(observer, never()).onNext(anyString());
    }

    @Test
    public void testSubscribeToEmptyCacheAndSkipEmpty_doNotCallOnNext() throws Exception {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null), true);

        subject.subscribe(subscriber);

        assert_().that(subscriber.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testSubscribeToEmptyCacheAndDoNotSkipEmpty_callOnNext() throws Exception {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null), false);

        subject.subscribe(subscriber);

        assert_().that(subscriber.getOnNextEvents()).containsExactly((String) null);
    }

    @Test
    public void testSubscribeFullCache_callOnNext() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<>("krowa"));

        subject.subscribe(observer);

        verify(observer).onNext("krowa");
    }

    @Test
    public void testOnNextOnSubject_storeData() throws Exception {
        final CacheSubject.InMemoryCache<String> cacheCreator = new CacheSubject.InMemoryCache<>(null);
        final CacheSubject<String> subject = CacheSubject.create(cacheCreator);

        subject.onNext("krowa");

        assert_().that(cacheCreator.readFromCache()).isEqualTo("krowa");
    }

    @Test
    public void testCallOnNextTwice_lastIsWritten() throws Exception {
        final CacheSubject.InMemoryCache<String> cacheCreator = new CacheSubject.InMemoryCache<>(null);
        final CacheSubject<String> subject = CacheSubject.create(cacheCreator);

        subject.onNext("krowa1");
        subject.onNext("krowa2");

        assert_().that(cacheCreator.readFromCache()).isEqualTo("krowa2");
    }

    @Test
    public void testOnNextOnSubject_callObserver() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));
        subject.subscribe(observer);

        subject.onNext("krowa");

        verify(observer).onNext("krowa");
    }

    @Test
    public void testOnNextNull_clearCache() throws Exception {
        final CacheSubject.InMemoryCache<String> cacheCreator = new CacheSubject.InMemoryCache<>(null);
        final CacheSubject<String> subject = CacheSubject.create(cacheCreator);
        subject.subscribe(observer);

        subject.onNext(null);

        assert_().that(cacheCreator.readFromCache()).isNull();
    }

    @Test
    public void testOnNextNull_doNotCallObserver() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<>("krowa"));
        subject.subscribe(observer);

        subject.onNext(null);

        verify(observer, never()).onNext(null);
    }

    @Test
    public void testOnError_callOnError() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));
        subject.subscribe(observer);

        final IOException exception = new IOException();
        subject.onError(exception);

        verify(observer).onError(exception);
    }

    @Test
    public void testOnCompleted_callOnCompleted() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));
        subject.subscribe(observer);

        subject.onCompleted();

        verify(observer).onCompleted();
    }

    @Test
    public void testWhenNoObservers_hasSubscribersReturnsFalse() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));

        assert_().that(subject.hasObservers()).isFalse();
    }

    @Test
    public void testWhenSubscribed_hasSubscribersReturnsTrue() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));
        subject.subscribe(observer);

        assert_().that(subject.hasObservers()).isTrue();
    }

    @Test
    public void testWhenUnsubscribe_hasSubscribersReturnsFalse() throws Exception {
        final CacheSubject<String> subject = CacheSubject.create(new CacheSubject.InMemoryCache<String>(null));

        subject.subscribe(observer).unsubscribe();

        assert_().that(subject.hasObservers()).isFalse();
    }
}
