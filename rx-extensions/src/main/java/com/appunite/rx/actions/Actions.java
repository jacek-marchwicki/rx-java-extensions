package com.appunite.rx.actions;

import javax.annotation.Nonnull;

import rx.functions.Action0;
import rx.functions.Action1;

public class Actions {

    /**
     * Runs all actions simultaneously, firing their <code>call</code> methods one by one.
     *
     * @param actions actions to run
     * @param <T>     common type of given actions
     * @return an action
     */
    @SafeVarargs
    @Nonnull
    public static <T> Action1<T> all(@Nonnull final Action1<? super T>... actions) {
        return new Action1<T>() {
            @Override
            public void call(final T t) {
                for (final Action1<? super T> action : actions) {
                    action.call(t);
                }
            }
        };
    }

    /**
     * Switches between actions according to emitted boolean value.
     *
     * @param actionWhenTrue  firing its <code>call</code> method when boolean value is true
     * @param actionWhenFalse firing its <code>call</code> method when boolean value is false
     * @return an action
     */
    @Nonnull
    public static Action1<? super Boolean> switchActions(@Nonnull final Action0 actionWhenTrue,
                                                         @Nonnull final Action0 actionWhenFalse) {
        return new Action1<Boolean>() {
            @Override
            public void call(final Boolean condition) {
                if (condition) {
                    actionWhenTrue.call();
                } else {
                    actionWhenFalse.call();
                }
            }
        };
    }
}
