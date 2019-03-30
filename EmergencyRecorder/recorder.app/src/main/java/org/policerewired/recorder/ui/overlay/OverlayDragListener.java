package org.policerewired.recorder.ui.overlay;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * A drag listener that allows drags on particular views to affect their container rather than
 * themselves.
 */
public class OverlayDragListener implements View.OnTouchListener {
  private static final String TAG = "ODL";

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

  float dX; // adjustment
  float dY; // adjustment

  int lastAction;

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
        if (lastAction == MotionEvent.ACTION_DOWN) {
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
