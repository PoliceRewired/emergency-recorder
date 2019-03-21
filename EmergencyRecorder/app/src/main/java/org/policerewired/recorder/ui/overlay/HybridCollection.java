package org.policerewired.recorder.ui.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.camerakit.CameraKitView;

import java.util.LinkedList;
import java.util.List;

public class HybridCollection {

  public List<byte[]> blobs;
  public List<Bitmap> bitmaps;

  public HybridCollection() {
    blobs = new LinkedList<>();
    bitmaps = new LinkedList<>();
  }

  public CameraKitView.FrameCallback frame_callback = new CameraKitView.FrameCallback() {
    @Override
    public void onFrame(CameraKitView cameraKitView, byte[] bytes) {
      Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      bitmaps.add(bmp);
    }
  };

  public CameraKitView.ImageCallback image_callback = new CameraKitView.ImageCallback() {
    @Override
    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
      blobs.add(bytes);
    }
  };

}
