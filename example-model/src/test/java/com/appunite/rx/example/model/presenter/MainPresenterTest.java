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
import com.appunite.rx.android.adapter.BaseAdapterItem;
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

import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.when;


public class MainPresenterTest {

    @Mock
    PostsDao postsDao;


    private MainPresenter mainPresenter;
    private final TestScheduler scheduler = new TestScheduler();
    private TestSubject<ResponseOrError<PostsResponse>> postsSubject = TestSubject.create(scheduler);

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
        final TestSubscriber<Boolean> progress = new TestSubscriber<>();
        mainPresenter.progressObservable().subscribe(progress);

        assert_().that(progress.getOnNextEvents())
                .isEqualTo(ImmutableList.of(true));
        progress.assertNoErrors();
    }

    @Test
    public void testAfterDownload_progressBarIsHidden() throws Exception {
        final TestSubscriber<Boolean> progress = new TestSubscriber<>();
        mainPresenter.progressObservable().subscribe(progress);

        postsSubject.onNext(sampleData());
        scheduler.triggerActions();

        assert_().that(progress.getOnNextEvents())
                .isEqualTo(ImmutableList.of(true, false));
        progress.assertNoErrors();
    }

    @Nonnull
    private ResponseOrError<PostsResponse> sampleData() {
        return ResponseOrError.fromData(new PostsResponse("some_title", ImmutableList.<Post>of(), null));
    }

    @Test
    public void testBeforeDownload_errorIsNull() throws Exception {
        final TestSubscriber<Throwable> error = new TestSubscriber<>();
        mainPresenter.errorObservable().subscribe(error);

        assert_().that(error.getOnNextEvents())
                .contains(null);
        error.assertNoErrors();
    }

    @Test
    public void testAfterSuccessDownload_errorIsStillNull() throws Exception {
        final TestSubscriber<Throwable> error = new TestSubscriber<>();
        mainPresenter.errorObservable().subscribe(error);

        postsSubject.onNext(sampleData());
        scheduler.triggerActions();

        assert_().that(error.getOnNextEvents())
                .contains(null);
        error.assertNoErrors();
    }

    @Test
    public void testAfterFailedDownload_errorIsSet() throws Exception {
        final Exception e = new Exception();
        final TestSubscriber<Throwable> error = new TestSubscriber<>();
        mainPresenter.errorObservable().subscribe(error);

        postsSubject.onNext(ResponseOrError.<PostsResponse>fromError(e));
        scheduler.triggerActions();

        assert_().that(error.getOnNextEvents())
                .containsExactly(null, e)
                .inOrder();
        error.assertNoErrors();
    }

    @Test
    public void testAfterSuccessDownload_titleIsSet() throws Exception {
        final TestSubscriber<String> title = new TestSubscriber<>();
        mainPresenter.titleObservable().subscribe(title);

        postsSubject.onNext(sampleData());
        scheduler.triggerActions();

        assert_().that(title.getOnNextEvents())
                .contains("some_title");
        title.assertNoErrors();
    }

    @Test
    public void testBeforeDownload_titleIsNotSet() throws Exception {
        final TestSubscriber<String> title = new TestSubscriber<>();
        mainPresenter.titleObservable().subscribe(title);

        assert_().that(title.getOnNextEvents())
                .isEmpty();
        title.assertNoErrors();
    }

    @Test
    public void testBeforeDownload_doNotPropagateItems() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> items = new TestSubscriber<>();
        mainPresenter.itemsObservable().subscribe(items);

        assert_().that(items.getOnNextEvents())
                .isEmpty();
        items.assertNoErrors();
    }

    @Test
    public void testAfterDownloadEmptyArray_emptyItemsArrayIsPropagated() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> items = new TestSubscriber<>();
        mainPresenter.itemsObservable().subscribe(items);

        postsSubject.onNext(sampleData());
        scheduler.triggerActions();

        assert_().that(items.getOnNextEvents())
                .contains(ImmutableList.of());
        items.assertNoErrors();
    }

    @Test
    public void testAfterDownload_itemsArePropagated() throws Exception {
        final TestSubscriber<List<BaseAdapterItem>> items = new TestSubscriber<>();
        mainPresenter.itemsObservable().subscribe(items);

        postsSubject.onNext(ResponseOrError.fromData(new PostsResponse("",
                ImmutableList.of(new Post("123", "krowa")), null)));
        scheduler.triggerActions();

        assert_().that(items.getOnNextEvents()).hasSize(1);
        assert_().that(items.getOnNextEvents().get(0)).hasSize(1);
        assert_().that(items.getOnNextEvents().get(0).get(0))
                .isInstanceOf(MainPresenter.AdapterItem.class);
        final MainPresenter.AdapterItem firstItem = (MainPresenter.AdapterItem) items
                .getOnNextEvents().get(0).get(0);
        assert_().that(firstItem.text()).isEqualTo("krowa");
        assert_().that(firstItem.id()).isEqualTo("123");
        items.assertNoErrors();
    }

    @Test
    public void testAfterClickOnFirstItem_openDetails() throws Exception {
        // Subscribe to open details
        final TestSubscriber<MainPresenter.AdapterItem> openDetails = new TestSubscriber<>();
        mainPresenter.openDetailsObservable().subscribe(openDetails);

        // Download item
        final TestSubscriber<List<BaseAdapterItem>> items = new TestSubscriber<>();
        mainPresenter.itemsObservable().subscribe(items);
        postsSubject.onNext(ResponseOrError.fromData(new PostsResponse("",
                ImmutableList.of(new Post("123", "krowa")), null)));
        scheduler.triggerActions();
        final BaseAdapterItem itemToClick = items.getOnNextEvents().get(0).get(0);

        // user click
        assert_().that(itemToClick).isInstanceOf(MainPresenter.AdapterItem.class);
        ((MainPresenter.AdapterItem)itemToClick).clickObserver().onNext(null);

        // verify if details opened
        assert_().that(openDetails.getOnNextEvents())
                .contains(itemToClick);
        openDetails.assertNoErrors();
    }

}