package com.appunite.rx.example.model.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostsIdsResponse {
    @Nonnull
    private final String title;

    @Nonnull
    private final List<PostId> posts;
    @Nullable
    private final String nextToken;

    public PostsIdsResponse(@Nonnull String title,
                            @Nonnull List<PostId> posts,
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
    public List<PostId> items() {
        return posts;
    }

    public String nextToken() {
        return nextToken;
    }
}
