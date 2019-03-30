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

  float dX;
  float dY;

  float penDownX;
  float penDownY;

  int lastAction;

  @Override
  public boolean onTouch(View v, MotionEvent event) {

    int[] overlayPosition = new int[2];
    overlay.getLocationOnScreen(overlayPosition);
    int overlayX = overlayPosition[0];
    int overlayY = overlayPosition[1];

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        //dX = overlayX - event.getRawX();
        //dY = overlayY - event.getRawY();
        penDownX = event.getRawX() - overlayX;
        penDownY = event.getRawY() - overlayY;

        lastAction = MotionEvent.ACTION_DOWN;
        break;

      case MotionEvent.ACTION_MOVE:

        //float newX = event.getRawX() + dX;
        //float newY = event.getRawY() + dY;
        float motionDownX = event.getRawX() - overlayX;
        float motionDownY = event.getRawY() - overlayY;

        float newX = overlayX + motionDownX - penDownX;
        float newY = overlayY + motionDownY - penDownY;

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlay.getLayoutParams();
        params.x = (int)newX;
        params.y = (int)newY;

        windowManager.updateViewLayout(overlay, params);
        lastAction = MotionEvent.ACTION_MOVE;
        break;

      case MotionEvent.ACTION_UP:
        if (lastAction == MotionEvent.ACTION_DOWN) {
          v.performClick(); // click detected
        }
        break;

      default:
        return false;
    }
    return true;
  }


}
