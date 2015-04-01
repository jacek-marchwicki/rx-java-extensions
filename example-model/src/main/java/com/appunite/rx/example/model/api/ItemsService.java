package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface ItemsService {

    @GET("/v1/posts?limit=50&fields=next_token%2Cposts(id%2Cname)&prettyPrint=false")
    @Nonnull
    Observable<Response> listItems(@Query("next_token") @Nullable String nextToken);

    @Nonnull
    @GET("/v1/posts/{postId}?prettyPrint=false")
    Observable<PostWithBody> getItem(@Path("postId") @Nonnull String id);
}
