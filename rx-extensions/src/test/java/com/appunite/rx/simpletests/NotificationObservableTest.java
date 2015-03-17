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
package com.appunite.rx.simpletests;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.operators.OnSubscribeRefCountDelayed;
import com.appunite.rx.operators.OperatorCounter;
import com.google.common.base.MoreObjects;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@SuppressWarnings("unchecked")
public class NotificationObservableTest {

    @Mock
    Observer<? super User> userObserver1;
    @Mock
    Observer<? super User> userObserver2;
    @Mock
    Observer<? super Integer> mIntegerObserver;
    private Observable<User> userObservable;
    private PublishSubject<Object> incrementSubject;
    private PublishSubject<User> apiResponse;
    private TestScheduler scheduler;

    static class User {
        @Nonnull
        private final String name;
        private final long commentsCount;

        User(@Nonnull final String name, final long commentsCount) {
            this.name = name;
            this.commentsCount = commentsCount;
        }

        public static User create(@Nonnull final String name, final long commentsCount) {
            return new User(name, commentsCount);
        }

        @Nonnull
        public String name() {
            return name;
        }

        public long commentsCount() {
            return commentsCount;
        }

        public User increemtnComments(int count) {
            return new User(name, commentsCount + count);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;

            final User user = (User) o;

            return commentsCount == user.commentsCount && name.equals(user.name);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (int) (commentsCount ^ (commentsCount >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("commentsCount", commentsCount)
                    .toString();
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        scheduler = new TestScheduler();

        apiResponse = PublishSubject.create();
        incrementSubject = PublishSubject.create();

        final Observable<Integer> incrementationCounter = incrementSubject.lift(OperatorCounter.create());

        final Observable<User> userFromApiWithIncrement = apiResponse
                .flatMap(new Func1<User, Observable<User>>() {
                    @Override
                    public Observable<User> call(final User user) {

                        return Observable.combineLatest(Observable.just(user), incrementationCounter, new Func2<User, Integer, User>() {
                            @Override
                            public User call(final User user, final Integer integer) {
                                return user.increemtnComments(integer);
                            }
                        });
                    }
                });

        userObservable = OnSubscribeRefCountDelayed.create(ObservableExtensions.behavior(userFromApiWithIncrement), 5, TimeUnit.SECONDS, scheduler);
    }

    @Test
    public void testSubscribeBeforeTimeout() throws Exception {
        final Subscription subscribe = userObservable.subscribe(userObserver1);
        apiResponse.onNext(User.create("franek", 10));
        subscribe.unsubscribe();

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        userObservable.subscribe(userObserver2);
        verify(userObserver2).onNext(User.create("franek", 10));
        verifyNoMoreInteractions(userObserver2);
    }

    @Test
    public void testSubscribeAfterTimeout() throws Exception {
        final Subscription subscribe = userObservable.subscribe(userObserver1);
        apiResponse.onNext(User.create("franek", 10));
        subscribe.unsubscribe();

        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        userObservable.subscribe(userObserver2);

        verifyZeroInteractions(userObserver2);
    }

    @Test
    public void testTwoConcurrentSubscription() throws Exception {
        userObservable.subscribe(userObserver1);
        userObservable.subscribe(userObserver2);

        apiResponse.onNext(User.create("franek", 10));

        verify(userObserver1).onNext(User.create("franek", 10));
        verify(userObserver2).onNext(User.create("franek", 10));
    }

    @Test
    public void testOnSecondSubscription_returnsOnlyOnce() throws Exception {
        userObservable.subscribe(userObserver1);

        apiResponse.onNext(User.create("franek", 10));
        apiResponse.onNext(User.create("zenek", 10));


        verify(userObserver1).onNext(User.create("franek", 10));
        verify(userObserver1).onNext(User.create("zenek", 10));
        verifyNoMoreInteractions(userObserver1);

        userObservable.subscribe(userObserver2);
        verify(userObserver2).onNext(User.create("zenek", 10));
        verifyNoMoreInteractions(userObserver2);
    }

    @Test
    public void testTwoSequentSubscription() throws Exception {
        userObservable.subscribe(userObserver1).unsubscribe();
        apiResponse.onNext(User.create("zenek", 10));
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        userObservable.subscribe(userObserver2);
        apiResponse.onNext(User.create("franek", 10));

        verifyZeroInteractions(userObserver1);
        verify(userObserver2).onNext(User.create("franek", 10));
        verifyNoMoreInteractions(userObserver2);
    }

    @Test
    public void testIncrementAfterApiResponse() throws Exception {
        userObservable.subscribe(userObserver1);
        apiResponse.onNext(User.create("franek", 10));

        incrementSubject.onNext(null);

        verify(userObserver1).onNext(User.create("franek", 10));
        verify(userObserver1).onNext(User.create("franek", 11));
        verifyNoMoreInteractions(userObserver1);
    }

    @Test
    public void testIncrementBeforeSubscription() throws Exception {
        incrementSubject.onNext(null);

        userObservable.subscribe(userObserver1);
        apiResponse.onNext(User.create("franek", 10));

        verify(userObserver1).onNext(User.create("franek", 10));
        verifyNoMoreInteractions(userObserver1);
    }

    @Test
    public void testIncrementBeforeResponseAfterSubscription() throws Exception {
        userObservable.subscribe(userObserver1);
        incrementSubject.onNext(null);
        apiResponse.onNext(User.create("franek", 10));

        verify(userObserver1).onNext(User.create("franek", 10));
        verifyNoMoreInteractions(userObserver1);
    }

    @Test
    public void testApiRefresh_counterIsOverridenByApi() throws Exception {
        userObservable.subscribe(userObserver1);
        apiResponse.onNext(User.create("franek", 10));
        incrementSubject.onNext(null);
        incrementSubject.onNext(null);

        reset(userObserver1);
        apiResponse.onNext(User.create("zenek", 20));
        verify(userObserver1).onNext(User.create("zenek", 20));
        verifyNoMoreInteractions(userObserver1);
    }

}
