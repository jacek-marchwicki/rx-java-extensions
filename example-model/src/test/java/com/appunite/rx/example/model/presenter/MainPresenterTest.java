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

package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.Post;
import com.appunite.rx.example.model.model.PostsResponse;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import javax.annotation.Nonnull;

import rx.observers.TestObserver;
import rx.subjects.ReplaySubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.when;


public class MainPresenterTest {

    @Mock
    PostsDao postsDao;


    private MainPresenter mainPresenter;
    private ReplaySubject<ResponseOrError<PostsResponse>> postsSubject = ReplaySubject.create();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(postsDao.postsObservable()).thenReturn(postsSubject);

        mainPresenter = new MainPresenter(postsDao);
    }

    @Test
    public void testAfterStart_presenterIsNotNull() throws Exception {
        assert_().that(mainPresenter).isNotNull();
    }

    @Test
    public void testBeforeDownload_progressBarIsShown() throws Exception {
        final TestObserver<Boolean> progress = new TestObserver<>();
        mainPresenter.progressObservable().subscribe(progress);

        assert_().that(progress.getOnNextEvents())
                .isEqualTo(ImmutableList.of(true));
    }

    @Test
    public void testAfterDownload_progressBarIsHidden() throws Exception {
        final TestObserver<Boolean> progress = new TestObserver<>();
        mainPresenter.progressObservable().subscribe(progress);

        postsSubject.onNext(sampleData());

        assert_().that(progress.getOnNextEvents())
                .isEqualTo(ImmutableList.of(true, false));
    }

    @Nonnull
    private ResponseOrError<PostsResponse> sampleData() {
        return ResponseOrError.fromData(new PostsResponse("some_title", ImmutableList.<Post>of(), null));
    }

    @Test
    public void testBeforeDownload_errorIsNull() throws Exception {
        final TestObserver<Throwable> error = new TestObserver<>();
        mainPresenter.errorObservable().subscribe(error);

        assert_().that(error.getOnNextEvents())
                .contains(null);
    }

    @Test
    public void testAfterSuccessDownload_errorIsStillNull() throws Exception {
        final TestObserver<Throwable> error = new TestObserver<>();
        mainPresenter.errorObservable().subscribe(error);

        postsSubject.onNext(sampleData());

        assert_().that(error.getOnNextEvents())
                .contains(null);
    }

    @Test
    public void testAfterFailedDownload_errorIsSet() throws Exception {
        final Exception e = new Exception();
        final TestObserver<Throwable> error = new TestObserver<>();
        mainPresenter.errorObservable().subscribe(error);

        postsSubject.onNext(ResponseOrError.<PostsResponse>fromError(e));

        assert_().that(error.getOnNextEvents())
                .containsExactly(null, e)
                .inOrder();
    }

    @Test
    public void testAfterSuccessDownload_titleIsSet() throws Exception {
        final TestObserver<String> title = new TestObserver<>();
        mainPresenter.titleObservable().subscribe(title);

        postsSubject.onNext(sampleData());

        assert_().that(title.getOnNextEvents())
                .contains("some_title");
    }

    @Test
    public void testBeforeDownload_titleIsNotSet() throws Exception {
        final TestObserver<String> title = new TestObserver<>();
        mainPresenter.titleObservable().subscribe(title);

        assert_().that(title.getOnNextEvents())
                .isEmpty();
    }

    @Test
    public void testBeforeDownload_doNotPropagateItems() throws Exception {
        final TestObserver<List<MainPresenter.AdapterItem>> items = new TestObserver<>();
        mainPresenter.itemsObservable().subscribe(items);

        assert_().that(items.getOnNextEvents())
                .isEmpty();
    }

    @Test
    public void testAfterDownloadEmptyArray_emptyItemsArrayIsPropagated() throws Exception {
        final TestObserver<List<MainPresenter.AdapterItem>> items = new TestObserver<>();
        mainPresenter.itemsObservable().subscribe(items);

        postsSubject.onNext(sampleData());

        assert_().that(items.getOnNextEvents())
                .contains(ImmutableList.of());
    }

    @Test
    public void testAfterDownload_itemsArePropagated() throws Exception {
        final TestObserver<List<MainPresenter.AdapterItem>> items = new TestObserver<>();
        mainPresenter.itemsObservable().subscribe(items);

        postsSubject.onNext(ResponseOrError.fromData(new PostsResponse("", ImmutableList.of(new Post("123", "krowa")), null)));

        assert_().that(items.getOnNextEvents()).hasSize(1);
        assert_().that(items.getOnNextEvents().get(0)).hasSize(1);
        assert_().that(items.getOnNextEvents().get(0).get(0).text()).isEqualTo("krowa");
        assert_().that(items.getOnNextEvents().get(0).get(0).id()).isEqualTo("123");
    }

    @Test
    public void testAfterClickOnFirstItem_openDetails() throws Exception {
        // Subscribe to open details
        final TestObserver<MainPresenter.AdapterItem> openDetails = new TestObserver<>();
        mainPresenter.openDetailsObservable().subscribe(openDetails);

        // Download item
        final TestObserver<List<MainPresenter.AdapterItem>> items = new TestObserver<>();
        mainPresenter.itemsObservable().subscribe(items);
        postsSubject.onNext(ResponseOrError.fromData(new PostsResponse("", ImmutableList.of(new Post("123", "krowa")), null)));
        final MainPresenter.AdapterItem itemToClick = items.getOnNextEvents().get(0).get(0);

        // user click
        itemToClick.clickObserver().onNext(null);

        // verify if details opened
        assert_().that(openDetails.getOnNextEvents())
                .contains(itemToClick);
    }

}