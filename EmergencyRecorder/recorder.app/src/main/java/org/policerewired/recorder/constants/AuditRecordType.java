package org.policerewired.recorder.constants;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.policerewired.recorder.R;

/**
 * Various types of audit record that can appear in the app's audit log.
 */
public enum AuditRecordType {

  OutgoingCall(R.drawable.ic_call_black_24dp, R.color.colorPrimaryDark, R.string.event_record_outgoing_call, false, null, null, null),
  Photo(R.drawable.ic_image_black_24dp, R.color.colorPhoto, R.string.event_record_photo, true, "image/jpeg", "image/*", "jpg"),
  VideoRecording(R.drawable.ic_videocam_black_24dp, R.color.colorVideo, R.string.event_record_video_recording, true, "video/mp4", "video/*", "mp4"),
  BurstModePhoto(R.drawable.ic_burst_mode_black_24dp, R.color.colorHybrid, R.string.event_record_burst_mode_photo, true, "image/jpeg", "image/*", "jpg"),
  BurstModeVideo(R.drawable.ic_videocam_black_24dp, R.color.colorVideo, R.string.event_record_burst_mode_video, true, "video/mp4", "video/*", "mp4"),
  AudioRecording(R.drawable.ic_mic_black_24dp, R.color.colorAudio, R.string.event_record_audio_recording, true, "audio/m4a", "audio/*", "m4a"),
  Audit(R.drawable.ic_settings_black_24dp, R.color.colorDisabled, R.string.event_record_audit, false, null, null, null),
  Debug(R.drawable.ic_settings_black_24dp, R.color.colorDisabled, R.string.event_record_debug, false, null, null, null);

  /**
   * Icon used to represent this type of audit record.
   */
  public final int icon_id;

  /**
   * Colour used to represent this type of media / auditable event.
   */
  public final int colour_id;

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

  /**
   * Suffix for filenames generated to contain this type of media.
   */
  public final String filename_suffix;

  AuditRecordType(@DrawableRes int icon, @ColorRes int colour, @StringRes int description, boolean media, String mime, String mime_generic, String suffix) {
    this.description_id = description;
    this.icon_id = icon;
    this.colour_id = colour;
    this.is_media = media;
    this.mime_type = mime;
    this.generic_mime_type = mime_generic;
    this.filename_suffix = suffix;
  }
}
