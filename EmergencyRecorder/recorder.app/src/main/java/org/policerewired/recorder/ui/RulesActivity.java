package org.policerewired.recorder.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.BaseData;
import org.policerewired.recorder.db.entity.Rule;
import org.policerewired.recorder.ui.adapters.CrudAdapter;
import org.policerewired.recorder.ui.adapters.RulesAdapter;
import org.policerewired.recorder.ui.dialogs.EditRuleDialog;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Activity to display a modifiable list of the app's rules to the user.
 */
public class RulesActivity extends AbstractRecorderActivity {

  @BindView(R.id.recycler_rules) RecyclerView recycler_rules;
  @BindView(R.id.fab_add) FloatingActionButton fab_add;

  private LiveData<List<Rule>> live_rules;
  private RulesAdapter rules_adapter;

  private EditRuleDialog edit_dialog;

  @SuppressWarnings("ConstantConditions")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.activity_rules;
  }

  @Override
  protected void updateUI() {

    if (rules_adapter == null) {
      rules_adapter = new RulesAdapter(this, edit_rule_listener);
      recycler_rules.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
      recycler_rules.setAdapter(rules_adapter);
    }

    if (bound) {
      if (live_rules == null) {
        live_rules = service.getRules();
      }

      live_rules.observe(this, rules -> rules_adapter.updateFrom(rules));

    } else {
      rules_adapter.clear();

      if (live_rules != null) {
        live_rules.removeObservers(this);
        live_rules = null;
      }
    }
  }

  @OnClick(R.id.fab_add)
  public void add_click() {
    Rule rule = BaseData.createNewRule();
    edit_dialog = new EditRuleDialog(RulesActivity.this, rule, new EditRuleDialog.Listener() {
      @Override
      public void done(Rule rule) {
        service.insert(rule);
      }

      @Override
      public void cancelled() {
        // do nothing
      }
    });
    edit_dialog.show();
  }

  private RulesAdapter.Listener<Rule> edit_rule_listener = new CrudAdapter.Listener<Rule>() {
    @Override public void onDeleteRequest(Rule rule) {
      new AlertDialog.Builder(RulesActivity.this)
        .setTitle(R.string.alert_title_confirm_delete_rule)
        .setMessage(getString(R.string.alert_message_confirm_delete_rule_for, rule.match, rule.name))
        .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
          service.delete(rule);
          dialog.dismiss();
        })
        .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
        .show();
    }

    @Override public void onEditRequest(Rule rule) {
      edit_dialog = new EditRuleDialog(RulesActivity.this, rule, new EditRuleDialog.Listener() {
        @Override
        public void done(Rule rule) {
          service.update(rule);
        }

        @Override
        public void cancelled() {
          // do nothing
        }
      });
      edit_dialog.show();
    }

    @Override public void onUpdateRequest(Rule rule) { /* NOP */ }
  };

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
