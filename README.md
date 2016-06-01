FloppyVideoView
=====

[ ![Download](https://api.bintray.com/packages/eneim/maven/FloppyVideoView/images/download.svg) ](https://bintray.com/eneim/maven/FloppyVideoView/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/im.ene.lab/flvv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/im.ene.lab/flvv)

<img src="https://github.com/eneim/FloppyVideoView/blob/develop/art/web_hi_res_512.png" width="256">

A VideoView which is Floppy :smile:. In short, a VideoView supports *ScaleType* just like ImageView.


## Usage

Try to replace your VideoView with this xml snippet first

- xml

```xml
<im.ene.lab.flvv.FloppyVideoView
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        app:retainRatio="true"
        app:scaleType="fitCenter"/>
```

- java

```java
FloppyVideoView videoView = (FloppyVideoView) findViewById(R.id.video_view);
videoView.setOnPreparedListener(() -> {
  // Video preparing is asynchronous, so your UI may be gone when it's done
  if (videoView != null) {
      videoView.start();
  }
});
```

See ```sample``` for more snippets, especially a co-op sample code with [Toro](https://github.com/eneim/Toro)

## Screen record

<img src="https://github.com/eneim/FloppyVideoView/blob/develop/art/sample.gif" width="288">

## Setup

Add my maven repo to you module's build.gradle repositories

```groovy
repositories {
  // just in 'rare' case you don't have this in your project
  jcenter()
}
```

```groovy
repositories {
  // available on maven central too
  mavenCentral()
}
```

Then add ```flvv``` to that module's dependencies

```groovy
dependencies {
  // other dependencies
  
  // FloppyVideoView v1.0.0
  compile 'im.ene.lab:flvv:1.0.0'
}
```

Latest version: [ ![Download](https://api.bintray.com/packages/eneim/maven/FloppyVideoView/images/download.svg) ](https://bintray.com/eneim/maven/FloppyVideoView/_latestVersion)

LICENSE
=====

  Copyright 2016 Nam Nguyen, nam@ene.im

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
