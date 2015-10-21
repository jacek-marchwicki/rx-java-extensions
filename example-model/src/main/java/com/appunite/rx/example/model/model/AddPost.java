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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddPost)) return false;

        final AddPost addPost = (AddPost) o;

        return name.equals(addPost.name)
                && body.equals(addPost.body);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AddPost{" +
                "name='" + name + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
