package org.policerewired.recorder.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputLayout;

import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.Behaviour;
import org.policerewired.recorder.db.entity.Rule;

import java.util.Arrays;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Manages a dialog that can be used to update the contents of a Rule.
 */
public class EditRuleDialog {

  private Context context;
  private AlertDialog dialog;

  private Rule rule;
  private Listener listener;

  @BindView(R.id.input_number) TextInputLayout input_number;
  @BindView(R.id.input_name) TextInputLayout input_name;
  @BindView(R.id.spinner_behaviour) AppCompatSpinner spinner_behaviour;

  public EditRuleDialog(Context context, Rule rule, Listener listener) {
    this.context = context;
    this.rule = rule;
    this.listener = listener;
    this.dialog = createDialog(context);
  }

  public void show() {
    dialog.show();

    // messy - we have to assign the click listeners AFTER showing the dialog to prevent auto-close
    // on click of any button. This then allows us to do validation in the dialog.
    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
      boolean ok = validate();
      if (ok) {
        updateRuleFromDialog();
        dialog.dismiss();
        listener.done(rule);
      }
    });

    dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
      dialog.dismiss();
      listener.cancelled();
    });

    ButterKnife.bind(this, dialog);
    createSpinnerAdapter();
    updateDialogFromRule();
  }

  private void createSpinnerAdapter() {
    ArrayAdapter<Behaviour> spinner_adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, Behaviour.values());
    spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner_behaviour.setAdapter(spinner_adapter);
  }

  private AlertDialog createDialog(Context context) {
    return new AlertDialog.Builder(context)
      .setView(R.layout.dialog_edit_rule)
      .setTitle(R.string.dialog_title_edit_rule)
      .setPositiveButton(R.string.btn_save, null)
      .setNegativeButton(R.string.btn_cancel, null)
      .create();
  }

  private boolean validate() {
    boolean ok = true;
    if (Strings.isEmptyOrWhitespace(input_number.getEditText().getText().toString())) {
      input_number.setError(context.getString(R.string.validation_rule_number_empty));
      ok = false;
    } else {
      input_number.setError(null);
    }
    if (Strings.isEmptyOrWhitespace(input_name.getEditText().getText().toString())) {
      input_name.setError(context.getString(R.string.validation_rule_name_empty));
      ok = false;
    } else {
      input_name.setError(null);
    }
    return ok;
  }

  private void updateDialogFromRule() {
    input_name.getEditText().setText(rule.name);
    input_number.getEditText().setText(rule.match);
    spinner_behaviour.setSelection(Arrays.asList(Behaviour.values()).indexOf(rule.behaviour));
    input_name.setEnabled(!rule.locked);   // only editable if the rule isn't locked
    input_number.setEnabled(!rule.locked); // only editable if the rule isn't locked
  }

  private void updateRuleFromDialog() {
    rule.name = input_name.getEditText().getText().toString();
    rule.match = input_number.getEditText().getText().toString();
    rule.behaviour = (Behaviour) spinner_behaviour.getSelectedItem();
  }

  public interface Listener {
    void done(Rule rule);
    void cancelled();
  }

}
