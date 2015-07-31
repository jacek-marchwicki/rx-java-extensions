package com.appunite.rx.example.model.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostsResponse {
    @Nonnull
    private final String title;

    @Nonnull
    private final List<Post> posts;
    @Nullable
    private final String nextToken;

    public PostsResponse(@Nonnull String title,
                         @Nonnull List<Post> posts,
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
    public List<Post> items() {
        return posts;
    }

    public String nextToken() {
        return nextToken;
    }
}
