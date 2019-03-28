package org.policerewired.recorder.service;

import android.net.Uri;

import org.policerewired.recorder.db.entity.Rule;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;

public interface IRecorderService {

  void getConfig();
  void setConfig();

  void showOverlay();
  void hideOverlay();

  LiveData<List<Rule>> getRules();
  void delete(Rule rule);
  void insert(Rule rule);
  void update(Rule rule);

  void recordCall(Date initiated, String number);
  void recordPhoto(Date taken, Uri uri);
  void recordHybridPhoto(Date taken, Uri uri);
  void recordHybridVideo(Date started, Uri uri);
  void recordAudio(Date started, Uri uri);
  void recordVideo(Date started, Uri uri);



}
