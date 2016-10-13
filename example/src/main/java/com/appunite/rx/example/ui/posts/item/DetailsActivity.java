package com.appunite.rx.example.ui.posts.item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.appunite.rx.android.widget.RxActivityMore;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.example.R;
import com.appunite.rx.example.application.dagger.FakeDagger;
import com.appunite.rx.example.ui.internal.BaseActivity;
import com.appunite.rx.example.ui.internal.ErrorHelper;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import javax.annotation.Nonnull;

import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DetailsActivity extends BaseActivity {

    private static final String EXTRA_ID = "EXTRA_ID";

    public static Intent getIntent(@Nonnull Context context, @Nonnull String id) {
        return new Intent(context, DetailsActivity.class)
                .putExtra(EXTRA_ID, checkNotNull(id));
    }

    @Nonnull
    private final SerialSubscription subscription = new SerialSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        final String postId = checkNotNull(getIntent().getStringExtra(EXTRA_ID));

        // Normally use dagger
        final DetailsPresenters presenter = new DetailsPresenters(
                MyAndroidSchedulers.mainThread(),
                FakeDagger.getPostsDaoInstance(getApplicationContext()),
                postId);

        ActivityCompat.postponeEnterTransition(this);

        subscription.set(Subscriptions.from(
                presenter.titleObservable()
                        .subscribe(RxToolbarMore.title(checkNotNull((Toolbar)findViewById(R.id.details_activity_toolbar)))),
                presenter.bodyObservable()
                        .subscribe(RxTextView.text(checkNotNull((TextView)findViewById(R.id.details_activity_body)))),
                presenter.progressObservable()
                        .subscribe(RxView.visibility(checkNotNull(findViewById(R.id.details_activity_progress)), View.INVISIBLE)),
                presenter.errorObservable()
                        .map(ErrorHelper.mapThrowableToStringError())
                        .subscribe(RxTextView.text(checkNotNull((TextView)findViewById(R.id.details_activity_error)))),
                presenter.startPostponedEnterTransitionObservable()
                        .subscribe(RxActivityMore.startPostponedEnterTransition(this))
        ));
    }

    @Override
    protected void onDestroy() {
        subscription.set(Subscriptions.empty());
        super.onDestroy();
    }
}
