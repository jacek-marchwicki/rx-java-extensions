/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appunite.rx.android.internal;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import rx.Subscription;

public abstract class MainThreadSubscription implements Subscription, Runnable {
    private static final Handler mainThread = new Handler(Looper.getMainLooper());

    @Keep
    @SuppressWarnings("unused") // Updated by 'unsubscribedUpdater' object.
    private volatile int unsubscribed;
    private static final AtomicIntegerFieldUpdater<MainThreadSubscription> unsubscribedUpdater =
            AtomicIntegerFieldUpdater.newUpdater(MainThreadSubscription.class, "unsubscribed");

    @Override
    public final boolean isUnsubscribed() {
        return unsubscribed != 0;
    }

    @Override
    public final void unsubscribe() {
        if (unsubscribedUpdater.compareAndSet(this, 0, 1)) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                onUnsubscribe();
            } else {
                mainThread.post(this);
            }
        }
    }

    @Override
    public final void run() {
        onUnsubscribe();
    }

    protected abstract void onUnsubscribe();
}