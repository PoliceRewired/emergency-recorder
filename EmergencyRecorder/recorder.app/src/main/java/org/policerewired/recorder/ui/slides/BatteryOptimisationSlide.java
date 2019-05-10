package org.policerewired.recorder.ui.slides;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.ISlidePolicy;

import org.policerewired.recorder.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BatteryOptimisationSlide extends Fragment implements ISlidePolicy {

  @BindView(R.id.icon_warn_power) ImageView icon_power;
  @BindView(R.id.btn_power) Button btn_power;

  private static final String ARG_LAYOUT_RES_ID = "layoutResId";
  private int layoutResId;

  public static BatteryOptimisationSlide newInstance() {
    BatteryOptimisationSlide slide = new BatteryOptimisationSlide();

    Bundle args = new Bundle();
    args.putInt(ARG_LAYOUT_RES_ID, R.layout.slide_battery_optimisation);
    slide.setArguments(args);

    return slide;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
      layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(layoutResId, container, false);
    ButterKnife.bind(this, view);
    refreshView();
    return view;
  }

  private void refreshView() {
    boolean should_request_power_change = !isFreeFromBatteryOptimisation();
    btn_power.setEnabled(should_request_power_change);
    icon_power.setImageResource(should_request_power_change ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_power.getDrawable().mutate().setTint(getContext().getColor(should_request_power_change ? R.color.colorWarning : R.color.colorAOK));
  }

  @OnClick(R.id.btn_power)
  public void power_click() {
    if (!isFreeFromBatteryOptimisation()) {
      requestIgnoreBatteryOptimisation();
    } else {
      refreshView();
    }
  }

  private boolean isFreeFromBatteryOptimisation() {
    PowerManager power = (PowerManager)getContext().getSystemService(Service.POWER_SERVICE);
    return power.isIgnoringBatteryOptimizations(getContext().getPackageName());
  }

  private void requestIgnoreBatteryOptimisation() {
    String data = "package:" + getContext().getPackageName();
    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
    intent.setData(Uri.parse(data));
    startActivityForResult(intent, 0);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    refreshView();
  }

  @Override
  public boolean isPolicyRespected() {
    return isFreeFromBatteryOptimisation();
  }

  @Override
  public void onUserIllegallyRequestedNextPage() {
    // TODO: warn user about permissions
  }
}