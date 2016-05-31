package com.appunite.rx.functions;

import org.junit.Test;

import java.util.LinkedHashMap;

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

    @Test
    public void testToStringFunction_null() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just(null)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly((String) null);
    }

    @Test
    public void testToStringFunction_string() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just("Awesome string!")
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("Awesome string!");
    }

    @Test
    public void testToStringFunction_int() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();

        Observable.just(1337)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("1337");
    }

    @Test
    public void testToStringFunction_hashMap() throws Exception {
        final TestSubscriber<String> toStringFunctionResult = new TestSubscriber<>();
        final LinkedHashMap<String, Integer> hashMap = new LinkedHashMap<>();
        hashMap.put("a", 1);
        hashMap.put("b", 4);
        hashMap.put("c", 7);

        Observable.just(hashMap)
                .map(Functions1.toStringFunction())
                .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("{a=1, b=4, c=7}");
    }
}