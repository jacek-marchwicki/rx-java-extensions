package com.appunite.rx.example;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;

import com.appunite.rx.android.LifecycleMainObservable;

import javax.annotation.Nonnull;

import rx.android.lifecycle.LifecycleEvent;
import rx.subjects.BehaviorSubject;

public class BaseActivity extends FragmentActivity {
    @Nonnull
    private final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();
    protected final LifecycleMainObservable lifecycleMainObservable = new LifecycleMainObservable(
            new LifecycleMainObservable.LifecycleProviderActivity(lifecycleSubject, this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lifecycleSubject.onNext(LifecycleEvent.CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        lifecycleSubject.onNext(LifecycleEvent.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(LifecycleEvent.RESUME);
    }

    @Override
    protected void onPause() {
        lifecycleSubject.onNext(LifecycleEvent.PAUSE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        lifecycleSubject.onNext(LifecycleEvent.STOP);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(LifecycleEvent.DESTROY);
        super.onDestroy();
    }
}
