package org.policerewired.recorder.db.entity;

import org.policerewired.recorder.constants.Behaviour;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "rule")
public class Rule {

  @PrimaryKey
  @NonNull
  public UUID ruleId;

  public String name;
  public String match;
  public Behaviour behaviour;

  public Rule() {
    ruleId = UUID.randomUUID();
  }

  @Ignore
  public Rule(String name, String match, Behaviour behaviour) {
    ruleId = UUID.randomUUID();
    this.name = name;
    this.match = match;
    this.behaviour = behaviour;
  }
}
