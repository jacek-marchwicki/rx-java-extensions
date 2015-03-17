package com.appunite.rx.example;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.appunite.rx.android.LifecycleMainObservable;
import com.appunite.rx.android.MoreViewActions;
import com.appunite.rx.example.model.presenter.MainPresenter;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewActions;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends ActionBarActivity {

    @Nonnull
    private final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();
    private final MainPresenter presenter = new MainPresenter(Schedulers.io(), AndroidSchedulers.mainThread());
    private final LifecycleMainObservable lifecycleMainObservable = new LifecycleMainObservable(
            new LifecycleMainObservable.LifecycleProviderActivity(lifecycleSubject, this));

    @InjectView(R.id.main_activity_title)
    TextView titleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(LifecycleEvent.CREATE);
        setContentView(R.layout.main_activity);

        ButterKnife.inject(this);

        lifecycleMainObservable.bindLifecycle(presenter.titleObservable())
                .subscribe(ViewActions.setText(titleTextView));
        lifecycleMainObservable.bindLifecycle(presenter.titleAlpha())
                .subscribe(MoreViewActions.setAlpha(titleTextView));
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
