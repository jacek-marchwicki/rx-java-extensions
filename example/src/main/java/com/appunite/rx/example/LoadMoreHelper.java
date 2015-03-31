package com.appunite.rx.example;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import javax.annotation.Nonnull;

import rx.functions.Func1;

public class LoadMoreHelper {

    @Nonnull
    public static Func1<Object, Boolean> mapToNeedLoadMore(@Nonnull final LinearLayoutManager layoutManager,
                                               @Nonnull final RecyclerView.Adapter<?> adapter) {
        return new Func1<Object, Boolean>() {
            @Override
            public Boolean call(final Object recyclerScrollEvent) {
                return LoadMoreHelper.isNeedLoadMore(layoutManager, adapter);
            }
        };
    }

    private static boolean isNeedLoadMore(@Nonnull LinearLayoutManager layoutManager,
                                         @Nonnull RecyclerView.Adapter<?> adapter) {
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        if (lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        final int countsOnPage = lastVisibleItemPosition - firstVisibleItemPosition;
        final int twoPages = countsOnPage * 2;
        return lastVisibleItemPosition + twoPages + 5 >= adapter.getItemCount();
    }

}
