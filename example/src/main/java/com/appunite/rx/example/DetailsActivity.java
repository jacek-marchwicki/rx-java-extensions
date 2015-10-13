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
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.DetailsPresenters;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DetailsActivity extends BaseActivity {

    private static final String EXTRA_ID = "EXTRA_ID";

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
        final DetailsPresenters detailsPresenters = new DetailsPresenters(MyAndroidSchedulers.mainThread(),
                FakeDagger.getPostsDaoInstance(getApplicationContext()));


        final DetailsPresenters.DetailsPresenter presenter = detailsPresenters
                .getPresenter(id);

        presenter.titleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(MoreViewActions.title(toolbar));

        presenter.bodyObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(body));

        presenter.progressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progress, View.INVISIBLE));

        presenter.errorObservable()
                .map(ErrorHelper.mapThrowableToStringError())
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(error));


        ActivityCompat.postponeEnterTransition(this);
        presenter.startPostponedEnterTransitionObservable()
                .compose(bindToLifecycle())
                .subscribe(MoreActivityActions.startPostponedEnterTransition(this));
    }

}
