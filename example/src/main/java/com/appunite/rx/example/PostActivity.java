package com.appunite.rx.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.MoreViewObservables;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.PostPresenter;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.client.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewActions;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class PostActivity extends BaseActivity {

    private static final String EXTRA_ID = "EXTRA_ID";

    public static Intent getIntent(@Nonnull Context context, @Nonnull String id) {
        return new Intent(context, PostActivity.class)
                .putExtra(EXTRA_ID, checkNotNull(id));
    }

    @InjectView(R.id.post_toolbar)
    Toolbar toolbar;
    @InjectView(R.id.accept_button)
    ImageView acceptButton;
    @InjectView(R.id.bodyText)
    EditText bodyText;
    @InjectView(R.id.nameText)
    EditText nameText;
    @InjectView(R.id.main_activity_error)
    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);

        ButterKnife.inject(this);

        toolbar.setNavigationIcon(R.drawable.ic_cancel_white_24dp);

        final PostPresenter postPresenter = new PostPresenter(AndroidSchedulers.mainThread(),
                FakeDagger.getPostsDaoInstance(getApplicationContext()));


        MoreViewObservables.navigationClick(toolbar)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        finish();
                    }
                });

        ViewObservable.clicks(acceptButton).map(new Func1<OnClickEvent, Object>() {
            @Override
            public Object call(OnClickEvent onClickEvent) {
                return new Object();
            }
        }).subscribe(postPresenter.sendObservable());

        WidgetObservable.text(bodyText)
                .map(new Func1<OnTextChangeEvent, String>() {
                    @Override
                    public String call(OnTextChangeEvent onTextChangeEvent) {
                        return onTextChangeEvent.text().toString();
                    }
                })
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(postPresenter.bodyObservable());

        WidgetObservable.text(nameText)
                .map(new Func1<OnTextChangeEvent, String>() {
                    @Override
                    public String call(OnTextChangeEvent onTextChangeEvent) {
                        return onTextChangeEvent.text().toString();
                    }
                })
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(postPresenter.nameObservable());

        postPresenter.postSuccesObservable()
                .compose(lifecycleMainObservable.<Response>bindLifecycle())
                .subscribe(new Action1<Response>() {
                    @Override
                    public void call(Response response) {
                        finish();
                    }
                });

        postPresenter.postErrorObservable()
                .compose(lifecycleMainObservable.<Throwable>bindLifecycle())
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                });

        postPresenter.errorObservable()
                .map(ErrorHelper.mapThrowableToStringError())
                .compose(lifecycleMainObservable.<String>bindLifecycle())
                .subscribe(ViewActions.setText(error));

    }

}
