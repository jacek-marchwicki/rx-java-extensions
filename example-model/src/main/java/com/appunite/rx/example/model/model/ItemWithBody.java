package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemWithBody extends Item {
    @Nullable
    private final String body;

    public ItemWithBody(@Nonnull String id,
                        @Nullable String name,
                        @Nullable String body) {
        super(id, name);
        this.body = body;
    }

    @Nullable
    public String body() {
        return body;
    }
}
