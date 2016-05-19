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

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import im.ene.lab.flvv.FloppyVideoView;
import im.ene.lab.flvv.sample.R;
import im.ene.lab.flvv.sample.model.Model;
import im.ene.lab.flvv.sample.model.VideoModel;
import im.ene.lab.toro.ToroAdapter;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroViewHolder;
import im.ene.lab.toro.VideoPlayerManager;
import im.ene.lab.toro.VideoPlayerManagerImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 5/19/16.
 */

interface ShowcaseView {

  void showLoading(boolean isLoading);

  void updateData(boolean forceUpdate, List<? extends Model> items);

  class Adapter extends ToroAdapter<VideoViewHolder> implements VideoPlayerManager {

    private final VideoPlayerManager delegate;

    private ArrayList<VideoModel> mItems;

    public Adapter() {
      super();
      delegate = new VideoPlayerManagerImpl();
      mItems = new ArrayList<>();
    }

    public final void addItems(List<VideoModel> items) {
      mItems.addAll(items);
    }

    @Nullable @Override protected Object getItem(int position) {
      return mItems.get(position);
    }

    @Override public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(VideoViewHolder.LAYOUT_RES, parent, false);
      return new VideoViewHolder(view);
    }

    @Override public int getItemCount() {
      return mItems.size();
    }

    @Override public ToroPlayer getPlayer() {
      return delegate.getPlayer();
    }

    @Override public void setPlayer(ToroPlayer player) {
      delegate.setPlayer(player);
    }

    @Override public void onRegistered() {
      delegate.onRegistered();
    }

    @Override public void onUnregistered() {
      delegate.onUnregistered();
    }

    @Override public void startPlayback() {
      delegate.startPlayback();
    }

    @Override public void pausePlayback() {
      delegate.pausePlayback();
    }

    @Override
    public void saveVideoState(String videoId, @Nullable Integer position, long duration) {
      delegate.saveVideoState(videoId, position, duration);
    }

    @Override public void restoreVideoState(String videoId) {
      delegate.restoreVideoState(videoId);
    }

    @Nullable @Override public Integer getSavedPosition(String videoId) {
      return delegate.getSavedPosition(videoId);
    }
  }

  class VideoViewHolder extends ToroViewHolder {

    static final int LAYOUT_RES = R.layout.vh_item_showcase;

    protected final FloppyVideoView mVideoView;
    private TextView mScaleType;
    private TextView mVideoName;

    private boolean mPlayable = true; // normally true
    private VideoModel mItem;

    public VideoViewHolder(View itemView) {
      super(itemView);
      mVideoView = (FloppyVideoView) itemView.findViewById(R.id.video_view);
      if (mVideoView == null) {
        throw new NullPointerException("Unusable ViewHolder");
      }

      mScaleType = (TextView) itemView.findViewById(R.id.scale_type);
      mVideoName = (TextView) itemView.findViewById(R.id.video_name);

      mVideoView.setOnPreparedListener(this);
      mVideoView.setOnCompletionListener(this);
      mVideoView.setOnErrorListener(this);
      mVideoView.setOnInfoListener(this);
      // mVideoView.setOnSeekCompleteListener(this);
    }

    @Override public void bind(@Nullable Object object) {
      if (!(object instanceof VideoModel)) {
        throw new IllegalArgumentException("This ViewHolder only accepts VideoModel");
      }

      mItem = (VideoModel) object;
      mVideoName.setText(mItem.videoName);
      mScaleType.setText(mVideoView.getScaleType().name());

      mVideoView.setVideoURI(Uri.parse(mItem.videoUri));
    }

    // Client could override this method for better practice
    @Override public void start() {
      mVideoView.start();
    }

    @Override public void pause() {
      mVideoView.pause();
    }

    @Override public int getDuration() {
      return mVideoView.getDuration();
    }

    @Override public int getCurrentPosition() {
      return mVideoView.getCurrentPosition();
    }

    @Override public void seekTo(int pos) {
      mVideoView.seekTo(pos);
    }

    @Override public boolean isPlaying() {
      return mVideoView.isPlaying();
    }

    @Override public int getBufferPercentage() {
      return mVideoView.getBufferPercentage();
    }

    @Override public boolean canPause() {
      return mVideoView.canPause();
    }

    @Override public boolean canSeekBackward() {
      return mVideoView.canSeekBackward();
    }

    @Override public boolean canSeekForward() {
      return mVideoView.canSeekForward();
    }

    @Override public int getAudioSessionId() {
      return mVideoView.getAudioSessionId();
    }

    @Override public boolean wantsToPlay() {
      // Default implementation
      return visibleAreaOffset() >= 0.8f;
    }

    @Override public boolean isAbleToPlay() {
      return mPlayable;
    }

    @Nullable @Override public String getVideoId() {
      return mItem != null ? mItem.videoId : null;
    }

    @CallSuper @Override public void onVideoPrepared(MediaPlayer mp) {
      mPlayable = true;
    }

    @Override public boolean onPlaybackError(MediaPlayer mp, int what, int extra) {
      mPlayable = false;
      return super.onPlaybackError(mp, what, extra);
    }

    @NonNull @Override public View getVideoView() {
      return mVideoView;
    }

    @Override public boolean isLoopAble() {
      return true;
    }
  }
}
