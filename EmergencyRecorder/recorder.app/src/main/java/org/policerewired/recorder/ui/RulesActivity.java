package org.policerewired.recorder.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

import android.os.Bundle;
import android.view.MenuItem;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.Rule;
import org.policerewired.recorder.ui.adapters.CrudAdapter;
import org.policerewired.recorder.ui.adapters.RulesAdapter;

import java.util.List;

public class RulesActivity extends AbstractRecorderActivity {

  @BindView(R.id.recycler_rules) RecyclerView recycler_rules;

  private LiveData<List<Rule>> live_rules;
  private RulesAdapter rules_adapter;

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
      rules_adapter = new RulesAdapter(this, rules_listener);
      recycler_rules.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
      recycler_rules.setAdapter(rules_adapter);
    }

    if (bound) {
      if (live_rules == null) {
        live_rules = service.getRules();
      }

      live_rules.observe(this, new Observer<List<Rule>>() {
        @Override
        public void onChanged(List<Rule> rules) {
          rules_adapter.updateFrom(rules);
        }
      });

    } else {
      rules_adapter.clear();

      if (live_rules != null) {
        live_rules.removeObservers(this);
        live_rules = null;
      }
    }
  }

  private RulesAdapter.Listener<Rule> rules_listener = new CrudAdapter.Listener<Rule>() {
    @Override public void onDeleteRequest(Rule rule) { }
    @Override public void onEditRequest(Rule rule) { }
    @Override public void onUpdateRequest(Rule rule) { }
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
