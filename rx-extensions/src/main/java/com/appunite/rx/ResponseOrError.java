package com.appunite.rx;

import com.appunite.rx.functions.Functions1;
import com.appunite.rx.functions.FunctionsN;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ResponseOrError<T> {
    @Nullable
    private final T data;
    @Nullable
    private final Throwable error;

    public ResponseOrError(@Nullable T data, @Nullable Throwable error) {
        checkArgument(data != null ^ error != null);
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseOrError<T> fromError(@Nonnull Throwable t) {
        return new ResponseOrError<>(null, checkNotNull(t));
    }

    public static <T> ResponseOrError<T> fromData(@Nonnull T data) {
        return new ResponseOrError<>(checkNotNull(data), null);
    }

    @Nonnull
    public T data() {
        checkState(data != null);
        assert data != null;
        return data;
    }

    public boolean isData() {
        return data != null;
    }

    public boolean isError() {
        return !isData();
    }

    @Nonnull
    public Throwable error() {
        checkState(error != null);
        assert error != null;
        return error;
    }

    @Nonnull
    public static Func1<? super ResponseOrError<?>, Boolean> funcIsData() {
        return new Func1<ResponseOrError<?>, Boolean>() {
            @Override
            public Boolean call(ResponseOrError<?> response) {
                return response.isData();
            }
        };
    }

    @Nonnull
    public static Func1<? super ResponseOrError<?>, Boolean> funcIsError() {
        return Functions1.neg(funcIsData());
    }


    @Nonnull
    public static Func1<? super ResponseOrError<?>, Throwable> toNullableThrowable() {
        return new Func1<ResponseOrError<?>, Throwable>() {
            @Override
            @Nullable
            public Throwable call(final ResponseOrError<?> responseOrError) {
                //noinspection ThrowableResultOfMethodCallIgnored
                return responseOrError.isData() ? null : responseOrError.error();
            }
        };
    }

    public <W> ResponseOrError<W> replaceIfDataWith(final W element) {
        return isData()
                ? ResponseOrError.fromData(element)
                : ResponseOrError.<W>fromError(error());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseOrError)) return false;

        final ResponseOrError that = (ResponseOrError) o;

        return !(data != null ? !data.equals(that.data) : that.data != null)
                && !(error != null ? !error.equals(that.error) : that.error != null);

    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("data", data)
                .add("error", error)
                .toString();
    }

    @Nonnull
    public static <T> Observable.Transformer<T, ResponseOrError<T>> toResponseOrErrorObservable() {
        return new Observable.Transformer<T, ResponseOrError<T>>() {

            @Override
            public Observable<ResponseOrError<T>> call(final Observable<T> observable) {
                return toResponseOrErrorObservable(observable);
            }
        };
    }

    @Nonnull
    private static <T> Observable<ResponseOrError<T>> toResponseOrErrorObservable(@Nonnull Observable<T> observable) {
        return observable
                .map(new Func1<T, ResponseOrError<T>>() {
                    @Override
                    public ResponseOrError<T> call(final T t) {
                        return ResponseOrError.fromData(t);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseOrError<T>>>() {
                    @Override
                    public Observable<? extends ResponseOrError<T>> call(final Throwable throwable) {
                        return Observable.just(ResponseOrError.<T>fromError(throwable));
                    }
                });

    }

    @Nonnull
    public static <T, K> Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>> map(@Nonnull final Func1<T, K> func) {
        return new Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>>() {
            @Override
            public Observable<ResponseOrError<K>> call(final Observable<ResponseOrError<T>> observable) {
                return map(observable, func);
            }
        };
    }

    @Nonnull
    private static <T, K> Observable<ResponseOrError<K>> map(@Nonnull final Observable<ResponseOrError<T>> observable,
                                                            @Nonnull final Func1<T, K> func) {
        checkNotNull(observable);
        checkNotNull(func);
        return observable.map(new Func1<ResponseOrError<T>, ResponseOrError<K>>() {
            @Override
            public ResponseOrError<K> call(final ResponseOrError<T> response) {
                if (response.isError()) {
                    return ResponseOrError.fromError(response.error());
                } else {
                    return ResponseOrError.fromData(func.call(response.data()));
                }
            }
        });
    }

    @Nonnull
    public static <T, K> Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>> flatMap(@Nonnull final Func1<T, Observable<ResponseOrError<K>>> func) {
        return new Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>>() {
            @Override
            public Observable<ResponseOrError<K>> call(final Observable<ResponseOrError<T>> observableObservable) {
                return flatMap(observableObservable, func);
            }
        };
    }

    @Nonnull
    private static <T, K> Observable<ResponseOrError<K>> flatMap(@Nonnull final Observable<ResponseOrError<T>> observable,
                                                            @Nonnull final Func1<T, Observable<ResponseOrError<K>>> func) {
        checkNotNull(observable);
        checkNotNull(func);
        return observable.flatMap(new Func1<ResponseOrError<T>, Observable<ResponseOrError<K>>>() {
            @Override
            public Observable<ResponseOrError<K>> call(final ResponseOrError<T> response) {
                if (response.isError()) {
                    return Observable.just(ResponseOrError.<K>fromError(response.error()));
                } else {
                    return func.call(response.data());
                }
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<ResponseOrError<T>, T> onlySuccess() {
        return new Observable.Transformer<ResponseOrError<T>, T>() {
            @Override
            public Observable<T> call(final Observable<ResponseOrError<T>> observable) {
                return onlySuccess(observable);
            }
        };
    }

    @Nonnull
    private static <T> Observable<T> onlySuccess(@Nonnull final Observable<ResponseOrError<T>> observable) {
        return observable.filter(funcIsData()).map(new Func1<ResponseOrError<T>, T>() {
            @Override
            public T call(final ResponseOrError<T> response) {
                return response.data();
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<ResponseOrError<T>, Throwable> onlyError() {
        return new Observable.Transformer<ResponseOrError<T>, Throwable>() {
            @Override
            public Observable<Throwable> call(final Observable<ResponseOrError<T>> observable) {
                return onlyError(observable);
            }
        };
    }

    @Nonnull
    private static <T> Observable<Throwable> onlyError(@Nonnull final Observable<ResponseOrError<T>> observable) {
        return observable.filter(funcIsError()).map(new Func1<ResponseOrError<?>, Throwable>() {
            @Override
            public Throwable call(final ResponseOrError<?> responseOrError) {
                return responseOrError.error();
            }
        });
    }

    @Nonnull
    public static <T> Observable.Transformer<ImmutableList<ResponseOrError<T>>, ResponseOrError<ImmutableList<T>>> fromListObservable() {
        return new Observable.Transformer<ImmutableList<ResponseOrError<T>>, ResponseOrError<ImmutableList<T>>>() {
            @Override
            public Observable<ResponseOrError<ImmutableList<T>>> call(final Observable<ImmutableList<ResponseOrError<T>>> observable) {
                return fromListObservable(observable);
            }
        };
    }

    @Nonnull
    private static <T> Observable<ResponseOrError<ImmutableList<T>>> fromListObservable(
            @Nonnull final Observable<ImmutableList<ResponseOrError<T>>> observable) {
        return observable.map(new Func1<ImmutableList<ResponseOrError<T>>, ResponseOrError<ImmutableList<T>>>() {
            @Override
            public ResponseOrError<ImmutableList<T>> call(final ImmutableList<ResponseOrError<T>> responses) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                for (ResponseOrError<T> response : responses) {
                    if (response.isError()) {
                        return ResponseOrError.fromError(response.error());
                    }
                    builder.add(response.data());
                }
                return ResponseOrError.fromData(builder.build());
            }
        });
    }

    public static Observable<Boolean> progressObservable(List<Observable<ResponseOrError<?>>> observables) {
        return Observable.combineLatest(observables, FunctionsN.returnFalse())
                .startWith(true);
    }
}
