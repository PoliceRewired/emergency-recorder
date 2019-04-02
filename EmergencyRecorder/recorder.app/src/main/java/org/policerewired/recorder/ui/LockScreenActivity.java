package org.policerewired.recorder.ui;

import android.os.Build;
import android.os.Bundle;

import org.policerewired.recorder.R;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

/**
 * Activity able to be displayed OVER the lock screen. Not currently required.
 */
@Deprecated
public class LockScreenActivity extends AbstractRecorderActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().addFlags(FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true);
    } else {
      getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);
    }
  }

  @Override
  protected int getLayoutId() {
    return R.layout.activity_lock_screen;
  }

  @Override
  protected void updateUI() {
    if (bound) {
      // todo
    }
  }
}
