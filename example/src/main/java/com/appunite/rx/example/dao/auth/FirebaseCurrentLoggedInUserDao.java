package com.appunite.rx.example.dao.auth;

import android.support.annotation.NonNull;

import com.appunite.rx.ResponseOrError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Objects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class FirebaseCurrentLoggedInUserDao implements MyCurrentLoggedInUserDao {

    private final Observable<ResponseOrError<LoggedInUserDao>> currentLoggedInUserObservable;


    private static class FirebaseLoggedInUserDao implements LoggedInUserDao {

        @Nonnull
        private final FirebaseUser firebaseUser;

        FirebaseLoggedInUserDao(@Nonnull FirebaseUser firebaseUser) {
            this.firebaseUser = firebaseUser;
        }

        @Nonnull
        @Override
        public String userId() {
            return firebaseUser.getUid();
        }

        @Nonnull
        @Override
        public Observable<String> authTokenObservable(final boolean forceRefresh) {
            return Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(final Subscriber<? super String> subscriber) {
                    final Task<GetTokenResult> token = firebaseUser.getToken(forceRefresh);
                    final OnCompleteListener<GetTokenResult> listener = new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }
                            final Exception exception = task.getException();
                            if (exception != null) {
                                subscriber.onError(exception);
                            } else {
                                subscriber.onNext(task.getResult().getToken());
                                subscriber.onCompleted();
                            }
                        }
                    };
                    token.addOnCompleteListener(listener);
                }
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FirebaseLoggedInUserDao)) return false;
            FirebaseLoggedInUserDao that = (FirebaseLoggedInUserDao) o;
            return Objects.equal(firebaseUser.getUid(), that.firebaseUser.getUid());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(firebaseUser.getUid());
        }
    }

    public FirebaseCurrentLoggedInUserDao() {
        currentLoggedInUserObservable = Observable
                .create(new Observable.OnSubscribe<ResponseOrError<LoggedInUserDao>>() {
                    @Override
                    public void call(final Subscriber<? super ResponseOrError<LoggedInUserDao>> subscriber) {
                        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        final AtomicReference<Object> previous = new AtomicReference<Object>("Wrong");
                        final FirebaseAuth.AuthStateListener listener = new FirebaseAuth.AuthStateListener() {
                            @Override
                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                final Object old = previous.get();
                                if (old == currentUser) {
                                    return;
                                }
                                previous.set(currentUser);
                                if (currentUser == null) {
                                    subscriber.onNext(ResponseOrError.<LoggedInUserDao>fromError(new NotAuthorizedException("Not yet authorized")));
                                } else {
                                    subscriber.onNext(ResponseOrError.<LoggedInUserDao>fromData(new FirebaseLoggedInUserDao(currentUser)));
                                }
                            }
                        };
                        firebaseAuth.addAuthStateListener(listener);

                        subscriber.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                firebaseAuth.removeAuthStateListener(listener);
                            }
                        }));
                    }
                })
                .onBackpressureLatest()
                .replay(1)
                .refCount();
    }

    @Nonnull
    @Override
    public Observable<ResponseOrError<LoggedInUserDao>> currentLoggedInUserObservable() {
        return currentLoggedInUserObservable;
    }
}
