package org.policerewired.recorder.constants;

import org.policerewired.recorder.R;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Various types of audit record that can appear in the app's audit log.
 */
public enum AuditRecordType {

  OutgoingCall(R.drawable.ic_call_black_24dp, R.string.event_record_outgoing_call, false, null, null),
  Photo(R.drawable.ic_camera_black_24dp, R.string.event_record_photo, true, "image/jpeg", "image/*"),
  VideoRecording(R.drawable.ic_videocam_black_24dp, R.string.event_record_video_recording, true, "video/mp4", "video/*"),
  BurstModePhoto(R.drawable.ic_burst_mode_black_24dp, R.string.event_record_burst_mode_photo, true, "image/jpeg", "image/*"),
  BurstModeVideo(R.drawable.ic_videocam_black_24dp, R.string.event_record_burst_mode_video, true, "video/mp4", "video/*"),
  AudioRecording(R.drawable.ic_mic_black_24dp, R.string.event_record_audio_recording, true, "audio/3gpp", "audio/*"),
  Audit(R.drawable.ic_settings_black_24dp, R.string.event_record_audit, false, null, null),
  Debug(R.drawable.ic_settings_black_24dp, R.string.event_record_debug, false, null, null);

  /**
   * Icon used to represent this type of audit record.
   */
  public final int icon_id;

  /**
   * Text used to describe this type of audit record.
   */
  public final int description_id;

  /**
   * True if this record represents a media object that can be shared or viewed.
   */
  public final boolean is_media;

  /**
   * Mime-type of the data.
   */
  public final String mime_type;

  /**
   * Generic mime-type of the data, used for sharing the data to other apps.
   */
  public final String generic_mime_type;

  AuditRecordType(@DrawableRes int icon, @StringRes int description, boolean media, String mime, String mime_generic) {
    this.description_id = description;
    this.icon_id = icon;
    this.is_media = media;
    this.mime_type = mime;
    this.generic_mime_type = mime_generic;
  }
}
