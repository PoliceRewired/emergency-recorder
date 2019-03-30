package org.policerewired.recorder.ui;

import android.os.Bundle;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.service.RecorderService;

import androidx.annotation.LayoutRes;
import butterknife.ButterKnife;

/**
 * An abstract class for Activities in this app - allowing them to bind to the RecorderSevice, and
 * request the app's permissions if called.
 */
public abstract class AbstractRecorderActivity extends AbstractServiceBoundAppCompatActivity<RecorderService, IRecorderService> {

  protected AbstractRecorderActivity() {
    super();
    inferredServiceClass = RecorderService.class;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutId());
    ButterKnife.bind(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateUI();
  }

  @LayoutRes
  protected abstract int getLayoutId();

  protected abstract void updateUI();

  @Override
  protected String[] getRequiredPermissions() {
    return EmergencyRecorderApp.permissions;
  }

  @Override protected void onPermissionsGranted() {
    updateUI();
  }

  @Override
  protected void onNotAllPermissionsGranted() {
    updateUI();
  }

  @Override protected void onGrantedOverlayPermission() { updateUI(); }
  @Override protected void onRefusedOverlayPermission() { updateUI(); }
  @Override protected void onUnecessaryCallToRequestOverlayPermission() { updateUI(); }

  @Override protected void onBoundChanged(boolean isBound) { updateUI(); }

}
