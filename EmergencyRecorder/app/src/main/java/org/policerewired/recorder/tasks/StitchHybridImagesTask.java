package org.policerewired.recorder.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.BitmapUtil;
import org.policerewired.recorder.DTO.HybridCollection;
import org.policerewired.recorder.util.CaptureAudioUtils;
import org.policerewired.recorder.util.CaptureVideoUtils;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.util.Date;

public class StitchHybridImagesTask extends AsyncTask<StitchHybridImagesTask.Params, StitchHybridImagesTask.Progress, StitchHybridImagesTask.Result> {
  private static final String TAG = StitchHybridImagesTask.class.getSimpleName();

  private Context context;
  private NamingUtils naming;
  private StorageUtils storage;

  public StitchHybridImagesTask(Context context) {
    this.context = context;
    this.naming = new NamingUtils(context);
    this.storage = new StorageUtils(context);
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

  }

  @Override
  protected void onPostExecute(Result result) {
    super.onPostExecute(result);

    if (result.success_video) {
      try {
        CaptureVideoUtils.insertVideo(
          context.getContentResolver(),
          result.video,
          naming.generate_video_title(result.started),
          naming.generate_video_description(result.started, result.images, result.duration_ms()),
          result.duration_ms());
      } catch (Exception e) {
        Log.e(TAG, "Unable to store video.", e);
      }
    }

    try {
      CaptureAudioUtils.insertAudio(
        context.getContentResolver(),
        result.audio,
        storage.externalAudioFile(context, result.started, ".3gpp"),
        naming.generate_audio_title(result.started),
        naming.generate_audio_description(result.started, result.duration_ms()),
        naming.generate_audio_album(result.started),
        result.duration_ms());
    } catch (Exception e) {
      Log.e(TAG, "Unable to store audio.", e);
    }
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
      BitmapFactory.decodeByteArray(param.collection.blobs.get(0), 0, param.collection.blobs.get(0).length, opts);
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

      for (byte[] blob : param.collection.blobs) {
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
