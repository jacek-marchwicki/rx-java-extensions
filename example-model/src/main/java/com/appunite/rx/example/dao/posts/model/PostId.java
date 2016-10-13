package com.appunite.rx.example.dao.posts.model;

import javax.annotation.Nonnull;

public class PostId {
    @Nonnull
    private final String id;

    public PostId(@Nonnull String id) {
        this.id = id;
    }

    @Nonnull
    public String id() {
        return id;
    }

}
