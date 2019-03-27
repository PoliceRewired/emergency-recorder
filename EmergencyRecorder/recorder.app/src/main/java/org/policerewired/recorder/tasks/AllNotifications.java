package org.policerewired.recorder.tasks;

public class AllNotifications {

  private static int NextNotificationId = 100;
  public static int getNextNotificationId() {
    return NextNotificationId++;
  }

}
