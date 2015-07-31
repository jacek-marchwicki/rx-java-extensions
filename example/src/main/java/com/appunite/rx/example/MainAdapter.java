package com.appunite.rx.example;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.appunite.rx.example.model.presenter.MainPresenter;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(@Nonnull MainPresenter.AdapterItem item);

    public abstract void recycle();
}

public class MainAdapter extends RecyclerView.Adapter<BaseViewHolder> implements
        Action1<List<MainPresenter.AdapterItem>>, ChangesDetector.ChangesAdapter {

    @Nonnull
    private final ChangesDetector<MainPresenter.AdapterItem, MainPresenter.AdapterItem> changesDetector;
    @Nonnull
    private List<MainPresenter.AdapterItem> items = ImmutableList.of();

    @Inject
    public MainAdapter() {
        this.changesDetector = new ChangesDetector<>(new SimpleDetector<MainPresenter.AdapterItem>());
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.main_adapter_item, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public void onViewRecycled(BaseViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void call(@Nonnull List<MainPresenter.AdapterItem> items) {
        this.items = items;
        changesDetector.newData(this, items, false);
    }

    private class MainViewHolder extends BaseViewHolder {

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
            subscription = new CompositeSubscription(
                    ViewObservable.clicks(text).subscribe(item.clickObserver())
            );
        }

        @Override
        public void recycle() {
            subscription.unsubscribe();
        }

    }

}
