package com.appunite.rx.example;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.MoreViewActions;
import com.appunite.rx.example.model.dao.ItemsDao;
import com.appunite.rx.example.model.presenter.MainPresenter;
import com.google.common.collect.ImmutableList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewActions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

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
        setContentView(R.layout.main_activity);

        final MainAdapter mainAdapter = new MainAdapter();

        ButterKnife.inject(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mainAdapter);

        // Normally use dagger
        final MainPresenter presenter = new MainPresenter(Schedulers.io(),
                AndroidSchedulers.mainThread(),
                ItemsDao.getInstance(Schedulers.io()));

        presenter.titleObservable()
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(MoreViewActions.setTitle(toolbar));

        presenter.itemsObservable()
                .compose(lifecycleMainObservable.<ImmutableList<MainPresenter.AdapterItem>>bindLifecycle())
                .subscribe(mainAdapter);

        presenter.progressObservable()
                .compose(lifecycleMainObservable.<Boolean>bindLifecycle())
                .subscribe(ViewActions.setVisibility(progress, View.INVISIBLE));

        presenter.errorObservable()
                .map(mapThrowableToStringError())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(ViewActions.setText(error));

        presenter.openDetailsObservable()
                .compose(lifecycleMainObservable.<MainPresenter.AdapterItem>bindLifecycle())
                .subscribe(new Action1<MainPresenter.AdapterItem>() {
                    @Override
                    public void call(MainPresenter.AdapterItem adapterItem) {
                        ActivityCompat.startActivity(MainActivity.this,
                                DetailsActivity.getIntent(MainActivity.this, adapterItem.id()),
                                ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this)
                                        .toBundle());
                    }
                });
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

}
