package com.appunite.rx.android;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;
import rx.subjects.BehaviorSubject;

public class AdapterLifecycleObservable {

    final static class OperatorSubscribeUntil<T, Boolean> implements Observable.Operator<T, T> {

        private final Observable<Boolean> other;

        public OperatorSubscribeUntil(final Observable<Boolean> other) {
            this.other = other;
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> child) {
            final Subscriber<T> parent = new SerializedSubscriber<>(child);

            other.unsafeSubscribe(new Subscriber<Boolean>(child) {

                @Override
                public void onCompleted() {
                    parent.unsubscribe();
                }

                @Override
                public void onError(Throwable e) {
                    parent.onError(e);
                }

                @Override
                public void onNext(Boolean t) {
                    if (t.equals(false)) {
                        parent.unsubscribe();
                    }
                }

            });

            return parent;
        }
    }
    @Nonnull
    private final BehaviorSubject<Boolean> lifecycleSubject = BehaviorSubject.create();

    public AdapterLifecycleObservable() {
    }

    @Nonnull
    public <T> Observable<T> bind(@Nonnull Observable<T> source) {
        lifecycleSubject.onNext(true);
        return source.lift(new OperatorSubscribeUntil<T, Boolean>(lifecycleSubject));
    }

    public void unbind() {
        lifecycleSubject.onNext(false);
    }
}
