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

package com.appunite.rx.example.dao.posts;

import com.appunite.rx.example.dao.posts.model.AddPost;
import com.appunite.rx.example.dao.posts.model.PostWithBody;
import com.appunite.rx.example.dao.posts.model.PostsIdsResponse;
import com.appunite.rx.example.dao.posts.model.PostsResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface PostsService {

    @GET("v1/posts?limit=50&fields=next_token%2Cposts(id%2Cname)&prettyPrint=false")
    @Nonnull
    Single<PostsResponse> listPosts(@Header("Authorization") String authorization, @Query("next_token") @Nullable String nextToken);

    @GET("v1/posts_ids?limit=50&prettyPrint=false")
    @Nonnull
    Single<PostsIdsResponse> listPostsIds(@Header("Authorization") String authorization, @Query("next_token") @Nullable String nextToken);

    @GET("v1/posts/{postId}?prettyPrint=false")
    @Nonnull
    Single<PostWithBody> getPost(@Header("Authorization") String authorization, @Path("postId") @Nonnull String id);

    @POST("v1/posts")
    @Nonnull
    Single<PostWithBody> createPost(@Header("Authorization") String authorization, @Body @Nonnull AddPost addPost);

}
