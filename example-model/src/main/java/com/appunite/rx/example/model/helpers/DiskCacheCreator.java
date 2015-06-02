package com.appunite.rx.example.model.helpers;

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
public class DiskCacheCreator<T> implements CacheSubject.CacheCreator<T> {

    @Nonnull
    private final Gson gson;
    @Nonnull
    private final Type typeOfT;
    @Nonnull
    private final File file;

    private final Object lock = new Object();

    public DiskCacheCreator(@Nonnull Gson gson,
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
