package com.appunite.rx.example.model.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Response {
    @Nonnull
    private final String title;

    @Nonnull
    private final ImmutableList<Item> items;
    @Nullable
    private final String nextToken;

    public Response(@Nonnull String title,
                    @Nonnull ImmutableList<Item> items,
                    @Nullable String nextToken) {
        this.title = title;
        this.items = items;
        this.nextToken = nextToken;
    }

    @Nonnull
    public String title() {
        return title;
    }

    @Nonnull
    public ImmutableList<Item> items() {
        return items;
    }

    public String nextToken() {
        return nextToken;
    }
}
