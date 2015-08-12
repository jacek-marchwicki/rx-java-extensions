package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;

public class AddPost {
    @Nonnull
    private final String name;
    @Nonnull
    private final String body;

    public AddPost(@Nonnull String name,
                   @Nonnull String body) {
        this.name = name;
        this.body = body;
    }

    @Nonnull
    public String name() {
        return name;
    }

    @Nonnull
    public String body() {
        return body;
    }
}
