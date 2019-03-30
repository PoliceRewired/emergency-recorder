package org.policerewired.recorder.tasks;

/**
 * A utility class for counting notification ids - to prevent clashes and repeats.
 */
public class AllNotifications {

  private static int NextNotificationId = 100;
  public static int getNextNotificationId() {
    return NextNotificationId++;
  }

}
