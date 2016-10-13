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
