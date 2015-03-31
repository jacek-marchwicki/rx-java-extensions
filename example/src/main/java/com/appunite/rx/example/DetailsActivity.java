package com.appunite.rx.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.MoreActivityActions;
import com.appunite.rx.android.MoreViewActions;
import com.appunite.rx.example.model.dao.ItemsDao;
import com.appunite.rx.example.model.presenter.DetailsPresenters;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewActions;
import rx.schedulers.Schedulers;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DetailsActivity extends BaseActivity {

    public static final String EXTRA_ID = "EXTRA_ID";

    public static Intent getIntent(@Nonnull Context context, @Nonnull String id) {
        return new Intent(context, DetailsActivity.class)
                .putExtra(EXTRA_ID, checkNotNull(id));
    }

    @InjectView(R.id.details_activity_toolbar)
    Toolbar toolbar;
    @InjectView(R.id.details_activity_progress)
    View progress;
    @InjectView(R.id.details_activity_error)
    TextView error;
    @InjectView(R.id.details_activity_body)
    TextView body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        final String id = checkNotNull(getIntent().getStringExtra(EXTRA_ID));

        ButterKnife.inject(this);

        // Normally use dagger
        final DetailsPresenters.DetailsPresenter presenter = new DetailsPresenters(Schedulers.io(),
                AndroidSchedulers.mainThread(),
                ItemsDao.getInstance(Schedulers.io()))
                .getPresenter(id);

        presenter.titleObservable()
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(MoreViewActions.setTitle(toolbar));

        presenter.bodyObservable()
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(ViewActions.setText(body));

        presenter.progressObservable()
                .compose(lifecycleMainObservable.<Boolean>bindLifecycle())
                .subscribe(ViewActions.setVisibility(progress, View.INVISIBLE));

        presenter.errorObservable()
                .map(ErrorHelper.mapThrowableToStringError())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(ViewActions.setText(error));


        ActivityCompat.postponeEnterTransition(this);
        presenter.startPostponedEnterTransitionObservable()
                .compose(lifecycleMainObservable.bindLifecycle())
                .subscribe(MoreActivityActions.startPostponedEnterTransition(this));
    }

}
