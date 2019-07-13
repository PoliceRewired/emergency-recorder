package org.policerewired.recorder.service;

import android.location.Location;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.db.entity.Rule;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
   * Generates a CSV edition of the audit log, stores it in the temporary cache
   * @return a File pointing to the stored audit log
   * @throws IOException if there's an issue creating the file
   */
  File createAuditLogFile() throws IOException;

  /**
   * @return a Live copy of all rules contained by the app.
   */
  LiveData<List<Rule>> getRules_live();

  /**
   * @return a static copy of all rules contained by the app.
   */
  List<Rule> getRules_static();

  /**
   * @param number phone number to query
   * @return all rules for the given number
   */
  List<Rule> getRulesFor_static(String number);

  /**
   * @return a Live copy of all events recorded in the log so far (excluding Debug events).
   */
  LiveData<List<AuditRecord>> getAuditLog_live();

  /**
   * @param chosen_media the media types to select for return
   * @param max the maximum number of events to return
   * @return a Live copy of the most recent MEDIA events recorded in the log so far (excluding Debug events).
   */
  LiveData<List<AuditRecord>> getMediaLog_live_mostRecent(AuditRecordType[] chosen_media, int max);

  /**
   * @return a static copy of all events recorded in the log so far (including Debug events).
   */
  List<AuditRecord> getAuditLog_static();

  /**
   * @return a static copy of all events recorded in the log so far (including Debug events).
   */
  List<AuditRecord> getAuditLog_static(Date from, Date until);

  /**
   * @return the number of entries in the audit log
   */
  long countAuditLog();

  /**
   * @return the number of entries in the audit log
   */
  long countAuditLog(Date from, Date until);

  /**
   * @return the earliest log record
   */
  AuditRecord getEarliestLog();

  /**
   * @return the latest log record
   */
  AuditRecord getLatestLog();

  /**
   * Zip up the audit log between the specified dates, and then share it.
   */
  void zipAndShareAuditLog(Date from, Date to);

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
   * Prepares and stores the photo.
   * @param data raw bytes of the photo, from the camera
   * @param started time the hybrid collection was begun
   * @param taken time the photo was taken
   * @param location location (if available)
   * @param geocode geocoded address (if available)
   * @param w3w what3words triplet (if avaialble)
   * @return uri of the stored photo
   */
  Uri storeHybridPhoto(@NotNull byte[] data, @NotNull Date started, @NotNull Date taken, Location location, String geocode, String w3w);

  /**
   * Stores the video.
   * @param source file where the video is stored
   * @param started time the video recording began
   * @param completed very shortly after the video recording finished
   * @param location location (if available)
   * @param geocode geocoded address (if available)
   * @param w3w what3words triplet (if avaialble)
   * @return uri of the stored photo
   */
  Uri storeVideo(@NotNull File source, @NotNull Date started, @NotNull Date completed, Location location, String geocode, String w3w);
}
