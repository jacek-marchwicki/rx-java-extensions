package com.appunite.rx.example.model.api;

import com.appunite.rx.example.model.dao.ItemsDao;
import com.appunite.rx.example.model.model.Item;
import com.appunite.rx.example.model.model.Response;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;

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
    public Observable<Item> getItem(String id) {
        final Observable<Item> apiCall;
        switch (id) {
            case "1":
                apiCall = Observable.just(new Item("1", "title")).delay(2, TimeUnit.SECONDS);
                break;
            case "2":
                apiCall = Observable.just(new Item("2", "title2")).delay(3, TimeUnit.SECONDS);
                break;
            default:
                apiCall = Observable.<Item>error(new IOException("error")).delay(4, TimeUnit.SECONDS);

                break;
        }
        return apiCall;
    }
}
