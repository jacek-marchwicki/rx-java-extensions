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
import rx.Observable;

public interface PostsService {

    @GET("v1/posts?limit=50&fields=next_token%2Cposts(id%2Cname)&prettyPrint=false")
    @Nonnull
    Observable<PostsResponse> listPosts(@Header("Authorization") String authorization, @Query("next_token") @Nullable String nextToken);

    @GET("v1/posts_ids?limit=50&prettyPrint=false")
    @Nonnull
    Observable<PostsIdsResponse> listPostsIds(@Header("Authorization") String authorization, @Query("next_token") @Nullable String nextToken);

    @GET("v1/posts/{postId}?prettyPrint=false")
    @Nonnull
    Observable<PostWithBody> getPost(@Header("Authorization") String authorization, @Path("postId") @Nonnull String id);

    @POST("v1/posts")
    @Nonnull
    Observable<PostWithBody> createPost(@Header("Authorization") String authorization, @Body @Nonnull AddPost addPost);

}
