package org.policerewired.recorder.ui.overlay;

public interface IBubbleCamOverlay {
  void show();
  void showAndRecord();
  void showAndHybrid();
  void hide();

  boolean isShowing();
  boolean isRecording();

  interface Listener {
    void photoCaptured();
    void videoCaptured();
    void hybridsCaptured(HybridCollection collection);
  }
}
