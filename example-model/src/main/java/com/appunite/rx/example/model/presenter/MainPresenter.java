package com.appunite.rx.example.model.presenter;

import com.appunite.detector.SimpleDetector;
import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.FuncN;

public class MainPresenter {

    @Nonnull
    private final Observable<ResponseOrError<String>> compose;
    @Nonnull
    private final Observable<ResponseOrError<ImmutableList<AdapterItem>>> items;

    public MainPresenter(@Nonnull Scheduler networkScheduler,
                         @Nonnull Scheduler uiScheduler,
                         @Nonnull NetworkDao networkDao) {
        compose = networkDao.getData()
                .compose(ResponseOrError.map(new Func1<NetworkDao.Response, String>() {
                    @Override
                    public String call(NetworkDao.Response response) {
                        return response.title();
                    }
                }))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        items = networkDao.getData()
                .compose(ResponseOrError.map(new Func1<NetworkDao.Response, ImmutableList<AdapterItem>>() {
                    @Override
                    public ImmutableList<AdapterItem> call(NetworkDao.Response response) {
                        final ImmutableList<NetworkDao.Items> items = response.items();
                        return FluentIterable.from(items).transform(new Function<NetworkDao.Items, AdapterItem>() {
                            @Nonnull
                            @Override
                            public AdapterItem apply(NetworkDao.Items input) {
                                return new AdapterItem(input.id(), input.name());
                            }
                        }).toList();
                    }
                }))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ImmutableList<AdapterItem>>>behaviorRefCount());
    }

    public Observable<String> titleObservable() {
        return compose.compose(ResponseOrError.<String>onlySuccess());
    }

    public Observable<ImmutableList<AdapterItem>> items() {
        return items.compose(ResponseOrError.<ImmutableList<AdapterItem>>onlySuccess());
    }

    public Observable<Throwable> error() {
        return Observable.combineLatest(Arrays.asList(
                        items.map(ResponseOrError.toNullableThrowable()).startWith((Throwable) null),
                        compose.map(ResponseOrError.toNullableThrowable()).startWith((Throwable) null)
                ),
                combineFirstThrowable())
                .startWith((Throwable) null)
                .distinctUntilChanged();
    }

    private FuncN<Throwable> combineFirstThrowable() {
        return new FuncN<Throwable>() {
            @Override
            public Throwable call(final Object... args) {
                for (Object arg : args) {
                    final Throwable throwable = (Throwable) arg;
                    if (throwable != null) {
                        return throwable;
                    }
                }
                return null;
            }
        };
    }

    public Observable<Boolean> progress() {
        return Observable.combineLatest(compose, items,
                new Func2<ResponseOrError<String>, ResponseOrError<ImmutableList<AdapterItem>>, Boolean>() {
                    @Override
                    public Boolean call(ResponseOrError<String> stringResponseOrError, ResponseOrError<ImmutableList<AdapterItem>> immutableListResponseOrError) {
                        return false;
                    }
                })
                .startWith(true);
    }

    public static class AdapterItem implements SimpleDetector.Detectable<AdapterItem> {

        @Nonnull
        private final String id;
        @Nullable
        private final String text;

        public AdapterItem(@Nonnull String id,
                           @Nullable String text) {
            this.id = id;
            this.text = text;
        }

        @Nullable
        public String text() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AdapterItem)) return false;

            final AdapterItem that = (AdapterItem) o;

            return id.equals(that.id) && !(text != null ? !text.equals(that.text) : that.text != null);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + (text != null ? text.hashCode() : 0);
            return result;
        }

        @Override
        public boolean matches(@Nonnull AdapterItem item) {
            return Objects.equal(id, item.id);
        }

        @Override
        public boolean same(@Nonnull AdapterItem item) {
            return equals(item);
        }
    }
}
