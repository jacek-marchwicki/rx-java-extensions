/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
class DiskCacheCreator<T> implements CacheSubject.CacheCreator<T> {

    @Nonnull
    private final Gson gson;
    @Nonnull
    private final Type typeOfT;
    @Nonnull
    private final File file;

    private final Object lock = new Object();

    DiskCacheCreator(@Nonnull Gson gson,
                     @Nonnull Type typeOfT,
                     @Nonnull File file) {
        this.gson = gson;
        this.typeOfT = typeOfT;
        this.file = file;
    }

    @Nullable
    @Override
    public T readFromCache() {
        synchronized (lock) {
            try {
                final BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(file)));
                try {
                    return gson.fromJson(bufferedReader, typeOfT);
                } finally {
                    bufferedReader.close();
                }
            } catch (IOException | JsonIOException | JsonSyntaxException ignore) {
                System.out.print(ignore);
                return null;
            }
        }
    }

    @Override
    public void writeToCache(@Nullable T data) {
        synchronized (lock) {
            if (data == null) {
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
            try {
                final BufferedWriter bufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file)));
                try {
                    gson.toJson(data, typeOfT, new JsonWriter(bufferedWriter));
                } finally {
                    bufferedWriter.close();
                }
            } catch (IOException | JsonIOException ignore) {
                System.out.print(ignore);
            }
        }
    }
}
