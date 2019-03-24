package org.policerewired.recorder.service;

import android.net.Uri;

import java.util.Date;

public interface IRecorderService {

  void getConfig();
  void setConfig();

  void showOverlay();
  void hideOverlay();

  void recordCall(Date initiated, String number);
  void recordPhoto(Date taken, Uri uri);
  void recordHybridPhoto(Date taken, Uri uri);
  void recordHybridVideo(Date started, Uri uri);
  void recordAudio(Date started, Uri uri);
  void recordVideo(Date started, Uri uri);

}
