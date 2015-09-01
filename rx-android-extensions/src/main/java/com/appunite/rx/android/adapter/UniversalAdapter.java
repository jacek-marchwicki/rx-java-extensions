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
import android.view.ViewGroup;

import com.appunite.detector.ChangesDetector;
import com.appunite.detector.SimpleDetector;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;

import rx.functions.Action1;

/**
 * Universal adapter for {@link RecyclerView}
 */
public class UniversalAdapter extends RecyclerView.Adapter<ViewHolderManager.BaseViewHolder>
        implements Action1<List<BaseAdapterItem>>, ChangesDetector.ChangesAdapter {
    @Nonnull
    private final ChangesDetector<BaseAdapterItem, BaseAdapterItem> changesDetector =
            new ChangesDetector<>(new SimpleDetector<BaseAdapterItem>());
    @Nonnull
    private final List<ViewHolderManager> managers;
    @Nonnull
    private List<BaseAdapterItem> items = ImmutableList.of();

    /**
     * Usage:
     * <pre>{@code
     *   public static class Item implements BaseAdapterItem {
     *
     *     @Nonnull
     *     private final String id;
     *     @Nullable
     *     private final String lastMessage;
     *
     *     public ChatsItem(@Nonnull String id,
     *     @Nullable String lastMessage) {
     *       this.id = id;
     *       this.lastMessage = lastMessage;
     *     }
     *
     *     @Override
     *     public boolean matches(@Nonnull BaseAdapterItem item) {
     *       return item instanceof Item && Objects.equal(id, ((Item)item).id);
     *     }
     *
     *     @Override
     *     public boolean same(@Nonnull BaseAdapterItem item) {
     *       return equals(item);
     *     }
     *
     *     @Nonnull
     *     public String id() {
     *       return id;
     *     }
     *
     *     @Nullable
     *     public String lastMessage() {
     *       return lastMessage;
     *     }
     *
     *     @Override
     *     public boolean equals(Object o) {
     *       if (this == o) return true;
     *       if (!(o instanceof Item)) return false;
     *       final Item chatsItem = (ChatsItem) o;
     *       return Objects.equal(id, chatsItem.id) &&
     *       Objects.equal(lastMessage, chatsItem.lastMessage);
     *     }
     *
     *     @Override
     *     public int hashCode() {
     *       return Objects.hashCode(id, lastMessage);
     *     }
     *
     *     @Override
     *     public long adapterId() {
     *       return id.hashCode();
     *     }
     *   }
     *
     *   private static class MyViewHolderManager implements ViewHolderManager {
     *     @Override
     *     public boolean matches(BaseAdapterItem baseAdapterItem) {
     *       return baseAdapterItem instanceof Item;
     *     }
     *
     *      @Override
     *     public BaseViewHolder createViewHolder(ViewGroup parent, LayoutInflater from) {
     *       return new Holder(from.inflate(R.layout.activity_chats_item, parent, false));
     *     }
     *
     *     public static class Holder extends BaseViewHolder<Item> {
     *
     *       private final TextView textView;
     *
     *       public Holder(View itemView) {
     *         super(itemView);
     *         textView = (TextView) itemView;
     *       }
     *
     *       @Override
     *       public void bind(@Nonnull Item item) {
     *         textView.setText(item.lastMessage());
     *       }
     *
     *     }
     *   }
     *
     *   final UniversalAdapter adapter = new UniversalAdapter(
     *     ImmutableList.<ViewHolderManager>of(new MyViewHolderManager()));
     *
     *   recyclerView.setAdapter(adapter);
     * }</pre>
     * @param managers for inflating views
     */
    public UniversalAdapter(@Nonnull List<ViewHolderManager> managers) {
        this.managers = managers;
    }

    @Override
    public void call(@Nonnull List<BaseAdapterItem> baseAdapterItems) {
        items = baseAdapterItems;
        changesDetector.newData(this, items, false);
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolderManager manager = managers.get(viewType);
        return manager.createViewHolder(parent, LayoutInflater.from(parent.getContext()));
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem baseAdapterItem = items.get(position);
        for (int i = 0; i < managers.size(); i++) {
            final ViewHolderManager manager = managers.get(i);
            if (manager.matches(baseAdapterItem)) {
                return i;
            }
        }
        throw new RuntimeException("Unsupported item type: " + baseAdapterItem);
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).adapterId();
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolderManager.BaseViewHolder holder) {
        return holder.onFailedToRecycleView();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolderManager.BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolderManager.BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
    }

    @Override
    public void onViewRecycled(ViewHolderManager.BaseViewHolder holder) {
        holder.onViewRecycled();
        super.onViewRecycled(holder);
    }
}
