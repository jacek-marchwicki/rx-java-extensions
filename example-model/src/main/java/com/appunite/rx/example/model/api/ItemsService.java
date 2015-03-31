package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.model.ItemWithBody;
import com.appunite.rx.example.model.model.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;

public interface ItemsService {
    @Nonnull
    Observable<Response> listItems(@Nullable String nextToken);

    @Nonnull
    Observable<ItemWithBody> getItem(@Nonnull String id);
}
