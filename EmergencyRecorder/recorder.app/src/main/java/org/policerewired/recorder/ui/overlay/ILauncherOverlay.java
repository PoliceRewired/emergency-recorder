package org.policerewired.recorder.ui.overlay;

/**
 * Interface for the LauncherOverlay - with key methods called from the RecorderService.
 */
public interface ILauncherOverlay {

  /**
   * Display the overlay - a small unobtrusive button that can launch the bubble cam overlay.
   */
  void show();

  /**
   * Hide the overlay.
   */
  void hide();

  /**
   * @return true if the overlay is visible to the user.
   */
  boolean isShowing();

  public interface Listener {
    void launchSelected();
  }
}
