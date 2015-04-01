package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostWithBody;
import com.appunite.rx.example.model.model.PostsResponse;
import com.appunite.rx.example.model.model.PostsResponse;
import com.google.common.base.Function;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class GuestbookServiceFake implements GuestbookService {

    @Nonnull
    @Override
    public Observable<PostsResponse> listItems(@Nullable final String nextToken) {
        return Observable
                .create(new Observable.OnSubscribe<PostsResponse>() {
                    public static final int ITEMS_AT_ONCE = 100;
                    public static final int MAX_ITEMS = 500;

                    @Override
                    public void call(Subscriber<? super PostsResponse> subscriber) {
                        try {
                            final int start = nextToken == null ? 1 : Integer.parseInt(nextToken);
                            final String next;
                            final int end;

                            if (start > MAX_ITEMS) {
                                end = MAX_ITEMS + ITEMS_AT_ONCE / 2;
                                next = null;
                            } else {
                                end = start + ITEMS_AT_ONCE;
                                next = String.valueOf(end);
                            }

                            subscriber.onNext(new PostsResponse("Title", getItemsBetween(start, end), next));
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }

                    private ImmutableList<Post> getItemsBetween(int start, int end) {
                        final Range<Integer> integerRange = Range.closedOpen(start, end);
                        return FluentIterable
                                .from(ContiguousSet.create(integerRange, DiscreteDomain.integers()))
                                .transform(new Function<Integer, Post>() {
                                    @Nullable
                                    @Override
                                    public Post apply(Integer input) {
                                        return new Post(String.valueOf(input), String.format("title - %d", input));
                                    }
                                })
                                .toList();
                    }
                })
                .delay(2, TimeUnit.SECONDS);
    }

    @Nonnull
    @Override
    public Observable<PostWithBody> getItem(@Nonnull String id) {
        switch (id) {
            case "1":
                return Observable.just(new PostWithBody("1", "title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac diam non augue consequat faucibus. Etiam turpis felis, elementum nec laoreet in, commodo nec ligula. Maecenas luctus leo eget laoreet tempor. Quisque sed elit nec urna aliquam commodo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nam a ante massa. Nunc tincidunt arcu lorem, eget pharetra tortor venenatis sodales. Vestibulum et blandit mauris."))
                        .delay(200, TimeUnit.MILLISECONDS);
            case "2":
                return Observable.just(new PostWithBody("2", "title2", "Phasellus placerat ligula erat, nec pellentesque libero rhoncus nec. In euismod leo porttitor sem pulvinar, eu venenatis nisi scelerisque. Ut efficitur fermentum massa a egestas. Morbi at tempus risus, id blandit turpis. Suspendisse sed magna et mauris tristique iaculis nec vel velit. Cras porta diam vitae velit cursus, bibendum venenatis nulla egestas. Aenean molestie magna rutrum nisl aliquet, sit amet lacinia orci feugiat. Quisque placerat quam vitae ultrices cursus."))
                        .delay(1000, TimeUnit.MILLISECONDS);
            default:
                return Observable.just("")
                        .delay(2, TimeUnit.SECONDS)
                        .flatMap(new Func1<String, Observable<PostWithBody>>() {
                            @Override
                            public Observable<PostWithBody> call(String s) {
                                return Observable.error(new IOException("error"));
                            }
                        });
        }
    }
}
