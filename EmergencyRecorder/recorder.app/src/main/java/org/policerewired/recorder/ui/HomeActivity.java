package org.policerewired.recorder.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.R;

import butterknife.BindView;

public class HomeActivity extends AbstractRecorderActivity {

  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;
  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;
  @BindView(R.id.icon_warn_power) ImageView icon_power;

  @BindView(R.id.fab_record) FloatingActionButton fab_record;

  @Override
  protected int getLayoutId() {
    return R.layout.activity_home;
  }

  private boolean isFreeFromBatteryOptimisation() {
    PowerManager power = (PowerManager)getSystemService(Service.POWER_SERVICE);
    return power.isIgnoringBatteryOptimizations(getPackageName());
  }

  @Override
  protected void updateUI() {
    setTitleBarToVersionWith(getString(R.string.app_name));

    boolean may_request_permissions = anyOutstandingPermissions();
    boolean may_request_overlay = !hasOverlayPermission();
    boolean may_record = !anyOutstandingPermissions() && hasOverlayPermission();
    boolean should_request_power_change = !isFreeFromBatteryOptimisation();

    icon_permissions.setImageResource(may_request_permissions ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_overlay.setImageResource(may_request_overlay ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_power.setImageResource(should_request_power_change ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);

    icon_permissions.getDrawable().mutate().setTint(getColor(may_request_permissions ? R.color.colorWarning : R.color.colorAOK));
    icon_overlay.getDrawable().mutate().setTint(getColor(may_request_overlay ? R.color.colorWarning : R.color.colorAOK));
    icon_power.getDrawable().mutate().setTint(getColor(should_request_power_change ? R.color.colorWarning : R.color.colorAOK));

    fab_record.setBackgroundColor(getColor(may_record ? R.color.colorVideo : R.color.colorDisabled));
    fab_record.setEnabled(may_record);
  }

}
