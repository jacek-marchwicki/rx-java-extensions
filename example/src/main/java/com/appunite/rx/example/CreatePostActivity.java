package com.appunite.rx.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.widget.RxActivityMore;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.CreatePostPresenter;
import com.appunite.rx.functions.Functions1;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;


public class CreatePostActivity extends BaseActivity {

    @InjectView(R.id.create_post_toolbar)
    Toolbar toolbar;
    @InjectView(R.id.accept_button)
    ImageView acceptButton;
    @InjectView(R.id.create_post_body_text)
    EditText bodyText;
    @InjectView(R.id.create_post_name_text)
    EditText nameText;
    @InjectView(R.id.create_post_loading_frame)
    View progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);

        ButterKnife.inject(this);

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

        final CreatePostPresenter presenter = new CreatePostPresenter(
                FakeDagger.getPostsDaoInstance(getApplicationContext()));

        RxToolbarMore.navigationClick(toolbar)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.navigationClickObserver());

        RxView.clicks(acceptButton)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.sendObservable());

        RxTextView.textChanges(bodyText)
                .compose(this.<CharSequence>bindToLifecycle())
                .subscribe(presenter.bodyObservable());

        RxTextView.textChanges(nameText)
                .compose(this.<CharSequence>bindToLifecycle())
                .subscribe(presenter.nameObservable());

        presenter.finishActivityObservable()
                .compose(this.bindToLifecycle())
                .subscribe(RxActivityMore.finish(this));

        presenter.postErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), R.string.create_post_error_message, Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.progressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progress, View.INVISIBLE));

        presenter.showBodyIsEmptyErrorObservable()
                .compose(this.bindToLifecycle())
                .map(Functions1.returnJust(getString(R.string.create_post_empty_body_error)))
                .subscribe(showError(bodyText));

        presenter.showNameIsEmptyErrorObservable()
                .compose(this.bindToLifecycle())
                .map(Functions1.returnJust(getString(R.string.create_post_empty_name_error)))
                .subscribe(showError(nameText));
    }

    @Nonnull
    private Action1<? super CharSequence> showError(@Nonnull final TextView editText) {
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
