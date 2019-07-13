package org.policerewired.recorder.ui;

import android.os.Bundle;

import androidx.annotation.LayoutRes;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.service.RecorderService;

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
    return EmergencyRecorderApp.get_permissions();
  }

  @Override protected void onPermissionsGranted() {
    updateUI();
    service.onPermissionsUpdated();
  }

  @Override
  protected void onNotAllPermissionsGranted() {
    updateUI();
    service.onPermissionsUpdated();
  }

  @Override protected void onGrantedOverlayPermission() {
    updateUI();
    service.onPermissionsUpdated();
  }

  @Override protected void onRefusedOverlayPermission() {
    updateUI();
    service.onPermissionsUpdated();
  }

  @Override protected void onUnecessaryCallToRequestOverlayPermission() {
    updateUI();
    service.onPermissionsUpdated();
  }

  @Override protected void onBoundChanged(boolean isBound) {
    updateUI();
    if (isBound) { service.onPermissionsUpdated(); }
  }

}
