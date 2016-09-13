package com.appunite.rx.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

public class MainActivity extends BaseActivity {

    @InjectView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @InjectView(R.id.main_activity_recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.main_activity_progress)
    View progress;
    @InjectView(R.id.main_activity_error)
    TextView error;
    @InjectView(R.id.main_activity_fab)
    FloatingActionButton fab;

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
            private CompositeSubscription subscription;

            public MainViewHolder(@Nonnull View itemView) {
                super(itemView);
                text = checkNotNull((TextView) itemView.findViewById(R.id.main_adapter_item_text));
            }

            @Override
            public void bind(@Nonnull MainPresenter.AdapterItem item) {
                text.setText(item.text());
                unsubscribe();
                subscription = new CompositeSubscription(
                        RxView.clicks(text)
                                .subscribe(item.clickObserver())
                );
            }

            @Override
            public void onViewRecycled() {
                unsubscribe();
            }

            private void unsubscribe() {
                if (subscription != null) {
                    subscription.unsubscribe();
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final UniversalAdapter mainAdapter = new UniversalAdapter(
                ImmutableList.<ViewHolderManager>of(new AdapterItemManager()));

        ButterKnife.inject(this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mainAdapter);

        // Normally use dagger
        final MainPresenter presenter = new MainPresenter(
                FakeDagger.getPostsDaoInstance(getApplication()),
                MyAndroidSchedulers.mainThread());


        presenter.titleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxToolbarMore.title(toolbar));

        presenter.itemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(mainAdapter);

        presenter.progressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progress, View.INVISIBLE));

        presenter.errorObservable()
                .map(ErrorHelper.mapThrowableToStringError())
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(error));

        presenter.openDetailsObservable()
                .compose(this.<MainPresenter.AdapterItem>bindToLifecycle())
                .subscribe(startDetailsActivityAction(this));

        RxRecyclerView.scrollEvents(recyclerView)
                .filter(LoadMoreHelper.mapToNeedLoadMore(layoutManager, mainAdapter))
                .compose(this.bindToLifecycle())
                .subscribe(presenter.loadMoreObserver());

        RxView.clicks(fab)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.clickOnFabObserver());

        presenter.startCreatePostActivityObservable()
                .compose(this.bindToLifecycle())
                .subscribe(startPostActivityAction(this));
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
