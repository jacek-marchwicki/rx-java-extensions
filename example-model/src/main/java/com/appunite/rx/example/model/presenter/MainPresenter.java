package com.appunite.rx.example.model.presenter;

import com.appunite.detector.SimpleDetector;
import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.ItemsDao;
import com.appunite.rx.example.model.model.Item;
import com.appunite.rx.example.model.model.Response;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class MainPresenter {

    @Nonnull
    private final Observable<ResponseOrError<String>> titleObservable;
    @Nonnull
    private final Observable<ResponseOrError<ImmutableList<AdapterItem>>> itemsObservable;
    @Nonnull
    private final Subject<AdapterItem, AdapterItem> openDetailsSubject = PublishSubject.create();
    @Nonnull
    private final ItemsDao itemsDao;

    public MainPresenter(@Nonnull Scheduler networkScheduler,
                         @Nonnull Scheduler uiScheduler,
                         @Nonnull ItemsDao itemsDao) {
        this.itemsDao = itemsDao;
        titleObservable = itemsDao.dataObservable()
                .compose(ResponseOrError.map(new Func1<Response, String>() {
                    @Override
                    public String call(Response response) {
                        return response.title();
                    }
                }))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());

        itemsObservable = itemsDao.dataObservable()
                .compose(ResponseOrError.map(new Func1<Response, ImmutableList<AdapterItem>>() {
                    @Override
                    public ImmutableList<AdapterItem> call(Response response) {
                        final ImmutableList<Item> items = response.items();
                        return FluentIterable.from(items).transform(new Function<Item, AdapterItem>() {
                            @Nonnull
                            @Override
                            public AdapterItem apply(Item input) {
                                return new AdapterItem(input.id(), input.name());
                            }
                        }).toList();
                    }
                }))
                .subscribeOn(networkScheduler)
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<ResponseOrError<ImmutableList<AdapterItem>>>behaviorRefCount());
    }

    @Nonnull
    public Observable<AdapterItem> openDetailsObservable() {
        return openDetailsSubject;
    }

    @Nonnull
    public Observable<String> titleObservable() {
        return titleObservable.compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    public Observable<ImmutableList<AdapterItem>> itemsObservable() {
        return itemsObservable.compose(ResponseOrError.<ImmutableList<AdapterItem>>onlySuccess());
    }

    @Nonnull
    public Observable<Throwable> errorObservable() {
        return ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)));

    }

    @Nonnull
    public Observable<Boolean> progressObservable() {
        return ResponseOrError.combineProgressObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)));
    }

    @Nonnull
    public Observer<Object> loadMoreObserver() {
        return itemsDao.loadMoreObserver();
    }

    public class AdapterItem implements SimpleDetector.Detectable<AdapterItem> {

        @Nonnull
        private final String id;
        @Nullable
        private final String text;

        public AdapterItem(@Nonnull String id,
                           @Nullable String text) {
            this.id = id;
            this.text = text;
        }

        @Nonnull
        public String id() {
            return id;
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

        @Nonnull
        public Observer<Object> clickObserver() {
            return Observers.create(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    openDetailsSubject.onNext(AdapterItem.this);
                }
            });
        }
    }
}
