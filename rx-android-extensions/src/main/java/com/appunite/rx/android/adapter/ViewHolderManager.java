/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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

package com.appunite.rx.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

/**
 * Manager that managing of creation {@link ViewHolderManager.BaseViewHolder}
 */
public interface ViewHolderManager {

    /**
     * Return if this manager can handle that kind of type
     * @param baseAdapterItem adapter item
     * @return true if can handle this item
     */
    boolean matches(@Nonnull BaseAdapterItem baseAdapterItem);

    /**
     * Create {@link ViewHolderManager.BaseViewHolder} for this item
     * @param parent parent view
     * @param inflater layout inflater
     * @return new {@link ViewHolderManager.BaseViewHolder}
     */
    @Nonnull
    BaseViewHolder createViewHolder(@Nonnull ViewGroup parent, @Nonnull LayoutInflater inflater);

    /**
     * ViewHolder for managing view
     * @param <T> type of adapter item
     */
    abstract class BaseViewHolder<T extends BaseAdapterItem> extends RecyclerView.ViewHolder {

        /**
         * Create view holder
         * @param itemView view
         */
        public BaseViewHolder(@Nonnull View itemView) {
            super(itemView);
        }

        /**
         * Called when a view created by this view holder has been recycled.
         *
         * @see {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)}
         */
        public void onViewRecycled() {

        }
        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * should update the contents of that view holder to reflect the item.
         *
         * @see {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}}
         */
        public abstract void bind(@Nonnull final T item);

        /**
         * Called by the RecyclerView if a view holder cannot be recycled due to its transient
         * state. Upon receiving this callback, view holder can clear the animation(s) that effect
         * the View's transient state and return <code>true</code> so that the View can be recycled.
         * Keep in mind that the View in question is already removed from the RecyclerView.
         *
         * @see {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)}
         *
         * @return True if the View should be recycled, false otherwise. Note that if this method
         * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
         * the View and recycle it regardless. If this method returns <code>false</code>,
         * RecyclerView will check the View's transient state again before giving a final decision.
         * Default implementation returns false.
         */
        public boolean onFailedToRecycleView() {
            return false;
        }

        /**
         * Called when a view created by this view holder has been attached to a window.
         *
         * @see {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
         */
        public void onViewAttachedToWindow() {

        }
        /**
         * Called when a view created by this view holder has been detached from its window.
         *
         * @see {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
         */
        public void onViewDetachedFromWindow() {

        }
    }
}
