package com.appunite.rx.functions;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assert_;

public class Functions1Test {

    @Test
    public void testIsNullOrEmpty_null() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just((String) null)
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(true);
    }

    @Test
    public void testIsNullOrEmpty_emptyString() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just("")
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(true);
    }

    @Test
    public void testIsNullOrEmpty_nonEmptyString() throws Exception {
        final TestSubscriber<Boolean> isNullOrEmptyResult = new TestSubscriber<>();

        Observable.just("Super string!")
                .map(Functions1.isNullOrEmpty())
                .subscribe(isNullOrEmptyResult);

        assert_().that(isNullOrEmptyResult.getOnNextEvents()).containsExactly(false);
    }
}