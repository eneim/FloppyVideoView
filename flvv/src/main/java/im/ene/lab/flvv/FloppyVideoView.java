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

package im.ene.lab.flvv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import java.io.IOException;
import java.util.Map;

import static im.ene.lab.flvv.ScaleType.CENTER;

/**
 * Created by eneim on 5/16/16.
 */
public class FloppyVideoView extends TextureView implements MediaPlayerControl {
  private static final String TAG = BuildConfig.LOG_TAG;
  // settable by the client
  private Uri mUri;
  private Map<String, String> mHeaders;

  // all possible internal states
  private static final int STATE_ERROR = -1;
  private static final int STATE_IDLE = 0;
  private static final int STATE_PREPARING = 1;
  private static final int STATE_PREPARED = 2;
  private static final int STATE_PLAYING = 3;
  private static final int STATE_PAUSED = 4;
  private static final int STATE_PLAYBACK_COMPLETED = 5;

  // mCurrentState is a TextureVideoView object's current state.
  // mTargetState is the state that a method caller intends to reach.
  // For instance, regardless the TextureVideoView object's current state,
  // calling pause() intends to bring the object to a target state
  // of STATE_PAUSED.
  private int mCurrentState = STATE_IDLE;
  private int mTargetState = STATE_IDLE;

  // All the stuff we need for playing and showing a video
  private Surface mSurface = null;
  private MediaPlayer mMediaPlayer = null;
  private int mAudioSession;
  private int mVideoWidth;
  private int mVideoHeight;
  private MediaController mMediaController;
  private OnCompletionListener mOnCompletionListener;
  private MediaPlayer.OnPreparedListener mOnPreparedListener;
  private int mCurrentBufferPercentage;
  private OnErrorListener mOnErrorListener;
  private OnInfoListener mOnInfoListener;
  private int mSeekWhenPrepared;  // recording the seek position while preparing
  private boolean mCanPause;
  private boolean mCanSeekBack;
  private boolean mCanSeekForward;

  public FloppyVideoView(Context context) {
    this(context, null);
  }

  public FloppyVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FloppyVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initVideoView();
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloppyVideoView);

    try {
      mRetainRatio = typedArray.getBoolean(R.styleable.FloppyVideoView_retainRatio, true);
      int scaleType = typedArray.getInt(R.styleable.FloppyVideoView_scaleType, 1);
      mScaleType = ScaleType.lookup(scaleType);
    } finally {
      typedArray.recycle();
    }

    mMatrix = new Matrix();
  }

  // AdjustViewBounds behavior will be in compatibility mode for older apps.
  private boolean mAdjustViewBoundsCompat = false;

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
    //        + MeasureSpec.toString(heightMeasureSpec) + ")");
    int widthSize = getDefaultSize(mVideoWidth, widthMeasureSpec);
    int heightSize = getDefaultSize(mVideoHeight, heightMeasureSpec);

    if (mVideoWidth > 0 && mVideoHeight > 0) {
      int w = mVideoWidth;
      int h = mVideoHeight;
      // Desired aspect ratio of the view's contents (not including padding)
      float desiredAspect = 0.0f;
      // We are allowed to change the view's width
      boolean resizeWidth = false;
      // We are allowed to change the view's height
      boolean resizeHeight = false;

      int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
      int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

      if (mRetainRatio) {
        resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        desiredAspect = (float) w / (float) h;
      }

      if (resizeHeight || resizeWidth) {
        // Get the max possible width given our constraints
        widthSize = resolveAdjustedSize(w, widthMeasureSpec);
        // Get the max possible height given our constraints
        heightSize = resolveAdjustedSize(h, heightMeasureSpec);

        if (desiredAspect != 0.0f) {
          // See what our actual aspect ratio is
          float actualAspect = (float) widthSize / (float) heightSize;
          if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
            boolean done = false;
            // Try adjusting width to be proportional to height
            if (resizeWidth) {
              int newWidth = (int) (desiredAspect * heightSize);
              if (!resizeHeight && !mAdjustViewBoundsCompat) {
                widthSize = resolveAdjustedSize(newWidth, widthMeasureSpec);
              }
              if (newWidth <= widthSize) {
                widthSize = newWidth;
                done = true;
              }
            }

            // Try adjusting height to be proportional to width
            if (!done && resizeHeight) {
              int newHeight = (int) (widthSize / desiredAspect);
              // Allow the height to outgrow its original estimate if width is fixed.
              if (!resizeWidth && !mAdjustViewBoundsCompat) {
                heightSize = resolveAdjustedSize(newHeight, heightMeasureSpec);
              }
              if (newHeight <= heightSize) {
                heightSize = newHeight;
              }
            }
          }
        }
      } else {
        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
      }
    } else {
      // no size yet, just adopt the given spec sizes
    }
    setMeasuredDimension(widthSize, heightSize);
  }

  @Override public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(FloppyVideoView.class.getName());
  }

  @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(FloppyVideoView.class.getName());
  }

  private int resolveAdjustedSize(int desiredSize, int measureSpec) {
    return getDefaultSize(desiredSize, measureSpec);
  }

  private void initVideoView() {
    mAdjustViewBoundsCompat = getContext().getApplicationInfo().targetSdkVersion
        <= Build.VERSION_CODES.JELLY_BEAN_MR1;
    mVideoWidth = 0;
    mVideoHeight = 0;
    setSurfaceTextureListener(mSurfaceTextureListener);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    mCurrentState = STATE_IDLE;
    mTargetState = STATE_IDLE;
  }

  /**
   * Sets video path.
   *
   * @param path the path of the video.
   */
  public void setVideoPath(String path) {
    setVideoURI(Uri.parse(path));
  }

  /**
   * Sets video URI.
   *
   * @param uri the URI of the video.
   */
  public void setVideoURI(Uri uri) {
    setVideoURI(uri, null);
  }

  /**
   * Sets video URI using specific headers.
   *
   * @param uri the URI of the video.
   * @param headers the headers for the URI request.
   * Note that the cross domain redirection is allowed by default, but that can be
   * changed with key/value pairs through the headers parameter with
   * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
   * to disallow or allow cross domain redirection.
   */
  public void setVideoURI(Uri uri, Map<String, String> headers) {
    mUri = uri;
    mHeaders = headers;
    mSeekWhenPrepared = 0;
    openVideo();
    requestLayout();
    invalidate();
  }

  public void stopPlayback() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      mTargetState = STATE_IDLE;
      AudioManager am = (AudioManager) getContext().getApplicationContext()
          .getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  private void openVideo() {
    if (mUri == null || mSurface == null) {
      // not ready for playback just yet, will try again later
      return;
    }
    // we shouldn't clear the target state, because somebody might have
    // called start() previously
    release(false);

    AudioManager am =
        (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    try {
      mMediaPlayer = new MediaPlayer();

      if (mAudioSession != 0) {
        mMediaPlayer.setAudioSessionId(mAudioSession);
      } else {
        mAudioSession = mMediaPlayer.getAudioSessionId();
      }
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mCurrentBufferPercentage = 0;
      mMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, mHeaders);
      mMediaPlayer.setSurface(mSurface);
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync();

      // we don't set the target state here either, but preserve the
      // target state that was there before.
      mCurrentState = STATE_PREPARING;
      attachMediaController();
    } catch (IOException ex) {
      if (BuildConfig.DEBUG) Log.w(TAG, "Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    } catch (IllegalArgumentException ex) {
      if (BuildConfig.DEBUG) Log.w(TAG, "Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    }
  }

  public void setMediaController(MediaController controller) {
    if (mMediaController != null) {
      mMediaController.hide();
    }
    mMediaController = controller;
    attachMediaController();
  }

  private void attachMediaController() {
    if (mMediaPlayer != null && mMediaController != null) {
      mMediaController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
      mMediaController.setAnchorView(anchorView);
      mMediaController.setEnabled(isInPlaybackState());
    }
  }

  MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
      new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
          mVideoWidth = mp.getVideoWidth();
          mVideoHeight = mp.getVideoHeight();
          if (mVideoWidth != 0 && mVideoHeight != 0) {
            getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
            requestLayout();
            updateScaleType();
          }
        }
      };

  MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
    public void onPrepared(MediaPlayer mp) {
      mCurrentState = STATE_PREPARED;

      mCanPause = mCanSeekBack = mCanSeekForward = true;

      if (mOnPreparedListener != null) {
        mOnPreparedListener.onPrepared(mMediaPlayer);
      }
      if (mMediaController != null) {
        mMediaController.setEnabled(true);
      }
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();

      int seekToPosition =
          mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
      if (seekToPosition != 0) {
        seekTo(seekToPosition);
      }
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
        getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
        // We won't get a "surface changed" callback if the surface is already the right size, so
        // start the video here instead of in the callback.
        if (mTargetState == STATE_PLAYING) {
          start();
          if (mMediaController != null) {
            mMediaController.show();
          }
        } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
          if (mMediaController != null) {
            // Show the media controls when we're paused into a video and make 'em stick.
            mMediaController.show(0);
          }
        }
      } else {
        // We don't know the video size yet, but should start anyway.
        // The video size might be reported to us later.
        if (mTargetState == STATE_PLAYING) {
          start();
        }
      }
    }
  };

  private MediaPlayer.OnCompletionListener mCompletionListener =
      new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
          mCurrentState = STATE_PLAYBACK_COMPLETED;
          mTargetState = STATE_PLAYBACK_COMPLETED;
          if (mMediaController != null) {
            mMediaController.hide();
          }
          if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mMediaPlayer);
          }
        }
      };

  private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
    public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(mp, arg1, arg2);
      }
      return true;
    }
  };

  private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
      if (BuildConfig.DEBUG) Log.d(TAG, "Error: " + framework_err + "," + impl_err);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      if (mMediaController != null) {
        mMediaController.hide();
      }

            /* If an error handler has been supplied, use it and finish. */
      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
          return true;
        }
      }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
      if (getWindowToken() != null) {
        Resources r = getContext().getResources();
        int messageId;

        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
          messageId = android.R.string.VideoView_error_text_invalid_progressive_playback;
        } else {
          messageId = android.R.string.VideoView_error_text_unknown;
        }

        new AlertDialog.Builder(getContext()).setMessage(messageId)
            .setPositiveButton(android.R.string.VideoView_error_button,
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                                        /* If we get here, there is no onError listener, so
                                         * at least inform them that the video is over.
                                         */
                    if (mOnCompletionListener != null) {
                      mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                  }
                })
            .setCancelable(false)
            .show();
      }
      return true;
    }
  };

  private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
      new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
          mCurrentBufferPercentage = percent;
        }
      };

  /**
   * Register a callback to be invoked when the media file
   * is loaded and ready to go.
   *
   * @param l The callback that will be run
   */
  public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
    mOnPreparedListener = l;
  }

  /**
   * Register a callback to be invoked when the end of a media file
   * has been reached during playback.
   *
   * @param l The callback that will be run
   */
  public void setOnCompletionListener(OnCompletionListener l) {
    mOnCompletionListener = l;
  }

  /**
   * Register a callback to be invoked when an error occurs
   * during playback or setup.  If no listener is specified,
   * or if the listener returned false, TextureVideoView will inform
   * the user of any errors.
   *
   * @param l The callback that will be run
   */
  public void setOnErrorListener(OnErrorListener l) {
    mOnErrorListener = l;
  }

  /**
   * Register a callback to be invoked when an informational event
   * occurs during playback or setup.
   *
   * @param l The callback that will be run
   */
  public void setOnInfoListener(OnInfoListener l) {
    mOnInfoListener = l;
  }

  TextureView.SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
    @Override public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width,
        final int height) {
      boolean isValidState = (mTargetState == STATE_PLAYING);
      boolean hasValidSize = (width > 0 && height > 0);
      if (mMediaPlayer != null && isValidState && hasValidSize) {
        if (mSeekWhenPrepared != 0) {
          seekTo(mSeekWhenPrepared);
        }
        start();
      }
    }

    @Override public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width,
        final int height) {
      mSurface = new Surface(surface);
      openVideo();
    }

    @Override public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
      // after we return from this we can't use the surface any more
      if (mSurface != null) {
        mSurface.release();
        mSurface = null;
      }
      if (mMediaController != null) mMediaController.hide();
      release(true);
      return true;
    }

    @Override public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
      // do nothing
    }
  };

  /*
   * release the media player in any state
   */
  private void release(boolean clearTargetState) {
    if (mMediaPlayer != null) {
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      if (clearTargetState) {
        mTargetState = STATE_IDLE;
      }
      AudioManager am = (AudioManager) getContext().getApplicationContext()
          .getSystemService(Context.AUDIO_SERVICE);
      am.abandonAudioFocus(null);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    if (isInPlaybackState() && mMediaController != null) {
      toggleMediaControlsVisiblity();
    }
    return false;
  }

  @Override public boolean onTrackballEvent(MotionEvent ev) {
    if (isInPlaybackState() && mMediaController != null) {
      toggleMediaControlsVisiblity();
    }
    return false;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
        keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
        keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
        keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
        keyCode != KeyEvent.KEYCODE_MENU &&
        keyCode != KeyEvent.KEYCODE_CALL &&
        keyCode != KeyEvent.KEYCODE_ENDCALL;
    if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        } else {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
        if (!mMediaPlayer.isPlaying()) {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
          || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        }
        return true;
      } else {
        toggleMediaControlsVisiblity();
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  private void toggleMediaControlsVisiblity() {
    if (mMediaController.isShowing()) {
      mMediaController.hide();
    } else {
      mMediaController.show();
    }
  }

  @Override public void start() {
    if (isInPlaybackState()) {
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
  }

  @Override public void pause() {
    if (isInPlaybackState()) {
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.pause();
        mCurrentState = STATE_PAUSED;
      }
    }
    mTargetState = STATE_PAUSED;
  }

  public void suspend() {
    release(false);
  }

  public void resume() {
    openVideo();
  }

  @Override public int getDuration() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getDuration();
    }

    return -1;
  }

  @Override public int getCurrentPosition() {
    if (isInPlaybackState()) {
      return mMediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  @Override public void seekTo(int msec) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(msec);
      mSeekWhenPrepared = 0;
    } else {
      mSeekWhenPrepared = msec;
    }
  }

  @Override public boolean isPlaying() {
    return isInPlaybackState() && mMediaPlayer.isPlaying();
  }

  @Override public int getBufferPercentage() {
    if (mMediaPlayer != null) {
      return mCurrentBufferPercentage;
    }
    return 0;
  }

  private boolean isInPlaybackState() {
    return (mMediaPlayer != null &&
        mCurrentState != STATE_ERROR &&
        mCurrentState != STATE_IDLE &&
        mCurrentState != STATE_PREPARING);
  }

  @Override public boolean canPause() {
    return mCanPause;
  }

  @Override public boolean canSeekBackward() {
    return mCanSeekBack;
  }

  @Override public boolean canSeekForward() {
    return mCanSeekForward;
  }

  public int getAudioSessionId() {
    if (mAudioSession == 0) {
      MediaPlayer foo = new MediaPlayer();
      mAudioSession = foo.getAudioSessionId();
      foo.release();
    }
    return mAudioSession;
  }

  /* Custom implementations */
  @NonNull private Matrix mMatrix;
  @NonNull private ScaleType mScaleType;
  private boolean mRetainRatio = true;

  /**
   * Controls how the image should be resized or moved to match the size
   * of this ImageView.
   *
   * @param scaleType The desired scaling mode.
   */
  public void setScaleType(ScaleType scaleType) {
    if (scaleType == null) {
      throw new NullPointerException();
    }

    if (mScaleType != scaleType) {
      mScaleType = scaleType;

      setWillNotCacheDrawing(mScaleType == CENTER);

      requestLayout();
      updateScaleType();
      invalidate();
    }
  }

  /**
   * Return the current scale type in use by this ImageView.
   *
   * @see ScaleType
   */
  public ScaleType getScaleType() {
    return mScaleType;
  }

  private void updateScaleType() {
    boolean hasChanged = false;
    if (ScaleType.CENTER_CROP == mScaleType) {

      float scaleX = (float) getWidth() / (float) mVideoWidth;
      float scaleY = (float) getHeight() / (float) mVideoHeight;
      float maxScale = Math.max(scaleX, scaleY);
      scaleX = maxScale / scaleX;
      scaleY = maxScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, getWidth() / 2, getHeight() / 2);
      hasChanged = true;

    } else if (ScaleType.CENTER_INSIDE == mScaleType) {

      float scaleX = (float) mVideoWidth / (float) getWidth();
      float scaleY = (float) mVideoHeight / (float) getHeight();
      float minScale = Math.min(scaleX, scaleY);
      if (minScale > 1) {
        scaleX = scaleX / minScale;
        scaleY = scaleY / minScale;
      }
      mMatrix.setScale(scaleX, scaleY, getWidth() / 2, getHeight() / 2);
      hasChanged = true;

    } else if (ScaleType.CENTER == mScaleType) {

      float sx = (float) mVideoWidth / (float) getWidth();
      float sy = (float) mVideoHeight / (float) getHeight();
      mMatrix.setScale(sx, sy, getWidth() / 2, getHeight() / 2);
      hasChanged = true;

    } else if (ScaleType.FIT_XY == mScaleType) {

      mMatrix.setScale(1, 1, 0, 0);
      hasChanged = true;

    } else if (ScaleType.FIT_START == mScaleType) {

      float scaleX = (float) getWidth() / mVideoWidth;
      float scaleY = (float) getHeight() / mVideoHeight;
      float minScale = Math.min(scaleX, scaleY);
      scaleX = minScale / scaleX;
      scaleY = minScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, 0, 0);
      hasChanged = true;

    } else if (ScaleType.FIT_END == mScaleType) {

      float scaleX = (float) getWidth() / mVideoWidth;
      float scaleY = (float) getHeight() / mVideoHeight;
      float minScale = Math.min(scaleX, scaleY);
      scaleX = minScale / scaleX;
      scaleY = minScale / scaleY;
      mMatrix.setScale(scaleX, scaleY, getWidth(), getHeight());
      hasChanged = true;

    }

    if (hasChanged) {
      setTransform(mMatrix);
    }
  }
}
