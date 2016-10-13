/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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

package com.appunite.rx.example.ui.posts.item;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.example.dao.posts.PostsDao;
import com.appunite.rx.example.dao.posts.model.PostWithBody;
import com.appunite.rx.example.internal.Strings;
import com.appunite.rx.functions.Functions1;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

class DetailsPresenters {

    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Observable<ResponseOrError<String>> nameObservable;
    @Nonnull
    private final Observable<ResponseOrError<String>> bodyObservable;

    DetailsPresenters(@Nonnull @UiScheduler Scheduler uiScheduler,
                      @Nonnull PostsDao postsDao,
                             /* for dagger: @Named("post_id") */@Nonnull final String postId) {

        this.uiScheduler = uiScheduler;
        final PostsDao.PostDao postDao = postsDao.postDao(postId);

        final Observable<ResponseOrError<PostWithBody>> postWithBodyObservable = postDao.postWithBodyObservable()
                .observeOn(uiScheduler)
                .replay(1)
                .refCount();
        nameObservable = postWithBodyObservable
                .compose(ResponseOrError.map(new Func1<PostWithBody, String>() {
                    @Override
                    public String call(PostWithBody item) {
                        return Strings.nullToEmpty(item.name());
                    }
                }))
                .replay(1).refCount();

        bodyObservable = postWithBodyObservable
                .compose(ResponseOrError.map(new Func1<PostWithBody, String>() {
                    @Override
                    public String call(PostWithBody item) {
                        return Strings.nullToEmpty(item.body());
                    }
                }))
                .replay(1).refCount();
    }

    @Nonnull
    Observable<String> bodyObservable() {
        return bodyObservable
                .compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    Observable<String> titleObservable() {
        return nameObservable
                .compose(ResponseOrError.<String>onlySuccess());
    }

    @Nonnull
    Observable<Boolean> progressObservable() {
        return ResponseOrError.combineProgressObservable(Arrays.asList(
                ResponseOrError.transform(nameObservable),
                ResponseOrError.transform(bodyObservable)));
    }

    @Nonnull
    Observable<Throwable> errorObservable() {
        return ResponseOrError.combineErrorsObservable(Arrays.asList(
                ResponseOrError.transform(nameObservable),
                ResponseOrError.transform(bodyObservable)));
    }

    @Nonnull
    Observable<Object> startPostponedEnterTransitionObservable() {
        final Observable<Boolean> filter = progressObservable().filter(Functions1.isFalse());
        final Observable<Throwable> error = errorObservable().filter(Functions1.isNotNull());
        final Observable<String> timeout = Observable.just("").delay(500, TimeUnit.MILLISECONDS, uiScheduler);
        return Observable.<Object>amb(filter, error, timeout).first();
    }

}
