package org.policerewired.recorder.ui;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.distribute.Distribute;

import org.jcodec.common.StringUtils;
import org.policerewired.recorder.R;

import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Main activity - show's the app's permissions and allows the user to launch the bubble cam overlay
 * or the rules activity.
 */
public class MainActivity extends AbstractRecorderActivity {

  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;
  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;
  @BindView(R.id.icon_warn_power) ImageView icon_power;
  @BindView(R.id.btn_permissions) Button btn_permissions;
  @BindView(R.id.btn_overlay) Button btn_overlay;
  @BindView(R.id.btn_power) Button btn_power;
  @BindView(R.id.fab_record) FloatingActionButton fab_record;

  @Override
  protected int getLayoutId() {
    return R.layout.activity_main;
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
    boolean may_request_power_change = !anyOutstandingPermissions() && should_request_power_change;

    btn_permissions.setEnabled(may_request_permissions);
    btn_overlay.setEnabled(may_request_overlay);
    btn_power.setEnabled(may_request_power_change);

    icon_permissions.setImageResource(may_request_permissions ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_overlay.setImageResource(may_request_overlay ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_power.setImageResource(should_request_power_change ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);

    icon_permissions.getDrawable().mutate().setTint(getColor(may_request_permissions ? R.color.colorWarning : R.color.colorAOK));
    icon_overlay.getDrawable().mutate().setTint(getColor(may_request_overlay ? R.color.colorWarning : R.color.colorAOK));
    icon_power.getDrawable().mutate().setTint(getColor(should_request_power_change ? R.color.colorWarning : R.color.colorAOK));

    fab_record.setBackgroundColor(getColor(may_record ? R.color.colorVideo : R.color.colorDisabled));
    fab_record.setEnabled(may_record);
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
    service.showOverlay();
  }

  @OnClick(R.id.btn_power)
  public void power_click() {
    String data = "package:" + getPackageName();
    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
    intent.setData(Uri.parse(data));
    startActivityForResult(intent, 0);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, R.string.menu_show_new_permissions_slides, 0, R.string.menu_show_new_permissions_slides);
    menu.add(Menu.NONE, R.string.menu_show_oss_licenses, 0, R.string.menu_show_oss_licenses);
    menu.add(Menu.NONE, R.string.menu_view_config, 0, R.string.menu_view_config);
    menu.add(Menu.NONE, R.string.menu_view_rules, 0, R.string.menu_view_rules);
    menu.add(Menu.NONE, R.string.menu_view_log, 0, R.string.menu_view_log);
    menu.add(Menu.NONE, R.string.menu_view_about, 0, R.string.menu_view_about);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.string.menu_show_new_permissions_slides:
        Intent i = new Intent(this, SetupActivity.class);
        startActivity(i);
        return true;

      case R.string.menu_show_oss_licenses:
        show_oss_licenses();
        return true;

      case R.string.menu_view_config:
        show_config();
        return true;

      case R.string.menu_view_log:
        startActivity(new Intent(this, LogActivity.class));
        return true;

      case R.string.menu_view_rules:
        startActivity(new Intent(this, RulesActivity.class));
        return true;

      case R.string.menu_view_about:
        show_about();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void show_oss_licenses() {
    OssLicensesMenuActivity.setActivityTitle(getString(R.string.oss_license_title));
    startActivity(new Intent(this, OssLicensesMenuActivity.class));
  }

  @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
  private void show_config() {
    List<String> lines = new LinkedList<>();

    lines.add(getString(R.string.config_embedded_listener, String.valueOf(getResources().getBoolean(R.bool.use_embedded_listener))));
    lines.add(getString(R.string.config_static_listener, String.valueOf(true)));
    lines.add(getString(R.string.config_hybrid_delay, getResources().getInteger(R.integer.hybrid_interval_ms)));
    lines.add(getString(R.string.config_location_delay, getResources().getInteger(R.integer.location_interval_ms)));
    lines.add(getString(R.string.config_appcenter_analytics_enabled, String.valueOf(Analytics.isEnabled().get())));
    lines.add(getString(R.string.config_appcenter_distribute_enabled, String.valueOf(Distribute.isEnabled().get())));
    lines.add(getString(R.string.config_photo_mode_supported, String.valueOf(getResources().getBoolean(R.bool.supports_photo_mode))));
    lines.add(getString(R.string.config_photo_max_width, getResources().getInteger(R.integer.default_picture_max_width)));
    lines.add(getString(R.string.config_hybrid_mode_supported, String.valueOf(getResources().getBoolean(R.bool.supports_hybrid_mode))));
    lines.add(getString(R.string.config_video_mode_supported, String.valueOf(getResources().getBoolean(R.bool.supports_video_mode))));
    lines.add(getString(R.string.config_hybrid_video_max_width, getResources().getInteger(R.integer.default_hybrid_video_max_width)));
    lines.add(getString(R.string.config_video_max_width, getResources().getInteger(R.integer.default_video_max_width)));
    lines.add(getString(R.string.config_video_bps, getResources().getInteger(R.integer.default_video_bps)));

    String[] lines_array = lines.toArray(new String[lines.size()]);
    String message = StringUtils.joinS(lines_array, "\n");

    new AlertDialog.Builder(this)
      .setTitle(R.string.dialog_title_config)
      .setMessage(message)
      .setPositiveButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
      .show();
  }

  private void show_about() {
    new AlertDialog.Builder(this)
      .setTitle(R.string.dialog_title_about)
      .setMessage(R.string.dialog_message_about)
      .setNeutralButton(R.string.btn_visit_police_rewired, (dialog, which) -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_police_rewired)));
        startActivity(browserIntent);
        dialog.dismiss();
      })
      .setPositiveButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
      .show();
  }

}
