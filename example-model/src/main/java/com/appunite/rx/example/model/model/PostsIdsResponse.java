package com.appunite.rx.example.model.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostsIdsResponse {
    @Nonnull
    private final String title;

    @Nonnull
    private final ImmutableList<PostId> posts;
    @Nullable
    private final String nextToken;

    public PostsIdsResponse(@Nonnull String title,
                            @Nonnull ImmutableList<PostId> posts,
                            @Nullable String nextToken) {
        this.title = title;
        this.posts = posts;
        this.nextToken = nextToken;
    }

    @Nonnull
    public String title() {
        return title;
    }

    @Nonnull
    public ImmutableList<PostId> items() {
        return posts;
    }

    public String nextToken() {
        return nextToken;
    }
}
