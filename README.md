# Extensions to RxJava
This library allow simple implementation for some tasks in android

This project is in very-hight development so everything can chenge. Because of this is not distributed as stand alone project, rather like submodule for your project.

# Examples

## Get retrofit

* Error managment
* Automatically retry when error
* Refresh button
* In memory cache with timeout

```java
Observable<ResponseOrError<Response>> observable = retrofitService.getItem(id)
	.compose(ResponseOrError.<Response>toResponseOrErrorObservable())
	.compose(MoreOperators.<Response>repeatOnError(networkScheduler))
	.compose(MoreOperators.<ResponseOrError<Response>>refresh(refreshSubject))
	.compose(MoreOperators.<ResponseOrError<Response>>cacheWithTimeout(networkScheduler));
```

## Load more

* Load more
* Manage errors
* Automatically retry when error
* Refresh button
* In memory cache with timeout

```java
final OperatorMergeNextToken<Response, Object> mergeNextToken = OperatorMergeNextToken
	.create(new Func1<Response, Observable<Response>>() {
		@Override
		public Observable<Response> call(@Nullable final Response response) {
		if (response == null) {
			// Load data
			return itemsService.listItems(null)
				.subscribeOn(networkScheduler);
		} else {
			final String nextToken = response.nextToken();
			if (nextToken == null) {
				// detect when there is no more data
				return Observable.never();
			}
			// return more data
			final Observable<Response> apiRequest = itemsService.listItems(nextToken)
				.subscribeOn(networkScheduler);
			// join with previous response
			return Observable.just(response).zipWith(apiRequest, new MergeTwoResponses());
		}

		}
	});

Observable<ResponseOrError<Response>> data = loadMoreSubject
	.startWith((Object) null)
	.lift(mergeNextToken)
	.compose(ResponseOrError.<Response>toResponseOrErrorObservable())
	.compose(MoreOperators.<Response>repeatOnError(networkScheduler))
	.compose(MoreOperators.<ResponseOrError<Response>>refresh(refreshSubject))
	.compose(MoreOperators.<ResponseOrError<Response>>cacheWithTimeout(networkScheduler));
```

## Get title from response

* Map error responses
* Concurent subscriptions use same response

```java
Observable<ResponseOrError<String>> titleObservable = itemsDao.dataObservable()
	.compose(ResponseOrError.map(new Func1<Response, String>() {
		@Override
		public String call(Response response) {
			return response.title();
		}
	}))
	.subscribeOn(networkScheduler)
	.observeOn(uiScheduler)
	.compose(ObservableExtensions.<ResponseOrError<String>>behaviorRefCount());
```

## Combine errors from multiple observers

```java
Observable<Throwable> errorObservable = 
	ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)));
```

## Combine progress from multiple observers

```java
Observable<Boolean> progressObservable = 
	ResponseOrError.combineProgressObservable(ImmutableList.of(
                ResponseOrError.transform(titleObservable),
                ResponseOrError.transform(itemsObservable)));
```


## Bind data to views

```java
presenter.titleObservable()
	.compose(lifecycleMainObservable.<String>bindLifecycle())
	.subscribe(MoreViewActions.setTitle(toolbar));
presenter.progressObservable()
	.compose(lifecycleMainObservable.<Boolean>bindLifecycle())
	.subscribe(ViewActions.setVisibility(progress, View.INVISIBLE));

presenter.errorObservable()
	.map(ErrorHelper.mapThrowableToStringError())
	.compose(lifecycleMainObservable.<String>bindLifecycle())
	.subscribe(ViewActions.setText(error));
```

# Build instructions

	./gradlew build

# How to integrate with your project
In your poject directory

```bash
git submodule add <repo> rx-java-extensions
```

add to settings your settings gradle:

```groovy
include ":rx-android-extensions"
include ":rx-extensions"

project(':rx-android-extensions').projectDir = new File('rx-java-extensions/rx-android-extensions')
project(':rx-extensions').projectDir = new File('rx-java-extensions/rx-extensions')
```

In your api project `build.gradle`:

```groovy
compile project(":rx-extensions")
```

In your android project `build.gradle`:

```bash
compile project(":rx-android-extensions")
```

# License

    Copyright [2015] [Jacek Marchwicki <jacek.marchwicki@gmail.com>]
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    	http://www.apache.org/licenses/LICENSE-2.0
        
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
