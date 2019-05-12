package org.policerewired.recorder.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.distribute.Distribute;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.jcodec.common.StringUtils;
import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.db.entity.Rule;
import org.policerewired.recorder.ui.adapters.HomeHistoryAdapter;
import org.policerewired.recorder.ui.adapters.HomeRuleSummaryAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends AbstractRecorderActivity {

  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;
  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;
  @BindView(R.id.icon_warn_power) ImageView icon_power;

  @BindView(R.id.fab_record) FloatingActionButton fab_record;

  @BindView(R.id.card_R2L) CardView card_twitter;
  @BindView(R.id.recycler_twitter) RecyclerView recycler_twitter;

  @BindView(R.id.card_R2R) CardView card_history;
  @BindView(R.id.recycler_history) RecyclerView recycler_history;
  private HomeHistoryAdapter history_adapter;
  private LiveData<List<AuditRecord>> live_history;
  @BindView(R.id.btn_history) Button btn_history;
  @BindView(R.id.text_history_summary) TextView text_history_summary;

  @BindView(R.id.card_R1L) CardView card_howto;
  @BindView(R.id.web_howto) WebView web_howto;

  @BindView(R.id.card_R1R) CardView card_config;
  @BindView(R.id.recycler_rules) RecyclerView recycler_rules;
  private HomeRuleSummaryAdapter rule_summaries_adapter;
  private List<HomeRuleSummaryAdapter.RuleSummary> rule_summaries;
  @BindView(R.id.text_rules) TextView text_rules;
  @BindView(R.id.btn_rules) Button btn_rules;

  @Override
  protected int getLayoutId() {
    return R.layout.activity_home;
  }

  @Override
  protected void onResume() {
    super.onResume();
    Executors.newSingleThreadExecutor().execute(twitter_init);
    Executors.newSingleThreadExecutor().execute(howto_init);
  }

  private Runnable howto_init = () -> runOnUiThread(() -> {
    web_howto.getSettings().setJavaScriptEnabled(false); // no need for js
    web_howto.loadUrl("file:///android_asset/howto.html");
  });

  private Runnable twitter_init = new Runnable() {
    @Override
    public void run() {
      Twitter.initialize(HomeActivity.this);
      UserTimeline userTimeline = new UserTimeline.Builder().screenName(getString(R.string.twitter_screenname)).build();
      recycler_twitter.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
      final TweetTimelineRecyclerViewAdapter adapter =
        new TweetTimelineRecyclerViewAdapter.Builder(HomeActivity.this)
          .setTimeline(userTimeline)
          .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
          .build();
      runOnUiThread(() -> recycler_twitter.setAdapter(adapter));
    }
  };

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
    boolean any_outstanding = may_request_permissions || may_request_overlay || should_request_power_change;

    icon_permissions.setImageResource(may_request_permissions ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_overlay.setImageResource(may_request_overlay ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_power.setImageResource(should_request_power_change ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);

    icon_permissions.getDrawable().mutate().setTint(getColor(may_request_permissions ? R.color.colorWarning : R.color.colorAOK));
    icon_overlay.getDrawable().mutate().setTint(getColor(may_request_overlay ? R.color.colorWarning : R.color.colorAOK));
    icon_power.getDrawable().mutate().setTint(getColor(should_request_power_change ? R.color.colorWarning : R.color.colorAOK));

    fab_record.setBackgroundColor(getColor(may_record ? R.color.colorVideo : R.color.colorDisabled));
    fab_record.setEnabled(may_record);

    updateHistory();
    // updateRules();
    updateRuleSummaries();

    if (bound && any_outstanding) {
      Intent i = new Intent(this, SetupActivity.class);
      startActivity(i);
    }
  }

  private void updateRuleSummaries() {
    if (rule_summaries_adapter == null) {
      rule_summaries_adapter = new HomeRuleSummaryAdapter(this, rules_summary_listener);
      recycler_rules.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
      recycler_rules.setAdapter(rule_summaries_adapter);
    }

    if (bound) {
      text_rules.setText(R.string.text_rules);
      List<Rule> rules = service.getRules_static();
      rule_summaries = HomeRuleSummaryAdapter.generateSummaries(this, rules);
      rule_summaries_adapter.updateFrom(rule_summaries);
    } else {
      text_rules.setText(R.string.text_please_wait);
      rule_summaries_adapter.clear();
    }
  }

  private HomeRuleSummaryAdapter.Listener<HomeRuleSummaryAdapter.RuleSummary> rules_summary_listener = new HomeRuleSummaryAdapter.Listener<HomeRuleSummaryAdapter.RuleSummary>() {
    @Override public void onDeleteRequest(HomeRuleSummaryAdapter.RuleSummary rule) { /* nop */ }
    @Override public void onEditRequest(HomeRuleSummaryAdapter.RuleSummary rule) { /* nop */ }
    @Override public void onUpdateRequest(HomeRuleSummaryAdapter.RuleSummary rule) { /* nop */ }
  };

  private void updateHistory() {
    if (history_adapter == null) {
      history_adapter = new HomeHistoryAdapter(this, history_listener);
      recycler_history.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
      recycler_history.setAdapter(history_adapter);
    }

    if (bound) {
      if (live_history == null) {
        AuditRecordType[] chosen_media_types = new AuditRecordType[] {
          //AuditRecordType.BurstModePhoto,
          AuditRecordType.BurstModeVideo,
          AuditRecordType.Photo,
          AuditRecordType.VideoRecording,
          AuditRecordType.AudioRecording
        };

        live_history = service.getMediaLog_live_mostRecent(
          chosen_media_types,
          getResources().getInteger(R.integer.home_max_media_items));
      }

      live_history.observe(this, items -> {
        if (items.size() > 0) {
          text_history_summary.setText(getString(R.string.text_summary_recent_history_of_X, items.size()));
        } else {
          text_history_summary.setText(R.string.text_empty_history);
        }
        history_adapter.updateFrom(items);
      });

    } else {
      history_adapter.clear();
      text_history_summary.setText(R.string.text_please_wait);
      if (live_history != null) {
        live_history.removeObservers(this);
        live_history = null;
      }
    }
  }

  private HomeHistoryAdapter.Listener history_listener = new HomeHistoryAdapter.Listener() {
    @Override
    public void view(AuditRecord auditRecord) {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.parse(auditRecord.data), auditRecord.type.mime_type);
      intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(auditRecord.data));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(Intent.createChooser(intent, getString(R.string.chooser_title_view_media)));
    }

    @Override
    public void share(AuditRecord auditRecord) {
      // not implemented
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
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

      case R.string.menu_show_oss_licenses:
        show_oss_licenses();
        return true;

      case R.string.menu_view_config:
        show_config();
        return true;

      case R.string.menu_view_log:
        show_log();
        return true;

      case R.string.menu_view_rules:
        show_rules();
        return true;

      case R.string.menu_view_about:
        show_about();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @OnClick(R.id.btn_history)
  public void history_click() {
    show_log();
  }

  @OnClick(R.id.btn_rules)
  public void rules_click() {
    show_rules();
  }

  private void show_log() {
    startActivity(new Intent(this, LogActivity.class));
  }

  private void show_rules() {
    startActivity(new Intent(this, RulesActivity.class));
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

  @OnClick(R.id.fab_record)
  public void record_click() {
    service.showOverlay();
  }

}
