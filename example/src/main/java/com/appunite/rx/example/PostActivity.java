package com.appunite.rx.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.appunite.rx.android.MoreActivityActions;
import com.appunite.rx.android.MoreViewObservables;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.PostPresenter;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
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

    @InjectView(R.id.accept)
    FloatingActionButton accept;
    @InjectView(R.id.cancel)
    FloatingActionButton cancel;
    @InjectView(R.id.bodyText)
    EditText bodyText;
    @InjectView(R.id.nameText)
    EditText nameText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);

        ButterKnife.inject(this);

        final PostPresenter postPresenter = new PostPresenter(AndroidSchedulers.mainThread(),
                FakeDagger.getPostsDaoInstance(getApplicationContext()));


        ViewObservable.clicks(cancel)
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        finish();
                    }
                });

        ViewObservable.clicks(accept).map(new Func1<OnClickEvent, Object>() {
            @Override
            public Object call(OnClickEvent onClickEvent) {
                Log.v("call", "click");
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
    }

}
