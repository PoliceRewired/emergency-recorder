package org.policerewired.recorder.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.policerewired.recorder.R;
import org.policerewired.recorder.service.IRecorderService;
import org.policerewired.recorder.util.NamingUtils;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectRangeDialog {

  private AppCompatActivity activity;
  private IRecorderService service;

  private NamingUtils naming;
  private AlertDialog dialog;

  private Listener listener;

  private Date earliest_item;
  private Date latest_item;

  private Date selected_from;
  private Date selected_to;

  @BindView(R.id.group_range) RadioGroup radio_group;

  @BindView(R.id.radio_24h) RadioButton radio_24h;
  @BindView(R.id.radio_48h) RadioButton radio_48h;
  @BindView(R.id.radio_7d) RadioButton radio_7d;
  @BindView(R.id.radio_31d) RadioButton radio_31d;
  @BindView(R.id.radio_all) RadioButton radio_allh;
  @BindView(R.id.radio_custom) RadioButton radio_custom;

  @BindView(R.id.input_from) TextInputLayout input_from;
  @BindView(R.id.input_to) TextInputLayout input_to;

  /**
   * Dialog showing the user a variety of ways to select a range of dates for export of the audit
   * log to a zip file.
   */
  public SelectRangeDialog(AppCompatActivity activity, IRecorderService service, Listener listener) {
    this.activity = activity;
    this.service = service;
    this.naming = new NamingUtils(activity);
    this.listener = listener;
    this.dialog = createDialog();
  }

  private AlertDialog createDialog() {
    return new AlertDialog.Builder(activity)
      .setView(R.layout.dialog_select_export_range)
      .setTitle(R.string.dialog_title_export_range)
      .setPositiveButton(R.string.btn_export, null)
      .setNegativeButton(R.string.btn_cancel, null)
      .create();
  }

  public void show(@NotNull Date earliest, @NotNull Date latest, Date from, Date to) {
    dialog.show();

    this.earliest_item = earliest;
    this.latest_item = latest;

    // messy - we have to assign the click listeners AFTER showing the dialog to prevent auto-close
    // on click of any button. This then allows us to do validation in the dialog.
    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      boolean ok = validate();
      if (ok) {
        dialog.dismiss();
        listener.done(selected_from, selected_to);
      }
    });

    dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
      dialog.dismiss();
      listener.cancelled();
    });

    ButterKnife.bind(this, dialog);

    if (from == null) { from = earliest_item; }
    if (to == null) { to = latest_item; }

    selected_from = from;
    selected_to = to;

    radio_group.clearCheck();
    radio_custom.setChecked(true);

    updateDialogFromDates();
  }

  private boolean validate() {
    boolean dates_in_order = selected_from.before(selected_to);
    if (!dates_in_order) {
      Toast.makeText(activity, R.string.toast_warn_from_not_before_to, Toast.LENGTH_SHORT).show();
    }
    return dates_in_order;
  }

  @SuppressWarnings("ConstantConditions")
  private void updateDialogFromDates() {
    input_from.getEditText().setText(naming.getShortDate(selected_from));
    input_to.getEditText().setText(naming.getShortDate(selected_to));
  }

  @OnClick({R.id.input_from, R.id.input_from_edit})
  public void input_from_click() {
    radio_group.clearCheck();
    radio_custom.setChecked(true);

    SwitchDateTimeDialogFragment pickerFragment = SwitchDateTimeDialogFragment.newInstance(
      activity.getString(R.string.picker_title_from_date),
      activity.getString(R.string.btn_ok),
      activity.getString(R.string.btn_cancel));

    pickerFragment.startAtCalendarView();
    pickerFragment.startAtCalendarView();
    pickerFragment.set24HoursMode(true);
    pickerFragment.setMaximumDateTime(selected_to);
    pickerFragment.setDefaultDateTime(selected_from);

    pickerFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
      @Override
      public void onPositiveButtonClick(Date date) {
        selected_from = date;
        updateDialogFromDates();
      }

      @Override
      public void onNegativeButtonClick(Date date) {
        // NOP
      }
    });

    pickerFragment.show(activity.getSupportFragmentManager(), "dialog_time");
  }

  @OnClick({R.id.input_to, R.id.input_to_edit})
  public void input_to_click() {
    radio_group.clearCheck();
    radio_custom.setChecked(true);

    SwitchDateTimeDialogFragment pickerFragment = SwitchDateTimeDialogFragment.newInstance(
      activity.getString(R.string.picker_title_to_date),
      activity.getString(R.string.btn_ok),
      activity.getString(R.string.btn_cancel));

    pickerFragment.startAtCalendarView();
    pickerFragment.startAtCalendarView();
    pickerFragment.set24HoursMode(true);
    pickerFragment.setMinimumDateTime(selected_from);
    pickerFragment.setDefaultDateTime(selected_to);

    pickerFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
      @Override
      public void onPositiveButtonClick(Date date) {
        selected_to = date;
        updateDialogFromDates();
      }

      @Override
      public void onNegativeButtonClick(Date date) {
        // NOP
      }
    });

    pickerFragment.show(activity.getSupportFragmentManager(), "dialog_time");
  }

  @OnClick({ R.id.radio_24h, R.id.radio_48h, R.id.radio_7d, R.id.radio_31d, R.id.radio_all, R.id.radio_custom })
  public void radio_selected(View selected) {

    Calendar now = Calendar.getInstance();
    selected_to = now.getTime();

    switch (selected.getId()) {
      case R.id.radio_24h:
        now.add(Calendar.HOUR, -24);
        selected_from = now.getTime();
        break;

      case R.id.radio_48h:
        now.add(Calendar.HOUR, -48);
        selected_from = now.getTime();
        break;

      case R.id.radio_7d:
        now.add(Calendar.WEEK_OF_MONTH, -1);
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        selected_from = now.getTime();
        break;

      case R.id.radio_31d:
        now.add(Calendar.MONTH, -1);
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        selected_from = now.getTime();
        break;

      case R.id.radio_all:
        selected_from = earliest_item;
        break;

      case R.id.radio_custom:
        break;

      default:
        throw new IllegalArgumentException("Unrecognised radio.");
    }

    updateDialogFromDates();
  }

  public interface Listener {
    void done(Date from, Date to);
    void cancelled();
  }


}
