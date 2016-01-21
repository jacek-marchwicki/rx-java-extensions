package com.appunite.rx.functions;

import org.junit.Test;

import java.util.HashMap;

import rx.Observable;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assert_;

public class Functions1Test {

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
        final HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("a", 1);
        hashMap.put("b", 4);
        hashMap.put("c", 7);

       Observable.just(hashMap)
               .map(Functions1.toStringFunction())
               .subscribe(toStringFunctionResult);

        assert_().that(toStringFunctionResult.getOnNextEvents()).containsExactly("{b=4, c=7, a=1}");
    }
}