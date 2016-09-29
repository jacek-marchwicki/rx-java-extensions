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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.observables.NetworkObservableProvider;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observers.Subscribers;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class NetworkObservableProviderImpl implements NetworkObservableProvider {

    @Nonnull
    private final ConnectivityManager connectivityManager;
    private final Observable<NetworkStatus> networkStatusObservable;

    public NetworkObservableProviderImpl(@Nonnull final Context context) {
        checkNotNull(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        networkStatusObservable = Observable
                .create(new Observable.OnSubscribe<NetworkStatus>() {
                    @Override
                    public void call(final Subscriber<? super NetworkStatus> subscriber) {
                        subscriber.onNext(getActiveNetworkStatus());

                        final BroadcastReceiver receiver = new NetworkBroadcastReceiver(subscriber);
                        context.registerReceiver(receiver, filter);

                        subscriber.add(Subscribers.create(new Action1<Object>() {
                            @Override
                            public void call(final Object o) {
                                context.unregisterReceiver(receiver);
                            }
                        }));
                    }
                })
                .distinctUntilChanged()
                .replay(1).refCount();
    }

    private NetworkStatus getActiveNetworkStatus() {
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return NetworkStatus.NO_NETWORK;
        }
        if (!activeNetworkInfo.isConnected()) {
            return NetworkStatus.NO_NETWORK;
        }

        final int type = activeNetworkInfo.getType();
        final int subtype = activeNetworkInfo.getSubtype();
        if (type == ConnectivityManager.TYPE_WIFI) {
            return NetworkStatus.BEST;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subtype) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return NetworkStatus.WEAK; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return NetworkStatus.WEAK; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return NetworkStatus.WEAK; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return NetworkStatus.WEAK; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return NetworkStatus.WEAK; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return NetworkStatus.WEAK; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return NetworkStatus.BEST; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return NetworkStatus.GOOD; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return NetworkStatus.BEST; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return NetworkStatus.GOOD; // ~ 400-7000 kbps
            /*
             * Above API level 7
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return NetworkStatus.GOOD; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return NetworkStatus.BEST; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return NetworkStatus.BEST; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return NetworkStatus.BEST; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return NetworkStatus.BEST; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return NetworkStatus.GOOD;
            }
        } else if (type == ConnectivityManager.TYPE_WIMAX) {
            return NetworkStatus.BEST;
        } else if (type == ConnectivityManager.TYPE_ETHERNET) {
            return NetworkStatus.BEST;
        } else {
            return NetworkStatus.GOOD;
        }

    }

    @Nonnull
    @Override
    public Observable<NetworkStatus> networkObservable() {
        return networkStatusObservable;
    }

    private class NetworkBroadcastReceiver extends BroadcastReceiver {
        private final Subscriber<? super NetworkStatus> subscriber;

        public NetworkBroadcastReceiver(final Subscriber<? super NetworkStatus> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            subscriber.onNext(getActiveNetworkStatus());
        }
    }
}
