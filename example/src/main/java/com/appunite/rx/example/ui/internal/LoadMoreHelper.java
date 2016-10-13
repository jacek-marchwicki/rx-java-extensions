/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.example.ui.internal;

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
