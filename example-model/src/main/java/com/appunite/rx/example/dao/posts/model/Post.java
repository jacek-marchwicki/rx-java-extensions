package com.appunite.rx.example.dao.posts.model;

import javax.annotation.Nonnull;

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
