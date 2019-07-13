package org.policerewired.recorder.ui.overlay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Mode;

import org.jcodec.common.StringUtils;
import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.service.RecorderService;
import org.policerewired.recorder.tasks.AbstractGeocodingTask;
import org.policerewired.recorder.tasks.AbstractWhat3WordsTask;
import org.policerewired.recorder.tasks.HybridCollection;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.PhotoAnnotationUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * The overlay containing the camera view, buttons, and location information a user sees.
 */
@SuppressWarnings("FieldCanBeLocal")
public class BubbleCamOverlay implements IBubbleCamOverlay {
  private static final String TAG = BubbleCamOverlay.class.getSimpleName();

  @BindView(R.id.layout_overlay) RelativeLayout layout;
  @BindView(R.id.card_video) CardView card_video;
  @BindView(R.id.text_state) TextView text_state;
  @BindView(R.id.fab_control_video) FloatingActionButton fab_control_video;
  @BindView(R.id.fab_control_photo) FloatingActionButton fab_control_photo;
  @BindView(R.id.fab_control_hybrid) FloatingActionButton fab_control_hybrid;
  @BindView(R.id.camera_view) CameraView camera;
  @BindView(R.id.icon_close) ImageView icon_close;
  @BindView(R.id.text_location) TextView text_location;
  @BindView(R.id.text_w3w) TextView text_w3w;

  private MediaRecorder audio_recorder;
  private File audio_file;

  private File video_file;
  private Date video_started;

  private View overlay;
  private WindowManager.LayoutParams overlay_params;
  private WindowManager windowManager;
  private Context context;
  private IRecorderService service;
  private Handler handler;
  private Listener listener;

  private NamingUtils naming;
  private PhotoAnnotationUtils annotation;
  private StorageUtils storage;

  private boolean created;

  private FusedLocationProviderClient fusedLocationClient;
  private LocationCallback locationCallback;
  private Location lastLocation;
  private String lastGeocode;
  private String lastW3W;
  private Date locationUpdated;
  private AbstractGeocodingTask geocoder;
  private AbstractWhat3WordsTask what3wordser;

  private BubbleCamConfig config;

  private HybridCollection hybridCollection;

  private State state = State.Hidden;

  public BubbleCamOverlay(RecorderService service, Listener listener, BubbleCamConfig config) {
    this.service = service;
    this.context = service;
    this.handler = new Handler(context.getMainLooper());
    this.listener = listener;
    this.config = config;
    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    context.setTheme(R.style.AppTheme);
    this.naming = new NamingUtils(context);
    this.annotation = new PhotoAnnotationUtils(context);
    this.storage = new StorageUtils(context);
    this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
      camera.setMode(Mode.PICTURE);
      camera.takePicture();
    }
  }

  @OnClick(R.id.icon_close)
  public void close_click() {
    setState(State.Hidden);
  }

  private void takeHybridImage() {
    if (state.hybriding) {
      card_video.setCardBackgroundColor(context.getColor(R.color.colorHybrid));
      camera.takePicture();
    }
  }

  @Override
  public boolean isShowing() {
    return state.visible;
  }

  @Override
  public boolean isRecording() {
    return state.recording;
  }

  /**
   * Inflates the layout for this overlay, sets the drag listeners, and default window behaviours.
   */
  @SuppressLint("ClickableViewAccessibility")
  public void onCreate() {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    overlay = inflater.inflate(R.layout.overlay_bubble_cam, null);
    ButterKnife.bind(this, overlay);

    OverlayDragListener drag = new OverlayDragListener(layout, windowManager);
    layout.setOnTouchListener(drag);
    card_video.setOnTouchListener(drag);
    camera.setOnTouchListener(drag);

    camera.addCameraListener(camera_listener);

    int window_type;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      window_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      window_type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }

    overlay_params = new WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      window_type,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // cannot receive keypresses
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL // passes external taps to other windows
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED // ignored for O+
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON // turns on the screen if activated
        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON // screen does not dim or time-out
      , PixelFormat.TRANSLUCENT);

    // FLAG_SHOW_WHEN_LOCKED is deprecated from API 27 (Android O)
    // FLAG_DISMISS_KEYGUARD was deprecated from API 26 (and not that useful anyway)
    // Best fallback option: use setShowWhenLocked() - a method only available on Activities
    // Can our overlays be Activities?
    //ActivityManager am = (ActivityManager)context.getSystemService(Service.ACTIVITY_SERVICE);
    //am.moveTaskToFront(taskId, flags);

    overlay_params.gravity = Gravity.START | Gravity.TOP;
    overlay_params.x = 0;
    overlay_params.y = 0;

    created = true;
  }

  /**
   * Listener to camera activity
   */
  private CameraListener camera_listener = new CameraListener() {
    @Override
    public void onCameraError(@NonNull CameraException exception) {
      super.onCameraError(exception);
      Log.e(TAG, "Exception received from camera.", exception);
    }

    @Override
    public void onPictureTaken(@NonNull PictureResult result) {
      super.onPictureTaken(result);

      if (state.hybriding) {
        final byte[] data = result.getData();
        final Date now = new Date();
        handler.post(() -> {
          Uri uri = service.storeHybridPhoto(data, now, hybridCollection.started, lastLocation, lastGeocode, lastW3W);
          hybridCollection.addImage(data);
          listener.hybridPhotoCaptured(now, uri);
        });
        resetFrameEdge();

      } else if (state.mayPhoto) {
        final Date now = new Date();
        final byte[] data = result.getData();
        handler.post(() -> {
          Uri uri = service.storeUserPhoto(data, now, lastLocation, lastGeocode, lastW3W);
          listener.photoCaptured(now, uri);
        });
        resetFrameEdge();
      }
    }

    @Override
    public void onVideoTaken(@NonNull VideoResult result) {
      super.onVideoTaken(result);
      if (result == null) {
        Log.w(TAG, "Null result returned through onVideoTaken.");
        EmergencyRecorderApp.recordAnalyticsIssue("Null result returned through onVideoTaken.", result);
        Toast.makeText(context, R.string.toast_warning_null_video_returned_for_storage, Toast.LENGTH_LONG).show();
      }
      if (result.getFile() == null) {
        Log.w(TAG, "Null file returned through onVideoTaken.");
        EmergencyRecorderApp.recordAnalyticsIssue("Null video file returned through onVideoTaken.", result);
        Toast.makeText(context, R.string.toast_warning_null_video_returned_for_storage, Toast.LENGTH_LONG).show();
      }
      Date video_completed = new Date();
      Uri uri = service.storeVideo(result.getFile(), video_started, video_completed, lastLocation, lastGeocode, lastW3W);
      listener.videoCaptured(video_started, uri);
    }
  };

  /**
   * Method called to switch the bubble camera's state.
   * @see org.policerewired.recorder.ui.overlay.BubbleCamOverlay.State
   * @param next the state to switch to
   */
  private void setState(State next) {

    // special case - CameraKit does not yet support video mode, so fallback on Hybrid recording
    if (!context.getResources().getBoolean(R.bool.supports_video_mode) && next == State.Recording) {
      setState(State.Hybrid);
      return;
    }

    if (state == next) {
      return;
    }

    if (!state.visible && next.visible) {
      windowManager.addView(overlay, overlay_params);
      camera.open();
      startLocationUpdates();
    }

    if (!state.recording && next.recording) {
      try {
        startVideo();
      } catch (Exception e) {
        Log.e(TAG, "Unable to start video. Switching to hybrid.", e);
        setState(State.Hybrid);
        return;
      }
    }

    if (state.recording && !next.recording) {
      stopVideo();
    }

    if (!state.hybriding && next.hybriding) {
      startHybrid();
    }

    if (state.hybriding && !next.hybriding) {
      stopHybrid();
    }

    if (state.visible && !next.visible) {
      camera.close();
      windowManager.removeView(overlay);
      stopLocationUpdates();
      listener.closed();
    }

    state = next;
    updateUI();
  }

  /**
   * Initiates the hybrid recording process:
   * <ul>
   *   <li>Initialises and starts audio recording.</li>
   *   <li>Initialises a photo collection.</li>
   *   <li>Takes a photo, and schedules the next.</li>
   * </ul>
   */
  private void startHybrid() {
    try {
      camera.setMode(Mode.PICTURE);
      audio_recorder = new MediaRecorder();
      audio_file = storage.tempAudioFile(".m4a");
      audio_recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT); // should be ok?
      audio_recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      audio_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
      audio_recorder.setOutputFile(audio_file.getPath());
      audio_recorder.prepare();
      audio_recorder.start();

    } catch (IOException e) {
      Log.e(TAG, "Unable to initiate Audio Recorder");
    }

    hybridCollection = new HybridCollection(config.hybrid_interval_ms);
    takeHybridImage();
    scheduleNextHybridImage();
  }

  /**
   * Starts video recording
   * @throws IOException if unable to initiate recording with a temporary video file
   */
  private void startVideo() throws IOException {
    camera.setMode(Mode.VIDEO);
    video_started = new Date();
    video_file = storage.tempVideoFile(".mp4");
    camera.takeVideo(video_file);
  }

  /**
   * Completes video recording.
   */
  private void stopVideo() {
    camera.stopVideo();
  }

  /**
   * Completes the hybrid recording process:
   * <ul>
   *   <li>Stops the audio recording.</li>
   *   <li>Notifies the listener that hybrid collection has completed.</li>
   * </ul>
   * <i>After hybrid recording, the RecorderService starts a process to combine the photos taken
   * into a video, and store the audio taken.</i>
   */
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

  /**
   * Posts a delayed task to take the next hybrid image - and schedule the next after that!
   * NB. hybrid imaging stops when the state changes away from Hybrid.
   */
  private void scheduleNextHybridImage() {
    handler.postDelayed(() -> {
      if (state.hybriding) {
        takeHybridImage();
        scheduleNextHybridImage();
      }
    }, config.hybrid_interval_ms);
  }

  /**
   * Updates the visible state of gadgetry in the overlay based on the current state.
   */
  private void updateUI() {
    if (!created || !isShowing()) {
      return;
    }

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

  /**
   * Utility method to return the background of the main card to white. Called from various places.
   */
  private void resetFrameEdge() {
    card_video.setCardBackgroundColor(context.getColor(android.R.color.white));
  }


  @Override
  public void hide() {
    if (isShowing()) {
      setState(State.Hidden);
    }
  }

  @Override
  public void show() {
    if (!created) {
      onCreate();
    }
    resetFrameEdge();
    if (!isShowing()) {
      setState(State.Ready);
    }
  }

  @Override
  public void showAndRecord() {
    show();
    setState(State.Recording);
  }

  // hello lewis

  @Override
  public void showAndHybrid() {
    show();
    setState(State.Hybrid);
  }

  /**
   * Initiates a location request from the system, with callbacks to the geocoding and what3wordsing
   * methods when there is a location update.
   */
  private void startLocationUpdates() {
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        if (locationResult == null) {
          return;
        }
        if (locationResult.getLastLocation() != null) {
          Log.d(TAG, "Location update received.");
          lastLocation = locationResult.getLastLocation();
          camera.setLocation(lastLocation);
          locationUpdated = new Date();
        }
        if (isShowing()) {
          geocodeAndDisplay();
          w3wAndDisplay();
        }
      }
    };

    LocationRequest request = new LocationRequest();
    request.setPriority(PRIORITY_HIGH_ACCURACY);
    request.setInterval(config.location_interval_ms);

    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "Initiating location updates.");
      fusedLocationClient.requestLocationUpdates(request, locationCallback, null /* Looper */);
      text_location.setVisibility(VISIBLE);
      text_w3w.setVisibility(VISIBLE);
      text_location.setText(R.string.text_blank_location);
      text_w3w.setText(R.string.text_blank_w3w);
    } else {
      Log.w(TAG, "Location permission was not granted.");
      text_location.setText(R.string.text_blank_location);
      text_w3w.setText(R.string.text_blank_w3w);
      text_location.setVisibility(GONE);
      text_w3w.setVisibility(GONE);
    }
  }

  /**
   * Requests a geocoding through Android, and displays the nearest address when available.
   */
  @SuppressLint("StaticFieldLeak")
  private void geocodeAndDisplay() {
    AbstractGeocodingTask.Params param = new AbstractGeocodingTask.Params(lastLocation);

    geocoder = new AbstractGeocodingTask(context) {
      @Override
      protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (result.attempted && result.addresses != null && result.addresses.size() > 0) {

          Address address = result.addresses.get(0);
          String[] addressLines = new String[address.getMaxAddressLineIndex()+1];
          for(int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
            addressLines[line] = address.getAddressLine(line);
          }
          lastGeocode = StringUtils.join2(addressLines, '\n');
          text_location.setText(naming.describeGeocode(lastGeocode));

        } else {
          Log.w(TAG, "No address to display from geocoder.");
        }
      }
    };

    geocoder.execute(param);
  }

  /**
   * Passes the last location to the What3Words API, and displays the nearest word triplet.
   */
  @SuppressLint("StaticFieldLeak")
  private void w3wAndDisplay() {
    AbstractWhat3WordsTask.Params param = new AbstractWhat3WordsTask.Params(lastLocation);

    what3wordser = new AbstractWhat3WordsTask(context) {
      @Override
      protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (result.attempted && result.words.isSuccessful()) {
          lastW3W = result.words.getWords();
          text_w3w.setText(naming.describeW3W(lastW3W));
        } else {
          Log.w(TAG, "What3Words reports unsuccessful.");
        }
      }
    };

    what3wordser.execute(param);
  }

  /**
   * Cancels the location request.
   */
  private void stopLocationUpdates() {
    if (locationCallback != null) {
      fusedLocationClient.removeLocationUpdates(locationCallback);
      this.locationUpdated = null;
      this.lastGeocode = null;
      this.lastLocation = null;
      this.lastW3W = null;
    }
  }

  /**
   * Represents the various states this overlay can be in:
   * <ul>
   *   <li>Hidden: not visible to the user</li>
   *   <li>Ready: now showing the camera, not recording</li>
   *   <li>AuditRecord: recording video</li>
   *   <li>Hybrid: a process to take periodic photos, and record audio - referred to as "burst mode" in user land</li>
   * </ul>
   */
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
