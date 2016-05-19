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
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import im.ene.lab.flvv.sample.model.Model;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroViewHolder;
import java.util.List;

/**
 * Created by eneim on 5/19/16.
 */

interface ShowcaseView {

  void showLoading(boolean isLoading);

  void updateData(boolean forceUpdate, List<? extends Model> items);

  class Adapter extends ToroAdapter<VideoViewHolder> {

    @Nullable @Override protected Object getItem(int position) {
      return null;
    }

    @Override public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return null;
    }

    @Override public int getItemCount() {
      return 0;
    }
  }

  class VideoViewHolder extends ToroViewHolder {

    public VideoViewHolder(View itemView) {
      super(itemView);
    }

    @Override public void bind(@Nullable Object object) {

    }

    @Override public boolean wantsToPlay() {
      return false;
    }

    @Override public boolean isAbleToPlay() {
      return false;
    }

    @Nullable @Override public String getVideoId() {
      return null;
    }

    @NonNull @Override public View getVideoView() {
      return null;
    }

    @Override public void start() {

    }

    @Override public void pause() {

    }

    @Override public int getDuration() {
      return 0;
    }

    @Override public int getCurrentPosition() {
      return 0;
    }

    @Override public void seekTo(int pos) {

    }

    @Override public boolean isPlaying() {
      return false;
    }
  }
}
