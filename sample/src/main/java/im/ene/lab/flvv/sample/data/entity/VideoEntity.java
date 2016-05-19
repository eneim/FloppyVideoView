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

package im.ene.lab.flvv.sample.data.entity;

import android.support.annotation.NonNull;
import im.ene.lab.flvv.sample.data.repository.VideosRepo;
import java.util.ArrayList;

/**
 * Created by eneim on 5/19/16.
 */

public class VideoEntity extends Entity {

  public String name;

  public String video;

  public String source;

  public VideoEntity(String name, String video, String source) {
    this.name = name;
    this.video = video;
    this.source = source;
  }

  @NonNull @Override public String name() {
    return this.name;
  }

  static ArrayList<String> sCache;
  static final int MAX = VideosRepo.VIDEO_NAMES.length
      * VideosRepo.VIDEO_URIS.length
      * VideosRepo.VIDEO_SOURCES.length;

  public static VideoEntity newVideoEntity() {
    sCache = new ArrayList<>(); // refresh
    boolean isAccepted = false;
    int counter = 0;
    VideoEntity videoEntity = null;
    while (!isAccepted && (++counter < MAX)) {
      videoEntity = new VideoEntity(
          VideosRepo.VIDEO_NAMES[(int) (Math.random() * VideosRepo.VIDEO_NAMES.length)],
          VideosRepo.VIDEO_URIS[(int) (Math.random() * VideosRepo.VIDEO_URIS.length)],
          VideosRepo.VIDEO_SOURCES[(int) (Math.random() * VideosRepo.VIDEO_SOURCES.length)]);
      String videoString = videoEntity.toString();
      isAccepted = !sCache.contains(videoString);
    }

    if (videoEntity == null) {
      return null;
    }

    sCache.add(videoEntity.toString());
    return videoEntity;
  }

  @Override public String toString() {
    return "VideoEntity{" +
        "name='" + name + '\'' +
        ", video='" + video + '\'' +
        ", source='" + source + '\'' +
        '}';
  }
}
