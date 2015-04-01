package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Post extends PostId {
    @Nonnull
    private final String name;

    public Post(@Nonnull String id,
                @Nonnull String name) {
        super(id);
        this.name = name;
    }

    @Nonnull
    public String name() {
        return name;
    }
}
