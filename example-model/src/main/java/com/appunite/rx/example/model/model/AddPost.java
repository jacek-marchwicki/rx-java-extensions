package com.appunite.rx.example.model.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("body", body)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddPost)) return false;
        final AddPost addPost = (AddPost) o;
        return Objects.equal(name, addPost.name) &&
                Objects.equal(body, addPost.body);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, body);
    }
}
