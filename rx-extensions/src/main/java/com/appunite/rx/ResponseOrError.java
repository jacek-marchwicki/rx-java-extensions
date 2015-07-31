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

package com.appunite.rx;

import com.appunite.rx.functions.Functions1;
import com.appunite.rx.functions.FunctionsN;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Class that represents data or error
 * @param <T> type of data
 */
public class ResponseOrError<T> {
    @Nullable
    private final T data;
    @Nullable
    private final Throwable error;

    private ResponseOrError(@Nullable T data, @Nullable Throwable error) {
        checkArgument(data != null ^ error != null);
        this.data = data;
        this.error = error;
    }

    /**
     * Returns error response from throwable
     * @param t throwable
     * @param <T> type of possible data
     * @return representation of throwable
     */
    public static <T> ResponseOrError<T> fromError(@Nonnull Throwable t) {
        return new ResponseOrError<>(null, checkNotNull(t));
    }

    /**
     * Returns data response from data
     * @param data data
     * @param <T> type of data
     * @return representation of data
     */
    public static <T> ResponseOrError<T> fromData(@Nonnull T data) {
        return new ResponseOrError<>(checkNotNull(data), null);
    }

    /**
     * Returns data
     *
     * You need to check {@link #isData()}
     *
     * @return data
     * @throws java.lang.IllegalStateException when response is error
     */
    @Nonnull
    public T data() {
        checkState(data != null);
        assert data != null;
        return data;
    }

    /**
     * Returns if response contains data
     * @return true if contains data
     */
    public boolean isData() {
        return data != null;
    }

    /**
     * Returns if response contains error
     *
     * Helper for negation of {@link #isData()}
     * @return true if contains error
     */
    public boolean isError() {
        return !isData();
    }

    /**
     * Returns error
     *
     * You need to check {@link #isError()}
     * @return error throwable
     * @throws java.lang.IllegalStateException when response is data
     */
    @Nonnull
    public Throwable error() {
        checkState(error != null);
        assert error != null;
        return error;
    }

    /**
     * Function that converts ResponseOrError to Boolean containing {@link #isData()}
     * @return function
     */
    @Nonnull
    public static Func1<? super ResponseOrError<?>, Boolean> funcIsData() {
        return new Func1<ResponseOrError<?>, Boolean>() {
            @Override
            public Boolean call(ResponseOrError<?> response) {
                return response.isData();
            }
        };
    }

    /**
     * Function that converts ResponseOrError to Boolean containing {@link #isError()} ()}
     * @return function
     */
    @Nonnull
    public static Func1<? super ResponseOrError<?>, Boolean> funcIsError() {
        return Functions1.neg(funcIsData());
    }

    /**
     * Function that converts ResponseOrError to null if {@link #isData()} or {@link #error()} Throwable
     * @return function
     */
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

    /**
     * Parameters are equals using {@link Object#equals(Object)} on data or error
     * @param o object
     * @return true if equals
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseOrError)) return false;

        final ResponseOrError that = (ResponseOrError) o;

        return !(data != null ? !data.equals(that.data) : that.data != null)
                && !(error != null ? !error.equals(that.error) : that.error != null);

    }
    /**
     * Hash code using {@link Object#hashCode()} on data or error
     * @return hash code
     */
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

    /**
     * Convert {@link Observable} that can throw error to {@link Observable<ResponseOrError>}
     *
     * If source Observable returns obj, result observable will return ResponseOrError.fromData(obj)
     * If source Observable throws err, result observable will return ResponseOrError.fromError(err)
     *
     * @param <T> type of data
     * @return observable
     */
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

    /**
     * Map only success response ignoring error
     *
     * <pre>
     * {@code
     *  Observable<ResponseOrError<Boolean>> output =
     *      Observable.just(ResponseOrError.fromData("text")
     *      .compose(ResponseOrError.map(new Func<String, Boolean) {
     *          Boolean call(String in) {
     *             return in != null;
     *          }
     *      });
     * }
     * </pre>
     *
     * @param func that maps data of ResponseOrError to another data
     * @param <T> type of source object
     * @param <K> type of destination object
     * @return observable
     */
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
    /**
     * Flat map only success response ignoring error
     *
     * <pre>
     * {@code
     *  Observable<ResponseOrError<Boolean>> output =
     *      Observable.just(ResponseOrError.fromData("text")
     *      .compose(ResponseOrError.map(new Func<String, Observable<Boolean>) {
     *          Observable<Boolean> call(String in) {
     *             return Observable.just(in != null);
     *          }
     *      });
     * }
     * </pre>
     *
     * @param func that maps data of ResponseOrError to Observable
     * @param <T> type of source object
     * @param <K> type of destination object
     * @return observable
     */
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

    /**
     * Switch map only success response ignoring error
     *
     * <pre>
     * {@code
     *  Observable<ResponseOrError<Boolean>> output =
     *      Observable.just(ResponseOrError.fromData("text")
     *      .compose(ResponseOrError.switchMap(new Func<String, Observable<Boolean>) {
     *          Observable<Boolean> call(String in) {
     *             return Observable.just(in != null);
     *          }
     *      });
     * }
     * </pre>
     *
     * @param func that maps data of ResponseOrError to Observable
     * @param <T> type of source object
     * @param <K> type of destination object
     * @return observable
     */
    @Nonnull
    public static <T, K> Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>> switchMap(@Nonnull final Func1<T, Observable<ResponseOrError<K>>> func) {
        return new Observable.Transformer<ResponseOrError<T>, ResponseOrError<K>>() {
            @Override
            public Observable<ResponseOrError<K>> call(final Observable<ResponseOrError<T>> observableObservable) {
                return switchMap(observableObservable, func);
            }
        };
    }

    @Nonnull
    private static <T, K> Observable<ResponseOrError<K>> switchMap(@Nonnull final Observable<ResponseOrError<T>> observable,
                                                                 @Nonnull final Func1<T, Observable<ResponseOrError<K>>> func) {
        checkNotNull(observable);
        checkNotNull(func);
        return observable.switchMap(new Func1<ResponseOrError<T>, Observable<ResponseOrError<K>>>() {
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

    /**
     * Returns only success response of observable of {@link ResponseOrError} and convert to {@link #data()}
     * @param <T> type ResponseOrError
     * @return observable
     */
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

    /**
     * Returns only error response of observable of {@link ResponseOrError} and convert to {@link #error()}
     * @param <T> type of ResponseOrError
     * @return observable
     */
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

    /**
     * Converts lists of ResponseOrError to ResponseOrError with list
     *
     * If there will be error only first will be returned
     * @param <T> type of element
     * @return observable
     */
    @Nonnull
    public static <T> Observable.Transformer<List<ResponseOrError<T>>, ResponseOrError<List<T>>> newFromListObservable() {
        return new Observable.Transformer<List<ResponseOrError<T>>, ResponseOrError<List<T>>>() {
            @Override
            public Observable<ResponseOrError<List<T>>> call(final Observable<List<ResponseOrError<T>>> observable) {
                return fromListObservable(observable);
            }
        };
    }

    /**
     * Use {@link #newFromListObservable()}
     */
    @Deprecated
    @Nonnull
    public static <T> Observable.Transformer<ImmutableList<ResponseOrError<T>>, ResponseOrError<ImmutableList<T>>> fromListObservable() {
        return new Observable.Transformer<ImmutableList<ResponseOrError<T>>, ResponseOrError<ImmutableList<T>>>() {
            @Override
            public Observable<ResponseOrError<ImmutableList<T>>> call(final Observable<ImmutableList<ResponseOrError<T>>> observable) {
                return observable
                        .map(new Func1<ImmutableList<ResponseOrError<T>>, List<ResponseOrError<T>>>() {
                            @Override
                            public List<ResponseOrError<T>> call(ImmutableList<ResponseOrError<T>> responseOrErrors) {
                                return responseOrErrors;
                            }
                        })
                        .compose(ResponseOrError.<T>newFromListObservable())
                        .compose(ResponseOrError.map(new Func1<List<T>, ImmutableList<T>>() {
                            @Override
                            public ImmutableList<T> call(List<T> ts) {
                                return ImmutableList.copyOf(ts);
                            }
                        }));
            }
        };
    }

    @Nonnull
    private static <T> Observable<ResponseOrError<List<T>>> fromListObservable(
            @Nonnull final Observable<List<ResponseOrError<T>>> observable) {
        return observable.map(new Func1<List<ResponseOrError<T>>, ResponseOrError<List<T>>>() {
            @Override
            public ResponseOrError<List<T>> call(final List<ResponseOrError<T>> responses) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                for (ResponseOrError<T> response : responses) {
                    if (response.isError()) {
                        return ResponseOrError.fromError(response.error());
                    }
                    builder.add(response.data());
                }
                return ResponseOrError.fromData((List<T>)builder.build());
            }
        });
    }

    /**
     * Returns true unit all observables will returns some value
     *
     * @param observables observables that returns some data
     * @return observable
     */
    @Nonnull
    public static Observable<Boolean> combineProgressObservable(@Nonnull List<Observable<ResponseOrError<?>>> observables) {
        return Observable.combineLatest(observables, FunctionsN.returnFalse())
                .startWith(true);
    }

    /**
     * Converts {@code ResponseOrError<T>} to {@code ResponseOrError<?>}
     *
     * @param observable to transform
     * @param <T> type of source observable
     * @return observable
     * @see #combineErrorsObservable(List)
     * @see #combineProgressObservable(List)
     */
    @Nonnull
    public static <T> Observable<ResponseOrError<?>> transform(@Nonnull Observable<ResponseOrError<T>> observable) {
        return observable
                .map(new Func1<ResponseOrError<T>, ResponseOrError<?>>() {
                    @Override
                    public ResponseOrError<?> call(ResponseOrError<T> tResponseOrError) {
                        return tResponseOrError;
                    }
                });
    }

    /**
     * Returns throwable if some observable returned {@link #error()} otherwise null
     *
     * @param observables source observables
     * @return observable
     */
    @Nonnull
    public static Observable<Throwable> combineErrorsObservable(@Nonnull List<Observable<ResponseOrError<?>>> observables) {
        final ImmutableList<Observable<Throwable>> ob = FluentIterable
                .from(observables)
                .transform(new Function<Observable<ResponseOrError<?>>, Observable<Throwable>>() {
                    @Nonnull
                    @Override
                    public Observable<Throwable> apply(Observable<ResponseOrError<?>> input) {
                        return input.map(ResponseOrError.toNullableThrowable()).startWith((Throwable) null);
                    }
                })
                .toList();
        return Observable.combineLatest(ob,
                FunctionsN.combineFirstThrowable());
    }

    @Nonnull
    public static <T1, T2, R> Func2<ResponseOrError<T1>, T2, ResponseOrError<R>> toErrorFunc2(@Nonnull final Func2<T1, T2, R> func) {
        return new Func2<ResponseOrError<T1>, T2, ResponseOrError<R>>() {
            @Override
            public ResponseOrError<R> call(ResponseOrError<T1> t1ResponseOrError, T2 t2) {
                if (t1ResponseOrError.isData()) {
                    return ResponseOrError.fromData(func.call(t1ResponseOrError.data(), t2));
                } else {
                    return ResponseOrError.fromError(t1ResponseOrError.error());
                }
            }
        };
    }

    @Nonnull
    public static <T1, T2, T3, R> Func3<ResponseOrError<T1>, ResponseOrError<T2>, T3, ResponseOrError<R>> toErrorFunc3(@Nonnull final Func3<T1, T2, T3, R> func) {
        return new Func3<ResponseOrError<T1>, ResponseOrError<T2>, T3, ResponseOrError<R>>() {
            @Override
            public ResponseOrError<R> call(ResponseOrError<T1> t1ResponseOrError,
                                           ResponseOrError<T2> t2ResponseOrError, T3 t3) {
                if (t1ResponseOrError.isData() && t2ResponseOrError.isData()) {
                    return ResponseOrError.fromData(func.call(t1ResponseOrError.data(), t2ResponseOrError.data, t3));
                } else if (t1ResponseOrError.isError()){
                    return ResponseOrError.fromError(t1ResponseOrError.error());
                } else {
                    return ResponseOrError.fromError(t2ResponseOrError.error());
                }
            }
        };
    }
}
