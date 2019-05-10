package org.policerewired.recorder.ui.slides;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.github.paolorotolo.appintro.ISlidePolicy;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OverlaySlide extends Fragment implements ISlidePolicy {

  protected static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 7001;

  @BindView(R.id.icon_warn_overlay) ImageView icon_overlay;
  @BindView(R.id.btn_overlay) Button btn_overlay;

  private static final String ARG_LAYOUT_RES_ID = "layoutResId";
  private int layoutResId;

  public static OverlaySlide newInstance() {
    OverlaySlide slide = new OverlaySlide();

    Bundle args = new Bundle();
    args.putInt(ARG_LAYOUT_RES_ID, R.layout.slide_overlay);
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
  public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(layoutResId, container, false);
    ButterKnife.bind(this, view);
    refreshView();
    return view;
  }

  private void refreshView() {
    boolean may_request_overlay = !hasOverlayPermission();

    btn_overlay.setEnabled(may_request_overlay);
    icon_overlay.setImageResource(may_request_overlay ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_overlay.getDrawable().mutate().setTint(getContext().getColor(may_request_overlay ? R.color.colorWarning : R.color.colorAOK));
  }

  @OnClick(R.id.btn_overlay)
  public void overlay_click() {
    if (!hasOverlayPermission()) {
      requestOverlayPermission();
    } else {
      refreshView();
    }
  }

  @Override
  public boolean isPolicyRespected() {
    return hasOverlayPermission();
  }

  public boolean hasOverlayPermission() {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext());
  }

  public void requestOverlayPermission() {
    if (!hasOverlayPermission()) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
      startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    } else {
      refreshView();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    refreshView();
  }

  @Override
  public void onUserIllegallyRequestedNextPage() {
    // TODO: warn user about permissions
  }
}