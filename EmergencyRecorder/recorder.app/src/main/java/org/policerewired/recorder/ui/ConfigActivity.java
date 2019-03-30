package org.policerewired.recorder.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Main activity - show's the app's permissions and allows the user to launch the bubble cam overlay
 * or the rules activity.
 */
public class ConfigActivity extends AbstractRecorderActivity {

  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;
  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;
  @BindView(R.id.btn_permissions) Button btn_permissions;
  @BindView(R.id.btn_overlay) Button btn_overlay;
  @BindView(R.id.fab_record) FloatingActionButton fab_record;

  @Override
  protected int getLayoutId() {
    return R.layout.activity_config;
  }

  @Override
  protected void updateUI() {
    setTitleBarToVersionWith(getString(R.string.app_name));

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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, R.string.menu_view_rules, 0, R.string.menu_view_rules);
    menu.add(Menu.NONE, R.string.menu_view_about, 0, R.string.menu_view_about);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.string.menu_view_rules:
        startActivity(new Intent(this, RulesActivity.class));
        return true;

        // TODO: implement about box

      default:
        return super.onOptionsItemSelected(item);
    }

  }
}
