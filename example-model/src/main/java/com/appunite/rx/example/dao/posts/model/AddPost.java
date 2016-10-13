/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.example.dao.posts.model;

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
