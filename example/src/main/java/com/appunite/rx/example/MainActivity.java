package com.appunite.rx.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.LifecycleMainObservable;
import com.appunite.rx.android.MoreViewActions;
import com.appunite.rx.example.model.presenter.MainPresenter;
import com.appunite.rx.example.model.presenter.NetworkDao;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewActions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MainActivity extends FragmentActivity {

    @Nonnull
    private final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();
    private final MainPresenter presenter = new MainPresenter(Schedulers.io(), AndroidSchedulers.mainThread(), NetworkDao.getInstance(Schedulers.io()));
    private final LifecycleMainObservable lifecycleMainObservable = new LifecycleMainObservable(
            new LifecycleMainObservable.LifecycleProviderActivity(lifecycleSubject, this));

    @InjectView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @InjectView(R.id.main_activity_recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.main_activity_progress)
    View progress;
    @InjectView(R.id.main_activity_error)
    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(LifecycleEvent.CREATE);
        setContentView(R.layout.main_activity);

        final MainAdapter mainAdapter = new MainAdapter();

        ButterKnife.inject(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mainAdapter);

        presenter.titleObservable()
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(MoreViewActions.setTitle(toolbar));

        presenter.items()
                .compose(lifecycleMainObservable.<ImmutableList<MainPresenter.AdapterItem>>bindLifecycle())
                .subscribe(mainAdapter);

        presenter.progress()
                .compose(lifecycleMainObservable.<Boolean>bindLifecycle())
                .subscribe(ViewActions.setVisibility(progress, View.INVISIBLE));

        presenter.error()
                .map(mapThrowableToStringError())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(ViewActions.setText(error));
    }

    private Func1<Throwable, String> mapThrowableToStringError() {
        return new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                if (throwable == null) {
                    return null;
                }
                return "Some error: " + throwable.getMessage();
            }
        };
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
