package com.appunite.rx.example.model.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostWithBody extends Post {
    @Nonnull
    private final String body;

    public PostWithBody(@Nonnull String id,
                        @Nonnull String name,
                        @Nonnull String body) {
        super(id, name);
        this.body = body;
    }

    @Nonnull
    public String body() {
        return body;
    }
}
