package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.PostsIdsResponse;
import com.appunite.rx.example.model.model.PostsResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface GuestbookService {

    @GET("/v1/posts?limit=50&fields=next_token%2Cposts(id%2Cname)&prettyPrint=false")
    @Nonnull
    Observable<PostsResponse> listPosts(@Query("next_token") @Nullable String nextToken);

    @GET("/v1/posts_ids?limit=50&prettyPrint=false")
    @Nonnull
    Observable<PostsIdsResponse> listPostsIds(@Query("next_token") @Nullable String nextToken);

    @GET("/v1/posts/{postId}?prettyPrint=false")
    @Nonnull
    Observable<PostWithBody> getPost(@Path("postId") @Nonnull String id);

    @POST("/v1/posts")
    @Nonnull
    Observable<PostWithBody> createPost(@Body @Nonnull AddPost addPost);

}
