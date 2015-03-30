package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.subjects.PublishSubject;

@Singleton
public class NetworkDao {
    @Nonnull
    private final Observable<ResponseOrError<Response>> data;
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    /*
        Normally we rather use dagger instead of static, but for testing purposes is ok
         */
    private static final Object LOCK = new Object();
    private static NetworkDao networkDao;
    public static NetworkDao getInstance(@Nonnull Scheduler networkScheduler) {
        synchronized (LOCK) {
            if (networkDao != null) {
                return networkDao;
            }
            networkDao = new NetworkDao(networkScheduler);
            return networkDao;
        }
    }

    public NetworkDao(@Nonnull Scheduler networkScheduler) {
        final Response title = new Response("Title", ImmutableList.of(new Items("1", "title"), new Items("2", "title2")));
        // This normally would be retrofit
        final Observable<Response> apiCall = Observable.just(title)
                .delay(2, TimeUnit.SECONDS);

        data = apiCall
                .compose(ResponseOrError.<Response>toResponseOrErrorObservable())
                .compose(MoreOperators.<Response>repeatOnError(networkScheduler))
                .compose(MoreOperators.<ResponseOrError<Response>>refresh(refreshSubject))
                .compose(MoreOperators.<ResponseOrError<Response>>cacheWithTimeout(networkScheduler));
    }

    @Nonnull
    public Observable<ResponseOrError<Response>> getData() {
        return data;
    }

    @Nonnull
    public Observer<Object> refresh() {
        return refreshSubject;
    }
    
    public static class Items {
        @Nonnull
        private final String id;
        @Nullable
        private final String name;

        public Items(@Nonnull String id, 
                     @Nullable String name) {
            this.id = id;
            this.name = name;
        }

        @Nonnull
        public String id() {
            return id;
        }

        @Nullable
        public String name() {
            return name;
        }
    }
    
    public static class Response {
        @Nonnull
        private final String title;

        @Nonnull
        private final ImmutableList<Items> items;

        public Response(@Nonnull String title, 
                        @Nonnull ImmutableList<Items> items) {
            this.title = title;
            this.items = items;
        }

        @Nonnull
        public String title() {
            return title;
        }

        @Nonnull
        public ImmutableList<Items> items() {
            return items;
        }
    }
}
