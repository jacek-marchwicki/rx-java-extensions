package com.appunite.rx.example;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.UniversalAdapter;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.example.dagger.FakeDagger;
import com.appunite.rx.example.model.presenter.MainPresenter;
import com.firebase.ui.auth.AuthUI;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.Arrays;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import static com.google.common.base.Preconditions.checkNotNull;

public class MainActivity extends BaseActivity {


    @Nonnull
    private final SerialSubscription subscription = new SerialSubscription();

    public static class AdapterItemManager implements ViewHolderManager {

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof MainPresenter.AdapterItem;
        }

        @Nonnull
        @Override
        public BaseViewHolder createViewHolder(@Nonnull ViewGroup parent,
                                               @Nonnull LayoutInflater inflater) {
            return new MainViewHolder(inflater.inflate(R.layout.main_adapter_item, parent, false));
        }

        private class MainViewHolder extends BaseViewHolder<MainPresenter.AdapterItem> {

            @Nonnull
            private final TextView text;
            @Nonnull
            private final SerialSubscription subscription = new SerialSubscription();
            @Nonnull
            private final Observable<Object> clickObservable;

            MainViewHolder(@Nonnull View itemView) {
                super(itemView);
                text = checkNotNull((TextView) itemView.findViewById(R.id.main_adapter_item_text));
                clickObservable = RxView.clicks(text).share();
            }

            @Override
            public void bind(@Nonnull MainPresenter.AdapterItem item) {
                text.setText(item.text());
                subscription.set(Subscriptions.from(
                        clickObservable
                                .subscribe(item.clickObserver())
                ));
            }

            @Override
            public void onViewRecycled() {
                subscription.set(Subscriptions.empty());
            }

        }
    }

    public static class ErrorItemManager implements ViewHolderManager {

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof MainPresenter.ErrorAdapterItem;
        }

        @Nonnull
        @Override
        public BaseViewHolder createViewHolder(@Nonnull ViewGroup parent,
                                               @Nonnull LayoutInflater inflater) {
            return new MainViewHolder(inflater.inflate(R.layout.main_error_item, parent, false));
        }

        private class MainViewHolder extends BaseViewHolder<MainPresenter.ErrorAdapterItem> {

            @Nonnull
            private final TextView text;

            MainViewHolder(@Nonnull View itemView) {
                super(itemView);
                text = checkNotNull((TextView) itemView.findViewById(R.id.main_error_item_text));
            }

            @Override
            public void bind(@Nonnull MainPresenter.ErrorAdapterItem item) {
                //noinspection ThrowableResultOfMethodCallIgnored
                text.setText(item.error().getMessage());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final UniversalAdapter mainAdapter = new UniversalAdapter(Arrays.asList(
                new AdapterItemManager(),
                new ErrorItemManager()));

        final RecyclerView recyclerView = checkNotNull((RecyclerView) findViewById(R.id.main_activity_recycler_view));
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mainAdapter);

        // Normally use dagger
        final MainPresenter presenter = new MainPresenter(
                FakeDagger.getPostsDaoInstance(getApplication()),
                FakeDagger.getCurrentLoggedInUserDaoInstance(),
                MyAndroidSchedulers.mainThread());


        final Toolbar toolbar = checkNotNull((Toolbar) findViewById(R.id.main_activity_toolbar));
        toolbar.inflateMenu(R.menu.main_activity);
        final Observable<RxToolbarMore.ToolbarMenuEvent> menuClicks = RxToolbarMore.menuClick(toolbar).share();


        subscription.set(Subscriptions.from(
                menuClicks.filter(RxToolbarMore.filterMenuClick(R.id.main_activity_menu_login))
                        .subscribe(presenter.clickLoginObserver()),
                menuClicks.filter(RxToolbarMore.filterMenuClick(R.id.main_activity_menu_logout))
                        .subscribe(presenter.clickLogoutObserver()),
                presenter.loginVisibleObservable()
                    .subscribe(setVisibility(toolbar.getMenu().findItem(R.id.main_activity_menu_login))),
                presenter.logoutVisibleObservable()
                        .subscribe(setVisibility(toolbar.getMenu().findItem(R.id.main_activity_menu_logout))),
                presenter.titleObservable()
                        .subscribe(RxToolbarMore.title(toolbar)),
                presenter.itemsObservable()
                        .subscribe(mainAdapter),
                presenter.progressObservable()
                        .subscribe(RxView.visibility(checkNotNull(findViewById(R.id.main_activity_progress)), View.INVISIBLE)),
                presenter.errorObservable()
                        .map(ErrorHelper.mapThrowableToStringError())
                        .subscribe(RxTextView.text(checkNotNull((TextView)findViewById(R.id.main_activity_error)))),
                presenter.openDetailsObservable()
                        .subscribe(new Action1<MainPresenter.AdapterItem>() {
                            @Override
                            public void call(MainPresenter.AdapterItem adapterItem) {
                                //noinspection unchecked
                                final Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this)
                                        .toBundle();
                                ActivityCompat.startActivity(MainActivity.this,
                                        DetailsActivity.getIntent(MainActivity.this, adapterItem.id()),
                                        bundle);
                            }
                        }),
                RxRecyclerView.scrollEvents(recyclerView)
                        .filter(LoadMoreHelper.mapToNeedLoadMore(layoutManager, mainAdapter))
                        .subscribe(presenter.loadMoreObserver()),
                RxView.clicks(checkNotNull(findViewById(R.id.main_activity_fab)))
                        .subscribe(presenter.clickOnFabObserver()),
                presenter.startCreatePostActivityObservable()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                startActivity(CreatePostActivity.newIntent(MainActivity.this));
                            }
                        }),
                presenter.logoutUserObservable()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                AuthUI.getInstance().signOut(MainActivity.this);
                            }
                        }),
                presenter.startFirebaseLoginActivity()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                startActivity(AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setProviders(AuthUI.EMAIL_PROVIDER, AuthUI.GOOGLE_PROVIDER)
                                        .setTheme(R.style.LoginTheme)
                                        .build());
                            }
                        })
        ));
    }

    @Nonnull
    private Action1<Boolean> setVisibility(@Nonnull final MenuItem menuItem) {
        return new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                menuItem.setVisible(aBoolean);
            }
        };
    }

    @Override
    protected void onDestroy() {
        subscription.set(Subscriptions.empty());
        super.onDestroy();
    }

}
