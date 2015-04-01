package com.appunite.rx.example.model.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostsResponse {
    @Nonnull
    private final String title;

    @Nonnull
    private final ImmutableList<Post> posts;
    @Nullable
    private final String nextToken;

    public PostsResponse(@Nonnull String title,
                         @Nonnull ImmutableList<Post> posts,
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
    public ImmutableList<Post> items() {
        return posts;
    }

    public String nextToken() {
        return nextToken;
    }
}
