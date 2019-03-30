package org.policerewired.recorder.ui.overlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
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
  private Listener listener;

  private boolean created;

  private LauncherConfig config; // future planning

  private State state = State.Hidden;

  public LauncherOverlay(Context context, Listener listener, LauncherConfig config) {
    this.context = context;
    this.listener = listener;
    this.config = config;
    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    context.setTheme(R.style.AppTheme);
  }

  @Override
  public void show() {
    if (!created) {
      onCreate();
    }
    if (!isShowing()) {
      setState(State.Showing);
    }
  }

  @Override
  public void hide() {
    if (isShowing()) {
      setState(State.Hidden);
    }
  }

  @OnClick({R.id.icon_launch, R.id.layout_overlay})
  public void launcher_click() {
    listener.launchSelected();
  }

  private void setState(State next) {
    if (!state.showing && next.showing) {
      windowManager.addView(overlay, overlay_params);
    }
    if (state.showing && !next.showing) {
      windowManager.removeView(overlay);
    }
    state = next;
  }

  /**
   * Inflates the layout for this overlay, sets the drag listeners, and default window behaviours.
   */
  @SuppressLint("RtlHardcoded")
  public void onCreate() {
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

    overlay_params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

    created = true;
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
