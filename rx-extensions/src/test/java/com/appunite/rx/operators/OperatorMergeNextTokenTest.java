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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@SuppressWarnings("unchecked")
public class OperatorMergeNextTokenTest {

    private PublishSubject<Object> downloadNext;

    private Observable<Data> dataObservable;
    @Mock
    Observer<? super Data> dataObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        downloadNext = PublishSubject.create();


        dataObservable = downloadNext.startWith((Object) null)
                .lift(OperatorMergeNextToken.create(new Data(ImmutableList.<String>of(), 0),
                        new Func1<Data, Observable<Data>>() {
                    @Override
                    public Observable<Data> call(final Data data) {
                        if (data.nextPage < 0) {
                            return Observable.empty();
                        }
                        return Observable.just(data)
                                .zipWith(downloadData(data.nextPage), new Func2<Data, Data, Data>() {
                                    @Override
                                    public Data call(final Data data, final Data data2) {
                                        return data.mergeWith(data2);
                                    }
                                });
                    }
                }));

    }

    @Test
    public void testSubscribe_getData() throws Exception {
        dataObservable.subscribe(dataObserver);

        verify(dataObserver).onNext(new Data(ImmutableList.of("data1", "data2"), 1));
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void testLoadMore_mergeData() throws Exception {
        dataObservable.subscribe(dataObserver);
        verify(dataObserver).onNext(new Data(ImmutableList.of("data1", "data2"), 1));
        reset(dataObserver);

        downloadNext.onNext(null);

        verify(dataObserver).onNext(new Data(ImmutableList.of("data1", "data2", "data3", "data4"), 2));
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void testAfterLastMethod_noMoreData() throws Exception {
        dataObservable.subscribe(dataObserver);
        downloadNext.onNext(null);
        downloadNext.onNext(null);
        downloadNext.onNext(null);
        downloadNext.onNext(null);
        reset(dataObserver);


        downloadNext.onNext(null);
        verifyZeroInteractions(dataObserver);
    }

    @Test
    public void testAfterManyRequests_errorNotThrown() throws Exception {
        dataObservable.subscribe(dataObserver);

        downloadNext.onNext(null);
        downloadNext.onNext(null);
        downloadNext.onNext(null);
        downloadNext.onNext(null);

        verify(dataObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void testAfterError_propagateError() throws Exception {
        final IOException error = new IOException("error");
        dataObservable.subscribe(dataObserver);

        downloadNext.onError(error);

        verify(dataObserver).onError(error);
    }

    public Observable<Data> downloadData(int page) {
        switch (page) {
            case 0:
                return Observable.just(new Data(ImmutableList.of("data1", "data2"), 1));
            case 1:
                return Observable.just(new Data(ImmutableList.of("data3", "data4"), 2));
            case 2:
                return Observable.just(new Data(ImmutableList.of("data5", "data6"), -1));
            default:
                return Observable.error(new IOException("Wrong token"));
        }
    }

    private static class Data {
        @Nonnull
        private final ImmutableList<String> data;
        private final int nextPage;

        Data(@Nonnull final ImmutableList<String> data, final int nextPage) {
            this.data = data;
            this.nextPage = nextPage;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Data)) return false;

            final Data data1 = (Data) o;

            return nextPage == data1.nextPage && data.equals(data1.data);

        }

        public Data mergeWith(Data newData) {
            final ImmutableList<String> data = ImmutableList.<String>builder()
                    .addAll(this.data)
                    .addAll(newData.data)
                    .build();
            return new Data(data,
                    newData.nextPage);
        }

        @Override
        public int hashCode() {
            int result = data.hashCode();
            result = 31 * result + nextPage;
            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("data", data)
                    .add("nextPage", nextPage)
                    .toString();
        }
    }
}
