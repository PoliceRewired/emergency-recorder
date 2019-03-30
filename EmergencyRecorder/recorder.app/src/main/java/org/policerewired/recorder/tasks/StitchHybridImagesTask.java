package org.policerewired.recorder.tasks;

import android.app.NotificationChannel;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.BitmapUtil;
import org.policerewired.recorder.R;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.service.RecorderService;
import org.policerewired.recorder.util.CaptureAudioUtils;
import org.policerewired.recorder.util.CaptureVideoUtils;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.util.Date;

import androidx.core.app.NotificationCompat;

/**
 * Takes all images from a HybridCollection (burst mode), and combines them into a video.
 */
public class StitchHybridImagesTask extends AbstractNotifyingAsyncTask<StitchHybridImagesTask.Params, StitchHybridImagesTask.Progress, StitchHybridImagesTask.Result> {
  private static final String TAG = StitchHybridImagesTask.class.getSimpleName();

  private NamingUtils naming;
  private StorageUtils storage;
  private IRecorderService service;

  public StitchHybridImagesTask(RecorderService service, NotificationChannel channel) {
    super(service, channel);
    this.service = service;
    this.naming = new NamingUtils(context);
    this.storage = new StorageUtils(context);
  }

  @Override
  protected String getNotificationTitle() {
    return context.getString(R.string.task_title_stitching_photos);
  }

  @Override
  protected String getNotificationContent() {
    return context.getString(R.string.task_content_stitching_photos);
  }

  @Override
  protected String getNotificationTicker() {
    return context.getString(R.string.task_ticker_stitching_photos);
  }

  @Override
  protected int getNotificationIcon() {
    return R.drawable.ic_launcher_foreground;
  }

  @Override
  protected int getNotificationPriority() {
    return NotificationCompat.PRIORITY_MAX;
  }

  @Override
  protected String getCompletionToast() {
    return context.getString(R.string.task_toast_complete_stitching);
  }

  @Override
  protected String getFailureToast() {
    return context.getString(R.string.task_toast_failed_stitching);
  }

  @Override
  protected boolean wasSuccess(Result result) {
    return result.success_video;
  }

  @Override
  protected void onPostExecute(Result result) {
    if (result.success_video) {
      try {
        Uri uri = CaptureVideoUtils.insertVideo(
          context.getContentResolver(),
          result.video,
          naming.generate_video_title(result.started),
          naming.generate_video_description(result.started, result.images, result.duration_ms()),
          result.started,
          result.duration_ms());

        service.recordHybridVideo(result.started, uri);

      } catch (Exception e) {
        Log.e(TAG, "Unable to store video.", e);
      }
    }

    try {
      Uri uri = CaptureAudioUtils.insertAudio(
        context.getContentResolver(),
        result.audio,
        storage.externalAudioFile(context, result.started, ".3gpp"),
        naming.generate_audio_title(result.started),
        naming.generate_audio_description(result.started, result.duration_ms()),
        naming.generate_audio_album(result.started),
        result.started,
        result.duration_ms());

      service.recordAudio(result.started, uri);

    } catch (Exception e) {
      Log.e(TAG, "Unable to store audio.", e);
    }

    super.onPostExecute(result);
  }

  @Override
  protected void onProgressUpdate(Progress... values) {
    super.onProgressUpdate(values);

    Progress value = values[0];
    String content = context.getString(
      R.string.task_update_stitching_photos,
      context.getString(R.string.task_content_stitching_photos),
      value.parsed+1,
      value.of);

    updateNotificationText(content);
  }

  @Override
  protected Result doInBackground(Params... params) {
    Params param = params[0];

    try {

      File video_file = storage.tempVideoFile(context, ".mp4");

      SeekableByteChannel channel = NIOUtils.writableChannel(video_file);

      Rational fps = new Rational(1000, (int)param.collection.ms_per_blob);
      SequenceEncoder encoder = new AndroidSequenceEncoder(channel, fps);

      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeByteArray(param.collection.blobs.get(0).second, 0, param.collection.blobs.get(0).second.length, opts);
      int sourceWidth = opts.outWidth;
      int sourceHeight = opts.outHeight;

      int sampleSize = 1;
      while
        ((param.maxWidth != null && sourceWidth/sampleSize > param.maxWidth) ||
        (param.maxHeight != null && sourceHeight/sampleSize > param.maxHeight)) {
        sampleSize *= 2;
      }

      BitmapFactory.Options rescale = new BitmapFactory.Options();
      rescale.inSampleSize = sampleSize;

      int parsed = 0;
      int of = param.collection.blobs.size();

      for (Pair<Date, byte[]> pair : param.collection.blobs) {
        publishProgress(new Progress(parsed, of));
        byte[] blob = pair.second;
        Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length, rescale);
        Picture pic = BitmapUtil.fromBitmap(bmp);
        bmp.recycle();
        encoder.encodeNativeFrame(pic);
      }

      encoder.finish();
      channel.close();

      return new Result(true, video_file, param.collection.audio_file, param.collection.started, param.collection.blobs.size(), param.collection.ms_per_blob);

    } catch (Exception e) {
      Log.e(TAG, "Failed to stitch hybrid images together.", e);
      return new Result(false, null, param.collection.audio_file, param.collection.started, param.collection.blobs.size(), param.collection.ms_per_blob);
    }
  }

  public static class Params {
    HybridCollection collection;
    Integer maxWidth;
    Integer maxHeight;

    public Params(HybridCollection collection, Integer maxWidth, Integer maxHeight) {
      this.collection = collection;
      this.maxWidth = maxWidth;
      this.maxHeight = maxHeight;
    }
  }

  public static class Progress {
    int parsed;
    int of;

    public Progress(int parsed, int of) {
      this.parsed = parsed;
      this.of = of;
    }
  }

  public static class Result {
    public final boolean success_video;
    public final File video;
    public final Date started;
    public final int images;
    public final long ms_per_frame;
    public final File audio;

    public long duration_ms() {
      return images * ms_per_frame;
    }

    private Result(boolean success, File video, File audio, Date started, int images, long ms_per_frame) {
      this.success_video = success;
      this.video = video;
      this.audio = audio;
      this.started = started;
      this.images = images;
      this.ms_per_frame = ms_per_frame;
    }
  }
}
