package com.appunite.rx.example.model.helpers;

import com.appunite.rx.subjects.CacheSubject;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;

public interface CacheProvider {

    @Nonnull <T> CacheSubject.CacheCreator<T> getCacheCreatorForKey(@Nonnull String key,
                                                                    @Nonnull Type type);
}
