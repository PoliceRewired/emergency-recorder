package org.policerewired.recorder.ui.overlay;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.camerakit.CameraKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.tasks.HybridCollection;
import org.policerewired.recorder.R;
import org.policerewired.recorder.util.CapturePhotoUtils;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import androidx.cardview.widget.CardView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BubbleCamOverlay implements IBubbleCamOverlay {
  private static final String TAG = BubbleCamOverlay.class.getSimpleName();

  @BindView(R.id.layout_overlay) RelativeLayout layout;
  @BindView(R.id.card_video) CardView card_video;
  @BindView(R.id.text_state) TextView text_state;
  @BindView(R.id.fab_control_video) FloatingActionButton fab_control_video;
  @BindView(R.id.fab_control_photo) FloatingActionButton fab_control_photo;
  @BindView(R.id.fab_control_hybrid) FloatingActionButton fab_control_hybrid;
  @BindView(R.id.camera_kit) CameraKitView camera_kit;
  @BindView(R.id.icon_close) ImageView icon_close;

  private MediaRecorder audio_recorder;
  private File audio_file;

  private View overlay;
  private WindowManager.LayoutParams overlay_params;
  private WindowManager windowManager;
  private Context context;
  private Listener listener;

  private NamingUtils naming;
  private StorageUtils storage;

  private boolean created;

  private BubbleCamConfig config;

  private HybridCollection hybridCollection;

  private State state = State.Hidden;

  public BubbleCamOverlay(Context context, Listener listener, BubbleCamConfig config) {
    this.context = context;
    this.listener = listener;
    this.config = config;
    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    context.setTheme(R.style.AppTheme);
    this.naming = new NamingUtils(context);
    this.storage = new StorageUtils(context);
  }

  @OnClick(R.id.fab_control_video)
  public void control_video_click() {
    if (state.mayVideo) {
      setState(state.recording ? State.Ready : State.Recording);
    }
  }

  @OnClick(R.id.fab_control_hybrid)
  public void control_hybrid_click() {
    if (state.mayHybrid) {
      setState(state.hybriding ? State.Ready : State.Hybrid);
    }
  }

  @OnClick(R.id.fab_control_photo)
  public void control_photo_click() {
    if (state.mayPhoto) {
      card_video.setCardBackgroundColor(context.getColor(R.color.colorPhoto));
      camera_kit.captureImage(photo_callback);
    }
  }

  @OnClick(R.id.icon_close)
  public void close_click() {
    setState(State.Hidden);
  }

  private void takeHybridImage() {
    if (state.hybriding) {
      card_video.setCardBackgroundColor(context.getColor(R.color.colorHybrid));
      camera_kit.captureImage(hybrid_callback);
    }
  }

  @Override
  public boolean isShowing() { return state.visible; }

  @Override
  public boolean isRecording() { return state.recording; }

  public void onCreate() {
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    overlay = inflater.inflate(R.layout.overlay_bubble_cam, null);
    ButterKnife.bind(this, overlay);

    OverlayDragListener drag = new OverlayDragListener(layout, windowManager);
    layout.setOnTouchListener(drag);
    card_video.setOnTouchListener(drag);
    camera_kit.setOnTouchListener(drag);

    int window_type;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      window_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      window_type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }

    overlay_params = new WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      window_type,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
      , PixelFormat.TRANSLUCENT);

    overlay_params.gravity = Gravity.START | Gravity.TOP;
    overlay_params.x = 0;
    overlay_params.y = 0;

    created = true;
  }

  public void setState(State next) {
    if (state == next) { return; }

    if (!state.visible && next.visible) {
      windowManager.addView(overlay, overlay_params);
      camera_kit.onStart();
    }

    if (!state.recording && next.recording) {
      camera_kit.captureVideo(video_callback);
    }

    if (state.recording && !next.recording) {
      camera_kit.stopVideo();
    }

    if (!state.hybriding && next.hybriding) {
      startHybrid();
    }

    if (state.hybriding && !next.hybriding) {
      stopHybrid();
    }

    if (state.visible && !next.visible) {
      camera_kit.onStop();
      windowManager.removeView(overlay);
    }

    state = next;
    updateUI();
  }

  private void startHybrid() {
    try {
      audio_recorder = new MediaRecorder();
      audio_file = storage.tempAudioFile(context, ".3gpp");
      audio_recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT); // should be ok?
      audio_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      audio_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
      audio_recorder.setOutputFile(audio_file.getPath());
      audio_recorder.prepare();
      audio_recorder.start();

    } catch (IOException e) {
      Log.e(TAG, "Unable to initiate Media Recorder");
    }

    hybridCollection = new HybridCollection(config.hybrid_delay_ms);
    takeHybridImage();
    scheduleNextHybridImage();
  }

  private void stopHybrid() {
    try {
      audio_recorder.stop();
      audio_recorder.release();
    } catch (Exception e) {
      Log.e(TAG, "Difficulties releasing audio.");
    } finally {
      audio_recorder = null;
    }

    hybridCollection.audio_file = audio_file;
    listener.hybridsCaptured(hybridCollection);
  }

  private void scheduleNextHybridImage() {
    camera_kit.postDelayed(() -> {
      if (state.hybriding) {
        takeHybridImage();
        scheduleNextHybridImage();
      }
    }, config.hybrid_delay_ms);
  }

  private CameraKitView.ImageCallback photo_callback = new CameraKitView.ImageCallback() {
    @Override
    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
      Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      Date now = new Date();
      String title = naming.generate_photo_title(now);
      String description = naming.generate_photo_description(now);
      Uri uri = CapturePhotoUtils.insertImage(context.getContentResolver(), bmp, title, description, now);
      resetFrameEdge();
      listener.photoCaptured(now, uri);
    }
  };

  private CameraKitView.ImageCallback hybrid_callback = new CameraKitView.ImageCallback() {
    @Override
    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
      hybridCollection.image_callback.onImage(cameraKitView, bytes);
      resetFrameEdge();
    }
  };

  @SuppressWarnings("Convert2Lambda")
  private CameraKitView.VideoCallback video_callback = new CameraKitView.VideoCallback() {
    @Override
    public void onVideo(CameraKitView cameraKitView, Object o) {
      Log.w(TAG, "CameraKit does not support video recording, as of v1.0.0 beta 3.11 - this method will not be called.");
      // TODO: when CameraKit supports video, return and update this method to store the video
      listener.videoCaptured(null, null); // this won't be called until camerakit supports video
    }
  };

  private void updateUI() {
    if (!created || !isShowing()) { return; }

    if (state.visible) {
      fab_control_photo.setVisibility(config.mayShowPhoto ? VISIBLE : GONE);
      fab_control_video.setVisibility(config.mayShowVideo ? VISIBLE : GONE);
      fab_control_hybrid.setVisibility(config.mayShowHybrid ? VISIBLE : GONE);
      text_state.setText(state.resDescription);
      text_state.setBackgroundColor(context.getColor(state.resColor));
    }

    fab_control_photo.setEnabled(state.mayPhoto);
    fab_control_hybrid.setEnabled(state.mayHybrid);
    fab_control_video.setEnabled(state.mayVideo);

    fab_control_photo.setBackgroundTintList(ColorStateList.valueOf(context.getColor(state.mayPhoto ? R.color.colorPhoto : R.color.colorDisabled)));
    fab_control_video.setBackgroundTintList(ColorStateList.valueOf(context.getColor(state.mayVideo ? R.color.colorVideo : R.color.colorDisabled)));
    fab_control_hybrid.setBackgroundTintList(ColorStateList.valueOf(context.getColor(state.mayHybrid ? R.color.colorHybrid : R.color.colorDisabled)));

    fab_control_video.setImageResource(state.recording ? R.drawable.ic_stop_black_24dp : R.drawable.ic_fiber_manual_record_black_24dp);
    fab_control_hybrid.setImageResource(state.hybriding ? R.drawable.ic_stop_black_24dp : R.drawable.ic_burst_mode_black_24dp);
  }

  @Override
  public void hide() {
    if (isShowing()) {
      setState(State.Hidden);
    }
  }

  @Override
  public void show() {
    if (!created) { onCreate(); }
    resetFrameEdge();
    if (!isShowing()) {
      setState(State.Ready);
    }
  }

  private void resetFrameEdge() {
    card_video.setCardBackgroundColor(context.getColor(android.R.color.white));
  }

  @Override
  public void showAndRecord() {
    show();
    setState(State.Recording);
  }

  @Override
  public void showAndHybrid() {
    show();
    setState(State.Hybrid);
  }

  private enum State {
    Hidden(false, false, false, 0, 0, false, false, false),
    Ready(true, false, false, R.string.state_ready, R.color.colorDisabled, true, true, true),
    Recording(true, true, false, R.string.state_recording, R.color.colorVideo, false, true, false),
    Hybrid(true, false, true, R.string.state_hybrid, R.color.colorHybrid, false, false, true);

    public final boolean visible;
    public final boolean recording;
    public final boolean hybriding;

    public final int resDescription;
    public final int resColor;

    public final boolean mayPhoto;
    public final boolean mayVideo;
    public final boolean mayHybrid;

    State(boolean visible, boolean recording, boolean hybriding, int resDescription, int resColor, boolean mayPhoto, boolean mayVideo, boolean mayHybrid) {
      this.visible = visible;
      this.recording = recording;
      this.hybriding = hybriding;
      this.resDescription = resDescription;
      this.resColor = resColor;
      this.mayPhoto = mayPhoto;
      this.mayVideo = mayVideo;
      this.mayHybrid = mayHybrid;
    }
  }

}
