package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Post {
    @Nonnull
    private final String id;
    @Nonnull
    private final String name;

    public Post(@Nonnull String id,
                @Nonnull String name) {
        this.id = id;
        this.name = name;
    }

    @Nonnull
    public String id() {
        return id;
    }

    @Nonnull
    public String name() {
        return name;
    }
}
