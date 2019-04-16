package org.policerewired.recorder.db.entity;

import org.policerewired.recorder.constants.AuditRecordType;

import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an entry in the app's log - showing what activities took place for user reference.
 */
@Entity(tableName = "audit")
public class AuditRecord {

  @PrimaryKey
  @NonNull
  public UUID recordingId;

  public Date started;
  public String data;
  public AuditRecordType type;

  public AuditRecord() {
    recordingId = UUID.randomUUID();
  }

}
