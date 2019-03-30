package org.policerewired.recorder.service;

import android.location.Location;
import android.net.Uri;

import org.policerewired.recorder.db.entity.Rule;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Core interface to the Recorder Service - these methods are available to bound activities.
 */
public interface IRecorderService {

  void getConfig();
  void setConfig();

  /**
   * Displays the overlay to the user.
   */
  void showOverlay();

  /**
   * Reevaluate behaviour/visible overlays based on permissions.
   */
  void onPermissionsUpdated();

  /**
   * Hides the overlay from the user.
   */
  void hideOverlay();

  /**
   * @return a Live copy of all rules contained by the app.
   */
  LiveData<List<Rule>> getRules();

  /**
   * Removes a rule.
   */
  void delete(Rule rule);

  /**
   * Stores a new rule.
   */
  void insert(Rule rule);

  /**
   * Updates an existing rule.
   */
  void update(Rule rule);

  /**
   * Log that an outgoing call matching one of the app's rules was initiated.
   */
  void recordCall(Date initiated, String number);

  /**
   * Log that a photo was taken, initiated by the user.
   */
  void recordPhoto(Date taken, Uri uri);

  /**
   * Log that a photo was taken, as part of a hybrid collection.
   */
  void recordHybridPhoto(Date taken, Uri uri);

  /**
   * Log that a video was composed from photos taken as part of a hybrid collection.
   */
  void recordHybridVideo(Date started, Uri uri);

  /**
   * Log that an audio recording was completed.
   */
  void recordAudio(Date started, Uri uri);

  /**
   * Log that a video recording was completed.
   */
  void recordVideo(Date started, Uri uri);

  /**
   * Prepares the photo with visible annotations, and stores it.
   * @param data raw bytes of the photo, from the camera
   * @param taken time the photo was taken
   * @param location location (if available)
   * @param geocode geocoded address (if available)
   * @param w3w what3words triplet (if avaialble)
   * @return uri of the stored photo
   */
  Uri storeUserPhoto(byte[] data, Date taken, Location location, String geocode, String w3w);

  /**
   * Prepares the photo with visible annotations, and stores it.
   * @param data raw bytes of the photo, from the camera
   * @param started time the hybrid collection was begun
   * @param taken time the photo was taken
   * @param location location (if available)
   * @param geocode geocoded address (if available)
   * @param w3w what3words triplet (if avaialble)
   * @return uri of the stored photo
   */
  Uri storeHybridPhoto(byte[] data, Date started, Date taken, Location location, String geocode, String w3w);

}
