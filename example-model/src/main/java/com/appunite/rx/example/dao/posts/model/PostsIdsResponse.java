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
