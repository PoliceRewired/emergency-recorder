package org.policerewired.recorder.db.entity;

import org.policerewired.recorder.constants.RecordType;

import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an entry in the app's log - showing what activities took place for user reference.
 */
@Entity(tableName = "recording")
public class Recording {

  @PrimaryKey
  @NonNull
  public UUID recordingId;

  public Date started;
  public String data;
  public RecordType type;

  public Recording() {
    recordingId = UUID.randomUUID();
  }

}
