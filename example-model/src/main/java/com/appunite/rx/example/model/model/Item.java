package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Item {
    @Nonnull
    private final String id;
    @Nullable
    private final String name;

    public Item(@Nonnull String id,
                @Nullable String name) {
        this.id = id;
        this.name = name;
    }

    @Nonnull
    public String id() {
        return id;
    }

    @Nullable
    public String name() {
        return name;
    }
}
