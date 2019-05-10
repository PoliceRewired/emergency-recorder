package org.policerewired.recorder.ui.slides;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.ISlidePolicy;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.EmergencyRecorderApp;
import org.policerewired.recorder.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PermissionsSlide extends Fragment implements ISlidePolicy {
  protected static final int REQUEST_ALL_PERMISSION_CODES = 7002;

  @BindView(R.id.btn_permissions) Button btn_permissions;
  @BindView(R.id.icon_warn_permissions) ImageView icon_permissions;

  private static final String ARG_LAYOUT_RES_ID = "layoutResId";
  private int layoutResId;

  public static PermissionsSlide newInstance() {
    PermissionsSlide slide = new PermissionsSlide();

    Bundle args = new Bundle();
    args.putInt(ARG_LAYOUT_RES_ID, R.layout.slide_permissions);
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
    boolean may_request_permissions = anyOutstandingPermissions();
    btn_permissions.setEnabled(may_request_permissions);
    icon_permissions.setImageResource(may_request_permissions ? R.drawable.ic_warning_black_24dp : R.drawable.ic_check_circle_black_24dp);
    icon_permissions.getDrawable().mutate().setTint(getContext().getColor(may_request_permissions ? R.color.colorWarning : R.color.colorAOK));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    refreshView();
  }

  @OnClick(R.id.btn_permissions)
  public void click_permissions() {
    if (anyOutstandingPermissions()) {
      requestAllPermissions();
    } else {
      refreshView();
    }
  }

  @Override
  public boolean isPolicyRespected() {
    return !anyOutstandingPermissions();
  }

  @Override
  public void onUserIllegallyRequestedNextPage() {
    // TODO: warn user about permissions
  }

  /**
   * @return true if any permission from getRequiredPermissions is not granted.
   */
  protected boolean anyOutstandingPermissions() {
    for (int i = 0; i < getRequiredPermissions().length; i++) {
      if (needsPermission(getRequiredPermissions()[i])) { return true; }
    }
    return false;
  }

  /**
   * Invokes the UI to request all permissions as provided by getRequiredPermissions.
   */
  protected void requestAllPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(getRequiredPermissions(), REQUEST_ALL_PERMISSION_CODES);
    } else {
      refreshView(); // already granted before Android M
    }
  }

  /**
   * Checks for the given permission.
   * @param permission a permission (Android permissions are available in Manifest.permissions)
   * @return true if this permission is still required
   */
  protected boolean needsPermission(String permission) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
    } else {
      return false; // permission automatically granted before Marshmallow
    }
  }

  private String[] getRequiredPermissions() {
    return EmergencyRecorderApp.get_permissions();
  }

}