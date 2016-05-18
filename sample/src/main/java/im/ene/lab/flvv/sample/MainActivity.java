package im.ene.lab.flvv.sample;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import im.ene.lab.flvv.FloppyVideoView;
import im.ene.lab.flvv.ScaleType;

public class MainActivity extends AppCompatActivity {

  private FloppyVideoView mVideoView;
  private Button mScaleButton;

  private ScaleType[] TYPES = new ScaleType[] {
      ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_XY,
      ScaleType.FIT_START, ScaleType.FIT_END
  };

  int index = 0;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mVideoView = (FloppyVideoView) findViewById(R.id.video_view);
    mScaleButton = (Button) findViewById(R.id.scale_button);

    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        if (mVideoView != null) {
          mVideoView.start();
        }
      }
    });

    mScaleButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mVideoView.setScaleType(TYPES[(index++) % TYPES.length]);
        mScaleButton.setText("TYPE: " + mVideoView.getScaleType().name());
      }
    });

    mScaleButton.setText("TYPE: " + mVideoView.getScaleType().name());
  }

  @Override protected void onResume() {
    super.onResume();
    mVideoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.square);
  }
}
