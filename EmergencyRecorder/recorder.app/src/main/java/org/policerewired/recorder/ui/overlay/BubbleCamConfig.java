package org.policerewired.recorder.ui.overlay;

import android.content.Context;

import org.policerewired.recorder.R;

/**
 * Configuration for the bubble cam (which buttons to show, the various intervals for different
 * recurring processes).
 */
public class BubbleCamConfig {

  public boolean mayShowVideo;
  public boolean mayShowPhoto;
  public boolean mayShowHybrid;
  public long hybrid_interval_ms;
  public long location_interval_ms;

  private BubbleCamConfig(boolean p, boolean v, boolean h, long ms_h, long ms_l) {
    mayShowPhoto = p;
    mayShowVideo = v;
    mayShowHybrid = h;
    hybrid_interval_ms = ms_h;
    location_interval_ms = ms_l;
  }

  public static BubbleCamConfig defaults(Context context) {
    return new BubbleCamConfig(
      context.getResources().getBoolean(R.bool.supports_photo_mode),
      context.getResources().getBoolean(R.bool.supports_video_mode),
      context.getResources().getBoolean(R.bool.supports_hybrid_mode),
      context.getResources().getInteger(R.integer.hybrid_interval_ms),
      context.getResources().getInteger(R.integer.location_interval_ms));
  }

}
