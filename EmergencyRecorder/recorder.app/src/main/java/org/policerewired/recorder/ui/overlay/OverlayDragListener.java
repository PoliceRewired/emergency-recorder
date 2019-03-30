package org.policerewired.recorder.ui.overlay;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * A drag listener that allows drags on particular views to affect their container rather than
 * themselves.
 */
public class OverlayDragListener implements View.OnTouchListener {
  private static final String TAG = OverlayDragListener.class.getSimpleName();

  private View overlay;
  private WindowManager windowManager;

  public OverlayDragListener(View overlay, WindowManager windowManager) {
    this.overlay = overlay;
    this.windowManager = windowManager;
  }

  boolean permitDragX = true;
  boolean permitDragY = true;

  float penDownX;
  float penDownY;

  float originalX;
  float originalY;

  int lastAction;

  float tap_limit = 20.0f;

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    int[] overlayPosition = new int[2];

    //overlay.getLocationOnScreen(overlayPosition);
    //int overlayX = overlayPosition[0];
    //int overlayY = overlayPosition[1];

    WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlay.getLayoutParams();
    int overlayX = params.x;
    int overlayY = params.y;

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        originalX = event.getRawX();
        originalY = event.getRawY();

        penDownX = event.getRawX() - overlayX;
        penDownY = event.getRawY() - overlayY;

        lastAction = MotionEvent.ACTION_DOWN;
        return true;

      case MotionEvent.ACTION_MOVE:
        if (permitDragX) {
          float newDownX = event.getRawX() - overlayX;
          float newX = overlayX + newDownX - penDownX;
          params.x = (int) newX;
        }

        if (permitDragY) {
          float newDownY = event.getRawY() - overlayY;
          float newY = overlayY + newDownY - penDownY;
          params.y = (int) newY;
        }

        if (permitDragX || permitDragY) {
          windowManager.updateViewLayout(overlay, params);
        }
        lastAction = MotionEvent.ACTION_MOVE;
        return true;

      case MotionEvent.ACTION_UP:
        float newDownX = event.getRawX() - overlayX;
        float newDownY = event.getRawY() - overlayY;

        float diffX = event.getRawX() - originalX;
        float diffY = event.getRawY() - originalY;

        Log.v(TAG, "diffX " + diffX + ", diffY" + diffY);

        if (lastAction == MotionEvent.ACTION_DOWN ||
          (Math.abs(diffX) < tap_limit && Math.abs(diffY) < tap_limit)) {
          v.performClick(); // click detected
        }

        lastAction = MotionEvent.ACTION_UP;
        return true;

      default:
        return false;
    }
  }

  public boolean getPermitDragX() { return permitDragX; }
  public boolean getPermitDragY() { return permitDragY; }

  public void setPermitDragX(boolean permit) { permitDragX = permit; }
  public void setPermitDragY(boolean permit) { permitDragY = permit; }

}
