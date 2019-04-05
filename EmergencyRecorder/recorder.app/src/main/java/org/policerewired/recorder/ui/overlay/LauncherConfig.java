package org.policerewired.recorder.ui.overlay;

import android.content.Context;

import org.policerewired.recorder.R;

public class LauncherConfig {

  public long fast_slide_ms;
  public long slow_slide_ms;
  public long normal_dismiss_ms;
  public long fast_dismiss_ms;

  public LauncherConfig(int fast_ms, int slow_ms, int normal_dismiss_ms, int fast_dismiss_ms) {
    this.fast_slide_ms = fast_ms;
    this.slow_slide_ms = slow_ms;
    this.normal_dismiss_ms = normal_dismiss_ms;
    this.fast_dismiss_ms = fast_dismiss_ms;
  }

  public static LauncherConfig defaults(Context context) {
    return new LauncherConfig(
      context.getResources().getInteger(R.integer.launcher_fast_slide_ms),
      context.getResources().getInteger(R.integer.launcher_slow_slide_ms),
      context.getResources().getInteger(R.integer.launcher_normal_dismiss_ms),
      context.getResources().getInteger(R.integer.launcher_fast_dismiss_ms)
    );
  }
}