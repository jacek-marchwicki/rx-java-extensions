package com.appunite.rx.example.model.presenter;

import com.appunite.rx.ResponseOrError;
import com.appunite.rx.example.model.dao.PostsDao;
import com.appunite.rx.example.model.model.AddPost;
import com.appunite.rx.example.model.model.PostsResponse;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import retrofit.client.Header;
import retrofit.client.Response;
import rx.observers.TestObserver;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import static com.google.common.truth.Truth.assert_;
import static org.mockito.Mockito.when;

public class CreatePostPresenterTest {

    @Mock
    PostsDao postsDao;

    private TestScheduler testScheduler = Schedulers.test();
    private CreatePostPresenter postPresenter;
    private TestSubject<ResponseOrError<PostsResponse>> postsSubject = TestSubject.create(testScheduler);
    private TestSubject<ResponseOrError<Response>> postSuccessSubject = TestSubject.create(testScheduler);
    private TestObserver<AddPost> postRequestObserver = new TestObserver<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(postsDao.postsObservable()).thenReturn(postsSubject);
        when(postsDao.postSuccesObserver()).thenReturn(postSuccessSubject);
        when(postsDao.postRequestObserver()).thenReturn(postRequestObserver);

        postPresenter = new CreatePostPresenter(postsDao);
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

        assert_().that(postRequestObserver.getOnNextEvents())
                .containsExactly(new AddPost("pies", "body"));
    }

    @Test
    public void testClickSendWithoutData_postIsNotSend() throws Exception {
        postPresenter.sendObservable().onNext(null);

        assert_().that(postRequestObserver.getOnNextEvents())
                .isEmpty();
    }

    @Test
    public void testClickSendWithEmptyData_postIsNotSend() throws Exception {
        postPresenter.bodyObservable().onNext("");
        postPresenter.nameObservable().onNext("");

        postPresenter.sendObservable().onNext(null);

        assert_().that(postRequestObserver.getOnNextEvents())
                .isEmpty();
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

    private void fillDataAndSubmit() {
        postPresenter.bodyObservable().onNext("body");
        postPresenter.nameObservable().onNext("pies");
        postPresenter.sendObservable().onNext(null);
    }

    private void returnCorrectResponse() {
        postSuccessSubject.onNext(ResponseOrError.fromData(new Response("", 200, "", ImmutableList.<Header>of(), null)), 0);
        testScheduler.triggerActions();
    }

    private void returnException() {
        final IOException e = new IOException("xyz");
        postSuccessSubject.onNext(ResponseOrError.<Response>fromError(e), 0);
        testScheduler.triggerActions();
    }

}