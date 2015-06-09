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

package com.appunite.rx.observables;

import javax.annotation.Nonnull;

import rx.Observable;

public interface NetworkObservableProvider {

    enum NetworkStatus {
        NO_NETWORK(0),
        WEAK(1),
        GOOD(2),
        BEST(3);

        private final int pos;

        NetworkStatus(int pos) {
            this.pos = pos;
        }

        public boolean isNetwork() {
            return pos > 0;
        }

        public boolean weakOrBetter() {
            return pos >= WEAK.pos;
        }

        public boolean goodOrBetter() {
            return pos >= GOOD.pos;
        }

        public boolean best() {
            return pos >= BEST.pos;
        }
    }

    @Nonnull
    Observable<NetworkStatus> networkObservable();
}
