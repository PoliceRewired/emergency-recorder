package org.policerewired.recorder.ui.overlay;

import android.net.Uri;

import org.policerewired.recorder.tasks.HybridCollection;

import java.util.Date;

/**
 * Interface for the BubbleCamOverlay - with key methods called from the RecorderService.
 */
public interface IBubbleCamOverlay {

  /**
   * Displays the overlay.
   */
  void show();

  /**
   * Displays the overlay, and initiates recording immediately.
   */
  void showAndRecord();

  /**
   * Display the overlay, and initiates burst-mode (hybrid) photos + audio recording immediately.
   */
  void showAndHybrid();

  /**
   * Hides the overlay.
   */
  void hide();

  /**
   * @return true if the overlay is currently visible to the user.
   */
  boolean isShowing();

  /**
   * @return true if the overlay is currently engaged in recording activity.
   */
  boolean isRecording();

  interface Listener {
    void photoCaptured(Date taken, Uri photo);
    void videoCaptured(Date started, Uri video);
    void hybridPhotoCaptured(Date taken, Uri photo);
    void hybridsCaptured(HybridCollection collection);
  }
}
