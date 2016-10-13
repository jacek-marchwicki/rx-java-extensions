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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheTest {

    @Mock
    private Cache.CacheProvider<String, String> provider;
    private Cache<String, String> cache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        cache = new Cache<>(provider);
    }

    @Test
    public void testPreconditions() throws Exception {
        assert_().that(provider).isNotNull();
        assert_().that(cache).isNotNull();
    }

    @Test
    public void whenCacheIsCalled_valueIsReturned() throws Exception {
        when(provider.load("key1")).thenReturn("value1");

        assert_().that(cache.get("key1")).isEqualTo("value1");
    }

    @Test
    public void whenCacheIsCalledTwice_providerIsExecutedOnce() throws Exception {
        when(provider.load("key1")).thenReturn("value1");

        cache.get("key1");
        cache.get("key1");

        verify(provider, times(1)).load("key1");
    }

    @Test
    public void valuesForDifferentKeys_areReturnedCorrectly() throws Exception {
        when(provider.load("key1")).thenReturn("value1");
        when(provider.load("key2")).thenReturn("value2");

        assert_().that(cache.get("key1")).isEqualTo("value1");
        assert_().that(cache.get("key2")).isEqualTo("value2");
    }
}