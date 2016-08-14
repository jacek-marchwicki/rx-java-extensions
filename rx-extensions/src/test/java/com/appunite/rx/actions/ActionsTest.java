package com.appunite.rx.actions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import rx.functions.Action0;
import rx.functions.Action1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ActionsTest {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAll_allActionsFired() {
        Action1<Object> objectAction1 = (Action1<Object>) mock(Action1.class);
        Action1<Number> numberAction1 = (Action1<Number>) mock(Action1.class);
        Action1<Double> doubleAction1 = (Action1<Double>) mock(Action1.class);

        final ArgumentCaptor<Double> doubleArgumentCaptor = ArgumentCaptor.forClass(Double.class);

        Actions.all(objectAction1, numberAction1, doubleAction1)
                .call(1d);

        verify(objectAction1, times(1)).call(doubleArgumentCaptor.capture());
        verify(numberAction1, times(1)).call(doubleArgumentCaptor.capture());
        verify(doubleAction1, times(1)).call(doubleArgumentCaptor.capture());
    }

    @Test
    public void testSwitchActions_whenCoupleBooleanValues() {
        final Action0 actionWhenTrue = mock(Action0.class);
        final Action0 actionWhenFalse = mock(Action0.class);

        final Action1<? super Boolean> switchActions = Actions.switchActions(actionWhenTrue, actionWhenFalse);

        switchActions.call(true);

        verify(actionWhenTrue, times(1)).call();
        verify(actionWhenFalse, never()).call();

        reset(actionWhenTrue);
        switchActions.call(false);

        verify(actionWhenTrue, never()).call();
        verify(actionWhenFalse, times(1)).call();

        reset(actionWhenFalse);
        switchActions.call(true);
        switchActions.call(false);
        switchActions.call(true);

        verify(actionWhenTrue, times(2)).call();
        verify(actionWhenFalse, times(1)).call();
    }
}