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

package im.ene.lab.flvv.sample.features.showcase;

import android.support.annotation.NonNull;
import im.ene.lab.flvv.sample.data.entity.VideoEntity;
import im.ene.lab.flvv.sample.data.repository.VideosRepo;
import im.ene.lab.flvv.sample.model.VideoModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by eneim on 5/19/16.
 */

class ShowcasePresenterImpl implements ShowcasePresenter {

  private ShowcaseView view;
  private final VideosRepo videosRepo;

  ShowcasePresenterImpl() {
    videosRepo = new VideosRepoImpl();
  }

  @Override public void onViewCreated(ShowcaseView view) {
    this.view = view;
  }

  @Override public void onViewDestroyed(ShowcaseView view) {
    this.view = null;
  }

  @Override public void loadItems(boolean forceUpdate, final DataCallback callback) {
    if (view != null) {
      view.showLoading(true);
    }

    videosRepo.loadVideos(forceUpdate)
        .onErrorResumeNext(new Func1<Throwable, Observable<List<VideoEntity>>>() {
          @Override public Observable<List<VideoEntity>> call(Throwable throwable) {
            return Observable.empty();
          }
        })
        .delay(1500, TimeUnit.MILLISECONDS) // to mimic the long loading
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<VideoEntity>>() {
          @Override public void call(List<VideoEntity> videoEntities) {
            if (view != null) {
              view.showLoading(false);
            }
            List<VideoModel> models = new ArrayList<>();
            for (VideoEntity entity : videoEntities) {
              models.add(videosRepo.transform(entity));
            }

            if (callback != null) {
              callback.onDataLoaded(models);
            }
          }
        });
  }

  private static class VideosRepoImpl implements VideosRepo {

    @Override public Observable<List<VideoEntity>> loadVideos(boolean forceUpdate) {
      List<VideoEntity> items = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        items.add(VideoEntity.newVideoEntity());
      }

      return Observable.just(items);
    }

    @NonNull @Override public VideoModel transform(@NonNull VideoEntity source) {
      VideoModel model = new VideoModel();
      model.videoName = source.name;
      model.videoUri = source.video;
      model.videoId = source.name + ":" + source.video + ":" + source.source;
      return model;
    }
  }
}
