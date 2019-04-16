package org.policerewired.recorder.service;

import android.location.Location;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.db.entity.AuditRecord;
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
   * @return a Live copy of all events recorded in the log so far.
   */
  LiveData<List<AuditRecord>> getAuditLog_live();

  /**
   * @return a static copy of all events recorded in the log so far.
   */
  List<AuditRecord> getAuditLog_static();

  /**
   * Removes a rule.
   */
  void delete(@NotNull Rule rule);

  /**
   * Stores a new rule.
   */
  void insert(@NotNull Rule rule);

  /**
   * Updates an existing rule.
   */
  void update(@NotNull Rule rule);

  /**
   * Deletes all entries in the app's audit log.
   */
  void deleteEntireAuditLog();

  /**
   * Log that an outgoing call matching one of the app's rules was initiated.
   */
  void recordCall(@NotNull Date initiated, @NotNull String number);

  /**
   * Log that a photo was taken, initiated by the user.
   */
  void recordPhoto(@NotNull Date taken, @NotNull Uri uri);

  /**
   * Log that a photo was taken, as part of a hybrid collection.
   */
  void recordHybridPhoto(@NotNull Date taken, @NotNull Uri uri);

  /**
   * Log that a video was composed from photos taken as part of a hybrid collection.
   */
  void recordHybridVideo(@NotNull Date started, @NotNull Uri uri);

  /**
   * Log that an audio recording was completed.
   */
  void recordAudio(@NotNull Date started, @NotNull Uri uri);

  /**
   * Log that a video recording was completed.
   */
  void recordVideo(@NotNull Date started, @NotNull Uri uri);

  /**
   * Prepares the photo with visible annotations, and stores it.
   * @param data raw bytes of the photo, from the camera
   * @param taken time the photo was taken
   * @param location location (if available)
   * @param geocode geocoded address (if available)
   * @param w3w what3words triplet (if avaialble)
   * @return uri of the stored photo
   */
  Uri storeUserPhoto(@NotNull byte[] data, @NotNull Date taken, Location location, String geocode, String w3w);

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
  Uri storeHybridPhoto(@NotNull byte[] data, @NotNull Date started, @NotNull Date taken, Location location, String geocode, String w3w);

}
