/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.flvv.sample.data.repository;

import im.ene.lab.flvv.sample.BuildConfig;
import im.ene.lab.flvv.sample.R;
import im.ene.lab.flvv.sample.data.entity.VideoEntity;
import im.ene.lab.flvv.sample.model.VideoModel;
import java.util.List;
import rx.Observable;

/**
 * Created by eneim on 5/19/16.
 */

public interface VideosRepo extends Repository<VideoEntity, VideoModel> {

  String[] VIDEO_URIS = {
      "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
      "android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.vertical_big,
      "android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.square,
      "android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.horizontal,
      "android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.vertical
  };

  String[] VIDEO_NAMES = {
      "Big Buck Bunny", "Square Coffee machine", "City time leaps", "Tree slow motion"
  };

  String[] VIDEO_SOURCES = {
      "Cloud", "Local"
  };

  Observable<List<VideoEntity>> loadVideos(boolean forceUpdate);
}
