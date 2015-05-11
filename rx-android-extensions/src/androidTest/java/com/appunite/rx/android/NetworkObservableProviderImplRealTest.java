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