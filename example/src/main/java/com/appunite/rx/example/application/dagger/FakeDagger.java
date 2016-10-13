package com.appunite.rx.example.application.dagger;

import android.content.Context;

import com.appunite.gson.AndroidUnderscoreNamingStrategy;
import com.appunite.rx.android.MyAndroidNetworkSchedulers;
import com.appunite.rx.example.dao.auth.FirebaseCurrentLoggedInUserDao;
import com.appunite.rx.example.dao.internal.helpers.CacheProviderImpl;
import com.appunite.rx.example.dao.posts.PostsService;
import com.appunite.rx.example.dao.auth.MyCurrentLoggedInUserDao;
import com.appunite.rx.example.dao.posts.PostsDao;
import com.appunite.rx.example.dao.internal.helpers.CacheProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import javax.annotation.Nonnull;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Normally we rather use dagger instead of static, but for testing purposes is ok
 */
public class FakeDagger {

    private static final Object LOCK = new Object();
    private static PostsDao postsDao;
    private static MyCurrentLoggedInUserDao currentLoggedInUserDao;

    @Nonnull
    public static MyCurrentLoggedInUserDao getCurrentLoggedInUserDaoInstance() {
        synchronized (LOCK) {
            if (currentLoggedInUserDao != null) {
                return currentLoggedInUserDao;
            }
            currentLoggedInUserDao = new FirebaseCurrentLoggedInUserDao();
            return currentLoggedInUserDao;
        }
    }

    @Nonnull
    public static PostsDao getPostsDaoInstance(@Nonnull final Context context) {
        synchronized (LOCK) {
            if (postsDao != null) {
                return postsDao;
            }
            final Gson gson = getGson();
            final OkHttpClient client = getOkHttpClient(context);
            final Retrofit restAdapter = getRestAdapter(gson, client);
            final PostsService postsService = restAdapter.create(PostsService.class);
            final CacheProvider cacheProvider = getCacheProvider(context, gson);
            postsDao = new PostsDao(MyAndroidNetworkSchedulers.networkScheduler(), postsService, cacheProvider, getCurrentLoggedInUserDaoInstance());
            return postsDao;
        }
    }

    @Nonnull
    private static OkHttpClient getOkHttpClient(@Nonnull Context context) {
        final File cacheDirectory = new File(context.getCacheDir(), "ok-http");
        return new OkHttpClient.Builder()
                .cache(getCache(cacheDirectory))
                .build();
    }

    @Nonnull
    private static Retrofit getRestAdapter(@Nonnull Gson gson, @Nonnull OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl("https://atlantean-field-90117.appspot.com/_ah/api/guestbook/")
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Nonnull
    private static CacheProvider getCacheProvider(@Nonnull final Context context,  @Nonnull final Gson gson) {
        return new CacheProviderImpl(gson, context.getCacheDir());
    }

    @Nonnull
    private static Gson getGson() {
        return new GsonBuilder()
                .setFieldNamingStrategy(new AndroidUnderscoreNamingStrategy())
                .create();
    }

    @Nonnull
    private static Cache getCache(@Nonnull File cacheDirectory) {
        long cacheSize = 10L * 1024L * 1024L; // 10 MiB
        return new Cache(cacheDirectory, cacheSize);
    }

}
