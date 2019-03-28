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

  @NonNull
  public String name;

  @NonNull
  public String match;

  @NonNull
  public Behaviour behaviour;

  public boolean locked;

  public Rule() {
    ruleId = UUID.randomUUID();
  }

  @Ignore
  public Rule(@NonNull String name, @NonNull String match, @NonNull Behaviour behaviour, boolean locked) {
    ruleId = UUID.randomUUID();
    this.name = name;
    this.match = match;
    this.behaviour = behaviour;
    this.locked = locked;
  }

  public boolean matches(String number) {
    return match.equals(number);
  }

  public Rule copy() {
    return new Rule(name, match, behaviour, locked);
  }
}
