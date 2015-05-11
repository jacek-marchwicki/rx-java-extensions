package com.appunite.rx.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.appunite.rx.observables.NetworkObservableProvider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NetworkObservableProviderImplRealTest {

    private NetworkObservableProvider networkObservableProvider;

    @Before
    public void setUp() throws Exception {
        final Context targetContext = InstrumentationRegistry.getTargetContext();
        networkObservableProvider = new NetworkObservableProviderImpl(targetContext);
    }

    @Ignore
    @Test
    public void testNetworkStatusManually() throws Exception {
        final Subscription subscription = networkObservableProvider
                .networkObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<NetworkObservableProvider.NetworkStatus>() {
                    @Override
                    public void call(NetworkObservableProvider.NetworkStatus networkStatus) {
                        System.out.println("Network status: " + networkStatus);
                    }
                });
        Thread.sleep(10*60*1000);
        subscription.unsubscribe();
    }
}