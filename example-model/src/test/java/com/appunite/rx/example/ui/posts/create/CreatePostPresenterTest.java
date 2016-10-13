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

package com.appunite.rx.example.ui.posts.create;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.dao.posts.PostsDao;
import com.appunite.rx.example.dao.posts.model.AddPost;
import com.appunite.rx.example.dao.posts.model.PostWithBody;
import com.appunite.rx.example.dao.posts.model.PostsResponse;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreatePostPresenterTest {

    @Mock
    PostsDao postsDao;

    private TestScheduler testScheduler = Schedulers.test();
    private CreatePostPresenter postPresenter;
    private TestSubject<ResponseOrError<PostsResponse>> postsSubject = TestSubject.create(testScheduler);
    private TestSubject<ResponseOrError<PostWithBody>> addPostResult = TestSubject.create(testScheduler);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(postsDao.postsObservable()).thenReturn(postsSubject);
        when(postsDao.postRequestObserver(any(AddPost.class))).thenReturn(addPostResult);

        postPresenter = new CreatePostPresenter(postsDao, testScheduler);
    }

    @Test
    public void testAfterStart_presenterIsNotNull() throws Exception {
        assert_().that(postPresenter).isNotNull();
    }

    @Test
    public void testAfterNavigationUpClick_closeActivity() throws Exception {
        final TestSubscriber<Object> finishActivity = new TestSubscriber<>();
        postPresenter.finishActivityObservable().subscribe(finishActivity);

        postPresenter.navigationClickObserver().onNext(null);

        assert_().that(finishActivity.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterStart_activityIsNotClosed() throws Exception {
        final TestSubscriber<Object> finishActivity = new TestSubscriber<>();

        postPresenter.finishActivityObservable().subscribe(finishActivity);

        assert_().that(finishActivity.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testAfterFillingDataAndClickSend_postIsSent() throws Exception {
        fillDataAndSubmit();

        verify(postsDao).postRequestObserver(new AddPost("pies", "body"));
    }

    @Test
    public void testClickSendWithoutData_postIsNotSend() throws Exception {
        postPresenter.sendObservable().onNext(null);

        verify(postsDao, never()).postRequestObserver(any(AddPost.class));
    }

    @Test
    public void testClickSendWithEmptyData_postIsNotSend() throws Exception {
        postPresenter.bodyObservable().onNext("");
        postPresenter.nameObservable().onNext("");

        postPresenter.sendObservable().onNext(null);

        verify(postsDao, never()).postRequestObserver(any(AddPost.class));
    }

    @Test
    public void testAfterSaveSuccess_closeActivity() throws Exception {
        final TestSubscriber<Object> finishActivity = new TestSubscriber<>();
        postPresenter.finishActivityObservable().subscribe(finishActivity);
        fillDataAndSubmit();

        returnCorrectResponse();

        assert_().that(finishActivity.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterSaveSuccess_doNotShowError() throws Exception {
        final TestSubscriber<Object> showError = new TestSubscriber<>();
        postPresenter.postErrorObservable().subscribe(showError);
        fillDataAndSubmit();

        returnCorrectResponse();

        assert_().that(showError.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testAfterSaveFail_showError() throws Exception {
        final TestSubscriber<Object> showError = new TestSubscriber<>();
        postPresenter.postErrorObservable().subscribe(showError);
        fillDataAndSubmit();

        returnException();

        assert_().that(showError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterSaveFail_doNotCloseActivity() throws Exception {
        final TestSubscriber<Object> finishActivity = new TestSubscriber<>();
        postPresenter.finishActivityObservable().subscribe(finishActivity);
        fillDataAndSubmit();

        returnException();

        assert_().that(finishActivity.getOnNextEvents()).isEmpty();
    }

    @Test
    public void testAfterBodyIsNotSendAndSubmitClick_showBodyEmptyError() throws Exception {
        final TestSubscriber<Object> bodyEmptyError = new TestSubscriber<>();
        postPresenter.showBodyIsEmptyErrorObservable().subscribe(bodyEmptyError);
        postPresenter.nameObservable().onNext("pies");

        postPresenter.sendObservable().onNext(null);

        assert_().that(bodyEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterNameIsNotSendAndSubmitClick_showNameEmptyError() throws Exception {
        final TestSubscriber<Object> nameEmptyError = new TestSubscriber<>();
        postPresenter.showNameIsEmptyErrorObservable().subscribe(nameEmptyError);
        postPresenter.bodyObservable().onNext("pies");

        postPresenter.sendObservable().onNext(null);

        assert_().that(nameEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterBodyIsNullSendAndSubmitClick_showBodyEmptyError() throws Exception {
        final TestSubscriber<Object> bodyEmptyError = new TestSubscriber<>();
        postPresenter.showBodyIsEmptyErrorObservable().subscribe(bodyEmptyError);
        postPresenter.nameObservable().onNext("pies");
        postPresenter.bodyObservable().onNext(null);

        postPresenter.sendObservable().onNext(null);

        assert_().that(bodyEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterNameIsNullSendAndSubmitClick_showNameEmptyError() throws Exception {
        final TestSubscriber<Object> nameEmptyError = new TestSubscriber<>();
        postPresenter.showNameIsEmptyErrorObservable().subscribe(nameEmptyError);
        postPresenter.nameObservable().onNext(null);
        postPresenter.bodyObservable().onNext("pies");

        postPresenter.sendObservable().onNext(null);

        assert_().that(nameEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterBodyIsEmptyStringSendAndSubmitClick_showBodyEmptyError() throws Exception {
        final TestSubscriber<Object> bodyEmptyError = new TestSubscriber<>();
        postPresenter.showBodyIsEmptyErrorObservable().subscribe(bodyEmptyError);
        postPresenter.nameObservable().onNext("pies");
        postPresenter.bodyObservable().onNext("");

        postPresenter.sendObservable().onNext(null);

        assert_().that(bodyEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterNameIsEmptyStringSendAndSubmitClick_showNameEmptyError() throws Exception {
        final TestSubscriber<Object> nameEmptyError = new TestSubscriber<>();
        postPresenter.showNameIsEmptyErrorObservable().subscribe(nameEmptyError);
        postPresenter.nameObservable().onNext("");
        postPresenter.bodyObservable().onNext("pies");

        postPresenter.sendObservable().onNext(null);

        assert_().that(nameEmptyError.getOnNextEvents()).hasSize(1);
    }

    @Test
    public void testAfterStart_progressBarIsHidden() throws Exception {
        final TestSubscriber<Boolean> showProgress = new TestSubscriber<>();
        postPresenter.progressObservable().subscribe(showProgress);

        assert_().that(showProgress.getOnNextEvents()).isEqualTo(ImmutableList.of(false));
    }

    @Test
    public void testAfterFillingDataAndClickSend_progressBarIsShowed() throws Exception {
        final TestSubscriber<Object> showProgress = new TestSubscriber<>();
        postPresenter.progressObservable().subscribe(showProgress);
        fillDataAndSubmit();

        assert_().that(showProgress.getOnNextEvents()).isEqualTo(ImmutableList.of(false, true));
    }

    @Test
    public void testAfterSaveSucces_progressBarIsHidden() throws Exception {
        final TestSubscriber<Object> hideProgressBar = new TestSubscriber<>();
        postPresenter.progressObservable().subscribe(hideProgressBar);
        fillDataAndSubmit();
        returnCorrectResponse();

        assert_().that(hideProgressBar.getOnNextEvents()).isEqualTo(ImmutableList.of(false,true,false));
    }

    @Test
    public void testAfterSaveFail_progressBarIsHidden() throws Exception {
        final TestSubscriber<Object> hideProgressBar = new TestSubscriber<>();
        postPresenter.progressObservable().subscribe(hideProgressBar);
        fillDataAndSubmit();
        returnException();

        assert_().that(hideProgressBar.getOnNextEvents()).isEqualTo(ImmutableList.of(false,true,false));
    }

    private void fillDataAndSubmit() {
        postPresenter.bodyObservable().onNext("body");
        postPresenter.nameObservable().onNext("pies");
        postPresenter.sendObservable().onNext(null);
    }

    private void returnCorrectResponse() {
        addPostResult.onNext(ResponseOrError.fromData(new PostWithBody("id", "krowa", "123")), 0);
        testScheduler.triggerActions();
    }

    private void returnException() {
        final IOException e = new IOException("xyz");
        addPostResult.onNext(ResponseOrError.<PostWithBody>fromError(e), 0);
        testScheduler.triggerActions();
    }

}