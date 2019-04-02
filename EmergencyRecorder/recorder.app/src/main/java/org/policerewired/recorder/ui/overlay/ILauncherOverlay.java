package org.policerewired.recorder.ui.overlay;

/**
 * Interface for the LauncherOverlay - with key methods called from the RecorderService.
 */
public interface ILauncherOverlay {

  /**
   * Initialise and display the view as a stub.
   */
  void init();

  /**
   * Display the overlay - a small unobtrusive button that can launch the bubble cam overlay.
   */
  void show();

  /**
   * Hide the overlay.
   */
  void hide();

  /**
   * Inidicate presence to the user.
   */
  void indicate();

  /**
   * @return true if the launcher was able to initialise a view.
   */
  boolean isInitialised();

  /**
   * @return true if the overlay is visible to the user.
   */
  boolean isShowing();

  public interface Listener {
    void launchSelected();
  }
}
