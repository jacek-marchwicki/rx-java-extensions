package com.appunite.rx.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import com.google.common.collect.ImmutableList;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import javax.annotation.Nonnull;

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
            private final SerialSubscription subscription = new SerialSubscription();

            public MainViewHolder(@Nonnull View itemView) {
                super(itemView);
                text = checkNotNull((TextView) itemView.findViewById(R.id.main_adapter_item_text));
            }

            @Override
            public void bind(@Nonnull MainPresenter.AdapterItem item) {
                text.setText(item.text());
                subscription.set(Subscriptions.empty());
                subscription.set(Subscriptions.from(
                        RxView.clicks(text)
                                .subscribe(item.clickObserver())
                ));
            }

            @Override
            public void onViewRecycled() {
                subscription.set(Subscriptions.empty());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final UniversalAdapter mainAdapter = new UniversalAdapter(
                ImmutableList.<ViewHolderManager>of(new AdapterItemManager()));

        final RecyclerView recyclerView = checkNotNull((RecyclerView) findViewById(R.id.main_activity_recycler_view));
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mainAdapter);

        // Normally use dagger
        final MainPresenter presenter = new MainPresenter(
                FakeDagger.getPostsDaoInstance(getApplication()),
                MyAndroidSchedulers.mainThread());

        subscription.set(Subscriptions.from(
                presenter.titleObservable()
                        .subscribe(RxToolbarMore.title(checkNotNull((Toolbar)findViewById(R.id.main_activity_toolbar)))),
                presenter.itemsObservable()
                        .subscribe(mainAdapter),
                presenter.progressObservable()
                        .subscribe(RxView.visibility(checkNotNull(findViewById(R.id.main_activity_progress)), View.INVISIBLE)),
                presenter.errorObservable()
                        .map(ErrorHelper.mapThrowableToStringError())
                        .subscribe(RxTextView.text(checkNotNull((TextView)findViewById(R.id.main_activity_error)))),
                presenter.openDetailsObservable()
                        .subscribe(startDetailsActivityAction(this)),
                RxRecyclerView.scrollEvents(recyclerView)
                        .filter(LoadMoreHelper.mapToNeedLoadMore(layoutManager, mainAdapter))
                        .subscribe(presenter.loadMoreObserver()),
                RxView.clicks(checkNotNull(findViewById(R.id.main_activity_fab)))
                        .subscribe(presenter.clickOnFabObserver()),
                presenter.startCreatePostActivityObservable()
                        .subscribe(startPostActivityAction(this))
        ));
    }

    @Override
    protected void onDestroy() {
        subscription.set(Subscriptions.empty());
        super.onDestroy();
    }

    @Nonnull
    private static Action1<MainPresenter.AdapterItem> startDetailsActivityAction(final Activity activity) {
        return new Action1<MainPresenter.AdapterItem>() {
            @Override
            public void call(MainPresenter.AdapterItem adapterItem) {
                //noinspection unchecked
                final Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
                        .toBundle();
                ActivityCompat.startActivity(activity,
                        DetailsActivity.getIntent(activity, adapterItem.id()),
                        bundle);
            }
        };
    }

    @Nonnull
    private static Action1<Object> startPostActivityAction(final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object o) {

                activity.startActivity(CreatePostActivity.newIntent(activity));
            }
        };
    }

}
