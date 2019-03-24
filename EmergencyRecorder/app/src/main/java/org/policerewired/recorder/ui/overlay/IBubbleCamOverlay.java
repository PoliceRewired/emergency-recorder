package org.policerewired.recorder.ui.overlay;

import android.net.Uri;

import org.policerewired.recorder.tasks.HybridCollection;

import java.util.Date;

public interface IBubbleCamOverlay {
  void show();
  void showAndRecord();
  void showAndHybrid();
  void hide();

  boolean isShowing();
  boolean isRecording();

  interface Listener {
    void photoCaptured(Date taken, Uri photo);
    void videoCaptured(Date started, Uri video);
    void hybridsCaptured(HybridCollection collection);
  }
}
