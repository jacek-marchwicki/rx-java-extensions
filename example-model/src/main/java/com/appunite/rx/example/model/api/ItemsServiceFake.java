package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.model.Item;
import com.appunite.rx.example.model.model.ItemWithBody;
import com.appunite.rx.example.model.model.Response;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.functions.Func1;

public class ItemsServiceFake implements ItemsService {

    @Nonnull
    @Override
    public Observable<Response> listItems() {
        // This normally would be retrofit
        return Observable.just(new Response("Title", ImmutableList.of(
                new Item("1", "title"),
                new Item("2", "title2"),
                new Item("3", "not_existing_item")
                )))
                .delay(2, TimeUnit.SECONDS);
    }

    @Nonnull
    @Override
    public Observable<ItemWithBody> getItem(@Nonnull String id) {
        switch (id) {
            case "1":
                return Observable.just(new ItemWithBody("1", "title", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse ac diam non augue consequat faucibus. Etiam turpis felis, elementum nec laoreet in, commodo nec ligula. Maecenas luctus leo eget laoreet tempor. Quisque sed elit nec urna aliquam commodo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Nam a ante massa. Nunc tincidunt arcu lorem, eget pharetra tortor venenatis sodales. Vestibulum et blandit mauris."))
                        .delay(200, TimeUnit.MILLISECONDS);
            case "2":
                return Observable.just(new ItemWithBody("2", "title2", "Phasellus placerat ligula erat, nec pellentesque libero rhoncus nec. In euismod leo porttitor sem pulvinar, eu venenatis nisi scelerisque. Ut efficitur fermentum massa a egestas. Morbi at tempus risus, id blandit turpis. Suspendisse sed magna et mauris tristique iaculis nec vel velit. Cras porta diam vitae velit cursus, bibendum venenatis nulla egestas. Aenean molestie magna rutrum nisl aliquet, sit amet lacinia orci feugiat. Quisque placerat quam vitae ultrices cursus."))
                        .delay(1000, TimeUnit.MILLISECONDS);
            default:
                return Observable.just("")
                        .delay(2, TimeUnit.SECONDS)
                        .flatMap(new Func1<String, Observable<ItemWithBody>>() {
                            @Override
                            public Observable<ItemWithBody> call(String s) {
                                return Observable.error(new IOException("error"));
                            }
                        });
        }
    }
}
