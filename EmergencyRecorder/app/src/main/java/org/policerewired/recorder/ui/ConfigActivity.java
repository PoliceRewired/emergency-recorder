package org.policerewired.recorder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.flt.servicelib.AbstractPermissionExtensionAppCompatActivity;
import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.service.RecorderService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfigActivity extends AbstractServiceBoundAppCompatActivity<RecorderService, IRecorderService> {

  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;
  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;

  @BindView(R.id.btn_permissions) Button btn_permissions;
  @BindView(R.id.btn_overlay) Button btn_overlay;

  @BindView(R.id.fab_record) FloatingActionButton fab_record;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_config);
    ButterKnife.bind(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateUI();
  }

  private void updateUI() {
    boolean may_request_permissions = anyOutstandingPermissions();
    boolean may_request_overlay = !hasOverlayPermission();
    boolean may_record = !anyOutstandingPermissions() && hasOverlayPermission();

    btn_permissions.setEnabled(may_request_permissions);
    btn_overlay.setEnabled(may_request_overlay);

    icon_permissions.setImageResource(may_request_permissions ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_overlay.setImageResource(may_request_overlay ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);

    icon_permissions.getDrawable().mutate().setTint(getColor(may_request_permissions ? R.color.colorWarning : R.color.colorAOK));
    icon_overlay.getDrawable().mutate().setTint(getColor(may_request_overlay ? R.color.colorWarning : R.color.colorAOK));

    fab_record.setBackgroundColor(getColor(may_record ? R.color.colorVideo : R.color.colorDisabled));
    fab_record.setEnabled(may_record);

    // TODO
  }

  @OnClick(R.id.btn_permissions)
  public void permissions_click() {
    requestAllPermissions();
  }

  @OnClick(R.id.btn_overlay)
  public void overlay_click() {
    requestOverlayPermission();
  }

  @OnClick(R.id.fab_record)
  public void record_click() {
    informUser("recording button tapped");
    //startActivity(new Intent(this, BubbleCamActivity.class));
    service.showOverlay();
  }

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
