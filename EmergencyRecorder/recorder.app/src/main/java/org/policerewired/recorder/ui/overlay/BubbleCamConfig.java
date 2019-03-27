package org.policerewired.recorder.ui.overlay;

public class BubbleCamConfig {

  public boolean mayShowVideo;
  public boolean mayShowPhoto;
  public boolean mayShowHybrid;
  public long hybrid_delay_ms;

  private BubbleCamConfig(boolean p, boolean v, boolean h, long ms_h) {
    mayShowPhoto = p;
    mayShowVideo = v;
    mayShowHybrid = h;
    hybrid_delay_ms = ms_h;
  }

  public static BubbleCamConfig defaults() {
    return new BubbleCamConfig(true, false, true, 2000);
  }

}
