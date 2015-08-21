package com.appunite.rx.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.MoreViewActions;
import com.appunite.rx.android.MoreViewObservables;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.CreatePostPresenter;
import com.appunite.rx.functions.Functions1;

import java.util.Objects;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.client.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewActions;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

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


        MoreViewObservables.navigationClick(toolbar)
                .compose(lifecycleMainObservable.bindLifecycle())
                .subscribe(presenter.navigationClickObserver());

        ViewObservable.clicks(acceptButton)
                .compose(lifecycleMainObservable.bindLifecycle())
                .subscribe(presenter.sendObservable());

        WidgetObservable.text(bodyText)
                .map(new OnTextChangeAction())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(presenter.bodyObservable());

        WidgetObservable.text(nameText)
                .map(new OnTextChangeAction())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(presenter.nameObservable());

        presenter.finishActivityObservable()
                .compose(lifecycleMainObservable.bindLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object response) {
                        finish();
                    }
                });

        presenter.postErrorObservable()
                .compose(lifecycleMainObservable.<Throwable>bindLifecycle())
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), R.string.create_post_error_message, Toast.LENGTH_SHORT).show();
                    }
                });

        presenter.progressObservable()
                .compose(lifecycleMainObservable.<Boolean>bindLifecycle())
                .subscribe(ViewActions.setVisibility(progress, View.INVISIBLE));

        presenter.showBodyIsEmptyErrorObservable()
                .compose(lifecycleMainObservable.bindLifecycle())
                .map(Functions1.returnJust(getString(R.string.create_post_empty_body_error)))
                .subscribe(MoreViewActions.showError(bodyText));

        presenter.showNameIsEmptyErrorObservable()
                .compose(lifecycleMainObservable.bindLifecycle())
                .map(Functions1.returnJust(getString(R.string.create_post_empty_name_error)))
                .subscribe(MoreViewActions.showError(nameText));
    }

    private static class OnTextChangeAction implements Func1<OnTextChangeEvent, String> {
        @Override
        public String call(OnTextChangeEvent onTextChangeEvent) {
            return onTextChangeEvent.text().toString();
        }
    }
}
