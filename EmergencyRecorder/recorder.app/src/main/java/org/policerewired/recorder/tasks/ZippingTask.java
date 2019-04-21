package org.policerewired.recorder.tasks;

import android.app.NotificationChannel;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.NotificationCompat;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.StorageUtils;
import org.policerewired.recorder.util.Zipper;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Task responsible for zipping up a number of media items, and a log file; the zip file is then
 * made available for sharing.
 */
public class ZippingTask extends AbstractNotifyingAsyncTask<ZippingTask.Params, ZippingTask.Progress, ZippingTask.Result> {
  private static final String TAG = ZippingTask.class.getSimpleName();

  NamingUtils naming;
  StorageUtils storage;

  public ZippingTask(Context context, NotificationChannel channel) {
    super(context, channel);
    naming = new NamingUtils(context);
    storage = new StorageUtils(context);
  }

  @Override
  protected boolean wasSuccess(Result result) {
    return result.areAllSuccessful();
  }

  @Override
  protected String getNotificationTitle() {
    return context.getString(R.string.task_title_zipping);
  }

  @Override
  protected String getNotificationContent() {
    return context.getString(R.string.task_content_zipping);
  }

  @Override
  protected String getNotificationTicker() {
    return null; // no ticker required
  }

  @Override
  protected int getNotificationIcon() {
    return R.mipmap.ic_launcher_foreground;
  }

  @Override
  protected int getNotificationPriority() {
    return NotificationCompat.PRIORITY_MAX;
  }

  @Override
  protected String getCompletionToast() {
    return context.getString(R.string.task_toast_complete_zipping);
  }

  @Override
  protected String getFailureToast() {
    return context.getString(R.string.task_toast_errors_occurred);
  }

  @Override
  protected Result doInBackground(Params... params) {
    Params param = params[0];
    Zipper zipper = new Zipper(context, param.target_zip);
    Result result = new Result(param.target_zip);
    String zip_internal_folder = "log";

    try {
      zipper.open();

      // add the log file
      zipper.addFile(zip_internal_folder, param.log_file);

      // add each piece of media
      int processed = 0;
      for (AuditRecord record : param.source_records) {
        if (record.type.is_media) {
          try {
            Uri uri = Uri.parse(record.data);
            InputStream stream = context.getContentResolver().openInputStream(uri);
            String filename = Zipper.getFilename(record.started, record.type, uri);
            zipper.addStream(stream, zip_internal_folder, filename);
            result.successes.add(record);

          } catch (Exception e) {
            Log.w(TAG, "Exception encountered fetching and zipping media: " + record.data, e);
            result.failures.add(new Pair<>(record, e.getMessage()));
          }
        } // is media

        processed++;
        Progress progress = new Progress();
        progress.processed = processed;
        progress.of = param.source_records.size();
        publishProgress(progress);
      } // each record

      zipper.close();

    } catch (Exception e) {
      Log.e(TAG, "Critical exception encountered zipping media.", e);
      result.critical_exception = e;
    }

    return result;
  }

  public static class Params {
    public List<AuditRecord> source_records;
    public File target_zip;
    public File log_file;
  }

  public static class Progress {
    public int processed;
    public int of;
  }

  public static class Result {
    public Result(File target_zip) {
      this.successes = new LinkedList<>();
      this.failures = new LinkedList<>();
      this.target_zip = target_zip;
    }

    public List<AuditRecord> successes;
    public List<Pair<AuditRecord, String>> failures;
    public File target_zip;
    public Exception critical_exception;

    public boolean areAnySuccessful() {
      return successes.size() > 0 && critical_exception == null;
    }

    public boolean areAllSuccessful() {
      return successes.size() > 0 && failures.size() == 0 && critical_exception == null;
    }
  }

}
