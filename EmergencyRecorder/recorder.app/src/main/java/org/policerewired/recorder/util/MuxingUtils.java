package org.policerewired.recorder.util;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MuxingUtils {
  private static final String TAG = MuxingUtils.class.getSimpleName();

  /**
   * Method to mux audio and video streams together into a single mp4.
   * See: https://stackoverflow.com/questions/31572067/android-how-to-mux-audio-file-and-video-file
   * @param input_video_file source video
   * @param input_audio_file source audio
   * @param output_mp4 desired output mp4 file location
   */
  public static void mux(File input_video_file, File input_audio_file, File output_mp4) {

    try {

      //noinspection ResultOfMethodCallIgnored
      output_mp4.createNewFile();

      MediaExtractor videoExtractor = new MediaExtractor();
      videoExtractor.setDataSource(input_video_file.getAbsolutePath());

      MediaExtractor audioExtractor = new MediaExtractor();
      audioExtractor.setDataSource(input_audio_file.getAbsolutePath());

      Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
      Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );

      MediaMuxer muxer = new MediaMuxer(output_mp4.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

      videoExtractor.selectTrack(0);
      MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
      int videoTrack = muxer.addTrack(videoFormat);

      audioExtractor.selectTrack(0);
      MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
      int audioTrack = muxer.addTrack(audioFormat);

      Log.d(TAG, "Video Format " + videoFormat.toString() );
      Log.d(TAG, "Audio Format " + audioFormat.toString() );

      boolean sawEOS = false;
      int frameCount = 0;
      int offset = 100;
      int sampleSize = 256 * 1024;
      ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
      ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
      MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
      MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

      videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
      audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

      muxer.start();

      while (!sawEOS) {
        videoBufferInfo.offset = offset;
        videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
          Log.d(TAG, "saw input EOS.");
          sawEOS = true;
          videoBufferInfo.size = 0;
        } else {
          videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
          videoBufferInfo.flags = videoExtractor.getSampleFlags();
          muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
          videoExtractor.advance();

          frameCount++;

          Log.v(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
          Log.v(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
        }
      }

      // Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();

      boolean sawEOS2 = false;
      int frameCount2 =0;
      while (!sawEOS2)
      {
        frameCount2++;

        audioBufferInfo.offset = offset;
        audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
        {
          Log.d(TAG, "saw input EOS.");
          sawEOS2 = true;
          audioBufferInfo.size = 0;
        }
        else
        {
          audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
          audioBufferInfo.flags = audioExtractor.getSampleFlags();
          muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
          audioExtractor.advance();


          Log.v(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
          Log.v(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

        }
      }

      muxer.stop();
      muxer.release();

      Log.d(TAG, "Generated muxed mp4: " + output_mp4.length() + " bytes.");



    } catch (IOException e) {
      Log.d(TAG, "Mixer Error 1 " + e.getMessage());
    } catch (Exception e) {
      Log.d(TAG, "Mixer Error 2 " + e.getMessage());
    }
  }

}
