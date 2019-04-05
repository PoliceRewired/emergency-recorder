package org.policerewired.recorder.ui.overlay;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.policerewired.recorder.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The overlay containing a small, unobtrusive button that can launch the bubble cam overlay.
 */
public class LauncherOverlay implements ILauncherOverlay {
  private static final String TAG = LauncherOverlay.class.getSimpleName();

  @BindView(R.id.layout_overlay) FrameLayout layout;
  @BindView(R.id.icon_launch) ImageView icon_launch;

  private View overlay;
  private WindowManager.LayoutParams overlay_params;
  private WindowManager windowManager;
  private Context context;
  private Handler handler;
  private Listener listener;

  private boolean initialised;

  private LauncherConfig config;

  private State state = State.Hidden;

  public LauncherOverlay(Context context, Listener listener, LauncherConfig config) {
    this.context = context;
    this.listener = listener;
    this.config = config;
    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    this.handler = new Handler(context.getMainLooper());
    context.setTheme(R.style.AppThemeTransparent);
  }

  @Deprecated
  public void setContext(Context context) {
    this.context = context;
    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    context.setTheme(R.style.AppThemeTransparent);
  }

  @Override
  public void show() {
    if (!isShowing() && isInitialised()) {
      setState(State.Showing);
    }
  }

  @Override
  public void hide() {
    if (isShowing() && isInitialised()) {
      setState(State.Hidden);
    }
  }

  @Override
  public boolean isInitialised() {
    return initialised;
  }

  private void setState(State next) {
    if ((state == null || !state.showing) && next.showing) {

      slide_out(config.fast_slide_ms);
      schedule_slide_in(config.normal_dismiss_ms);
    }

    if ((state == null || state.showing) && !next.showing) {
      slide_in(config.slow_slide_ms);
    }

    state = next;
  }

  private void slide_out(long duration) {
    float newX = 0f;
    ObjectAnimator animation = ObjectAnimator.ofFloat(layout, "translationX", newX);
    animation.setDuration(duration);
    animation.start();
  }

  private void schedule_slide_in(long delay) {
    handler.removeCallbacks(runnable_hide);
    handler.postDelayed(runnable_hide, delay);
  }

  private void slide_in(long duration) {
    float newX = -icon_launch.getWidth();
    ObjectAnimator animation = ObjectAnimator.ofFloat(layout, "translationX", newX);
    animation.setDuration(duration);
    animation.start();
  }

  @Override
  public void indicate() {
    slide_out(config.fast_slide_ms);
    handler.postDelayed(
      () -> slide_in(config.fast_slide_ms),
      config.fast_slide_ms + config.fast_dismiss_ms);

  }

  private Runnable runnable_hide = () -> hide();

  @OnClick({R.id.icon_launch, R.id.layout_overlay})
  public void launcher_click() {
    if (state.showing) {
      listener.launchSelected();
    } else {
      setState(State.Showing);
    }
  }

  /**
   * Inflates the layout for this overlay, sets the drag listeners, and default window behaviours.
   */
  @SuppressLint("RtlHardcoded")
  public void init() {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    overlay = inflater.inflate(R.layout.overlay_launcher, null);
    ButterKnife.bind(this, overlay);

    OverlayDragListener drag = new OverlayDragListener(layout, windowManager);
    drag.setPermitDragX(false);
    layout.setOnTouchListener(drag);
    icon_launch.setOnTouchListener(drag);

    int window_type;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      window_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      window_type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }

    // similar properties to the BubbleCamOverlay, but doesn't keep the screen on
    overlay_params = new WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      window_type,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // cannot receive keypresses
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL // passes external taps to other windows
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED // deprecated for O+, use setShowWhenLocked()
      , PixelFormat.TRANSLUCENT);

    overlay_params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

    initialised = true;

    windowManager.addView(overlay, overlay_params);
    setState(State.Hidden);
  }

  @Override
  public boolean isShowing() {
    return state.showing;
  }

  private enum State {
    Hidden(false),
    Showing(true);

    public boolean showing;

    State(boolean showing) {
      this.showing = showing;
    }
  }
}
