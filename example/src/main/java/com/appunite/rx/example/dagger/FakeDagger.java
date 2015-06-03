package com.appunite.rx.example.dagger;

import android.content.Context;
import android.support.annotation.NonNull;

import com.appunite.gson.AndroidUnderscoreNamingStrategy;
import com.appunite.gson.ImmutableListDeserializer;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.example.model.api.GuestbookService;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.helpers.CacheProvider;
import com.appunite.rx.example.model.helpers.DiskCacheCreator;
import com.appunite.rx.subjects.CacheSubject;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

public class FakeDagger {

    /*
        Normally we rather use dagger instead of static, but for testing purposes is ok
         */
    private static final Object LOCK = new Object();
    private static PostsDao postsDao;

    private static class SyncExecutor implements Executor {
        @Override
        public void execute(@Nonnull final Runnable command) {
            command.run();
        }
    }

    public static PostsDao getInstance(@Nonnull final Context context) {
        synchronized (LOCK) {
            if (postsDao != null) {
                return postsDao;
            }
            final Gson gson = getGson();

            final OkHttpClient client = getOkHttpClient(context);
            final RestAdapter restAdapter = getRestAdapter(gson, client);
            final GuestbookService guestbookService = restAdapter.create(GuestbookService.class);
            final CacheProvider cacheProvider = getCacheProvider(context, gson);
            postsDao = new PostsDao(MyAndroidSchedulers.networkScheduler(), AndroidSchedulers.mainThread(), guestbookService, cacheProvider);
            return postsDao;
        }
    }

    @NonNull
    private static OkHttpClient getOkHttpClient(@Nonnull Context context) {
        final OkHttpClient client = new OkHttpClient();
        client.setCache(getCacheOrNull(new File(context.getCacheDir(), "ok-http")));
        return client;
    }

    private static RestAdapter getRestAdapter(Gson gson, OkHttpClient client) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint("https://atlantean-field-90117.appspot.com/_ah/api/guestbook/")
                .setExecutors(new SyncExecutor(), new SyncExecutor())
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("Retrofit"))
                .build();
    }

    @NonNull
    private static CacheProvider getCacheProvider(@Nonnull final Context context, final Gson gson) {
        return new CacheProvider() {
                    @Nonnull
                    @Override
                    public <T> CacheSubject.CacheCreator<T> getCacheCreatorForKey(@Nonnull String key, @Nonnull Type type) {
                        return new DiskCacheCreator<>(gson, type, new File(context.getCacheDir(), key + ".txt"));
                    }
                };
    }

    @NonNull
    private static Gson getGson() {
        return new GsonBuilder()
                        .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
                        .setFieldNamingStrategy(new AndroidUnderscoreNamingStrategy())
                        .create();
    }

    @Nullable
    private static Cache getCacheOrNull(@Nonnull File cacheDirectory) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        try {
            return new Cache(cacheDirectory, cacheSize);
        } catch (IOException e) {
            return null;
        }
    }
}
