package com.appunite.rx.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.PostPresenter;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.client.Response;
import rx.Observer;
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
    private boolean doubleBackToExitPressedOnce;

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

        postPresenter.postSuccesObservable()
                .subscribe(new Observer<Response>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    Toast.makeText(getApplicationContext(),"Error, try again later", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Response response) {
                        finish();
                    }
                });

    }
    
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Are you sure want to cancel post?", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 3000);
    }

}
