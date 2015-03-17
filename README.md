# Extensions to RxJava
This library allow simple implementation for some tasks in android

This project is in very-hight development so everything can chenge. Because of this is not distributed as stand alone project, rather like submodule for your project.

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
