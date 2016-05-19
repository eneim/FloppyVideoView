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

package im.ene.lab.flvv.sample.features.standalone;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import im.ene.lab.flvv.FloppyVideoView;
import im.ene.lab.flvv.ScaleType;
import im.ene.lab.flvv.sample.R;
import im.ene.lab.flvv.sample.data.repository.VideosRepo;

/**
 * Created by eneim on 5/19/16.
 */

public class StandalonePlayerActivity extends AppCompatActivity {

  private FloppyVideoView videoView;
  private Button changeScaleTypeButton;
  private Button changeVideo;

  final ScaleType[] SCALE_TYPES = {
      ScaleType.FIT_CENTER, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_END,
      ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE
  };

  int scaleTypeIndex = 0;
  int videoIndex = 0;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    videoView = (FloppyVideoView) findViewById(R.id.video_view);
    changeScaleTypeButton = (Button) findViewById(R.id.scale_button);
    changeVideo = (Button) findViewById(R.id.change_video);

    videoView.setScaleType(SCALE_TYPES[scaleTypeIndex % SCALE_TYPES.length]);
    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        if (videoView != null) {
          videoView.start();
        }
      }
    });

    changeScaleTypeButton.setText("SCALE TYPE: " + videoView.getScaleType().name());
    changeScaleTypeButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        videoView.setScaleType(SCALE_TYPES[++scaleTypeIndex % SCALE_TYPES.length]);
        changeScaleTypeButton.setText("SCALE TYPE: " + videoView.getScaleType().name());
      }
    });

    changeVideo.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        videoView.setVideoPath(VideosRepo.VIDEO_URIS[++videoIndex % VideosRepo.VIDEO_URIS.length]);
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    videoView.setVideoPath(VideosRepo.VIDEO_URIS[videoIndex % VideosRepo.VIDEO_URIS.length]);
  }

  @Override protected void onPause() {
    super.onPause();
    videoView.stopPlayback();
  }
}
