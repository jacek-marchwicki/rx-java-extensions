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

package com.appunite.cache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * In memory cache that stores values by key using provider - it never free memory
 * @param <K> key of cache
 * @param <V> value of cache
 */
public class Cache<K, V> {
    @Nonnull
    private final CacheProvider<K, V> provider;
    @Nonnull
    private final Map<K, V> cached = new HashMap<>();

    /**
     * Cache providers, it should generate values for given key
     *
     * @param <K> key
     * @param <V> value
     */
    public interface CacheProvider<K, V> {
        /**
         * Generate value for key
         *
         * @param key key
         * @return value
         */
        @Nonnull
        V load(@Nonnull K key);
    }

    /**
     * Create cache
     *
     * @param provider generator for keys
     */
    public Cache(@Nonnull  CacheProvider<K, V> provider) {
        this.provider = provider;
    }

    /**
     * Get value from cache
     * @param key key
     * @return value
     */
    @Nonnull
    public V get(@Nonnull K key) {
        synchronized (cached) {
            final V value = cached.get(key);
            if (value != null) {
                return value;
            }
            final V newValue = provider.load(key);
            cached.put(key, newValue);
            return newValue;
        }
    }
}