/**
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
package com.appunite.rx.android;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import rx.Scheduler;
import rx.schedulers.Schedulers;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Schedulers that have Android-specific functionality
 */
public final class MyAndroidNetworkSchedulers {

    private static final Scheduler NETWORK_SCHEDULER = Schedulers.from(Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
                    r.run();
                }
            }, "Rx-Network scheduler");
        }
    }));

    private MyAndroidNetworkSchedulers() {
        throw new AssertionError("No instances");
    }

    public static Scheduler networkScheduler() {
        return NETWORK_SCHEDULER;
    }
}
