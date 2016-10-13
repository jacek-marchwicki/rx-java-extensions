package com.appunite.rx.example.ui.posts.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.android.widget.RxActivityMore;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.example.R;
import com.appunite.rx.example.application.dagger.FakeDagger;
import com.appunite.rx.example.ui.internal.BaseActivity;
import com.appunite.rx.example.ui.internal.ErrorHelper;
import com.appunite.rx.functions.Functions1;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import javax.annotation.Nonnull;

import rx.functions.Action1;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import static com.google.common.base.Preconditions.checkNotNull;


public class CreatePostActivity extends BaseActivity {

    @Nonnull
    private final SerialSubscription subscription = new SerialSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);

        final TextView bodyText = checkNotNull((TextView) findViewById(R.id.create_post_body_text));
        final TextView nameText = checkNotNull((TextView) findViewById(R.id.create_post_name_text));

        // Normally use dagger
        final CreatePostPresenter presenter = new CreatePostPresenter(
                FakeDagger.getPostsDaoInstance(getApplicationContext()),
                MyAndroidSchedulers.mainThread());

        subscription.set(Subscriptions.from(
                RxToolbarMore.navigationClick(checkNotNull((Toolbar) findViewById(R.id.create_post_toolbar)))
                        .subscribe(presenter.navigationClickObserver()),
                RxView.clicks(checkNotNull(findViewById(R.id.accept_button)))
                        .subscribe(presenter.sendObservable()),
                RxTextView.textChanges(bodyText)
                        .subscribe(presenter.bodyObservable()),
                RxTextView.textChanges(nameText)
                        .subscribe(presenter.nameObservable()),
                presenter.finishActivityObservable()
                        .subscribe(RxActivityMore.finish(this)),
                presenter.postErrorObservable()
                        .map(ErrorHelper.mapThrowableToStringError())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String error) {
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        }),
                presenter.progressObservable()
                        .subscribe(RxView.visibility(checkNotNull(findViewById(R.id.create_post_loading_frame)), View.INVISIBLE)),
                presenter.showBodyIsEmptyErrorObservable()
                        .map(Functions1.returnJust(getString(R.string.create_post_empty_body_error)))
                        .subscribe(showError(bodyText)),
                presenter.showNameIsEmptyErrorObservable()
                        .map(Functions1.returnJust(getString(R.string.create_post_empty_name_error)))
                        .subscribe(showError(nameText))
        ));
    }

    @Override
    protected void onDestroy() {
        subscription.set(Subscriptions.empty());
        super.onDestroy();
    }

    @Nonnull
    private static Action1<? super CharSequence> showError(@Nonnull final TextView editText) {
        return new Action1<CharSequence>() {
            @Override
            public void call(CharSequence error) {
                editText.setError(error);
            }
        };
    }

    @Nonnull
    public static Intent newIntent(@Nonnull Context context) {
        return new Intent(context, CreatePostActivity.class);
    }

}
