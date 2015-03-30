package com.appunite.rx.example.model.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

public class Response {
    @Nonnull
    private final String title;

    @Nonnull
    private final ImmutableList<Item> items;

    public Response(@Nonnull String title,
                    @Nonnull ImmutableList<Item> items) {
        this.title = title;
        this.items = items;
    }

    @Nonnull
    public String title() {
        return title;
    }

    @Nonnull
    public ImmutableList<Item> items() {
        return items;
    }
}
