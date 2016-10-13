package com.appunite.rx.example.dao.internal.helpers;

import com.appunite.rx.subjects.CacheSubject;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;

public class CacheProviderImpl implements CacheProvider {
    @Nonnull
    private final Gson gson;
    @Nonnull
    private final File cacheDir;

    public CacheProviderImpl(@Nonnull Gson gson, @Nonnull File cacheDir) {
        this.gson = gson;
        this.cacheDir = cacheDir;
    }

    @Nonnull
    @Override
    public <T> CacheSubject.CacheCreator<T> getCacheCreatorForKey(@Nonnull String key, @Nonnull Type type) {
        return new DiskCacheCreator<>(gson, type,  new File(cacheDir, key + ".txt"));
    }
}
