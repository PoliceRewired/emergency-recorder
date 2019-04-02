package org.policerewired.recorder.ui;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.Recording;
import org.policerewired.recorder.ui.adapters.CrudAdapter;
import org.policerewired.recorder.ui.adapters.RecordingsAdapter;

import java.util.List;

public class LogActivity extends AbstractRecorderActivity {

  @BindView(R.id.recycler_log) RecyclerView recycler_log;
  @BindView(R.id.fab_share) FloatingActionButton fab_share;

  private LiveData<List<Recording>> live_recordings;
  private RecordingsAdapter recordings_adapter;

  @SuppressWarnings("ConstantConditions")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.activity_log;
  }

  @Override
  protected void updateUI() {
    if (recordings_adapter == null) {
      recordings_adapter = new RecordingsAdapter(this, recordings_listener);
      recycler_log.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, true));
      recycler_log.setAdapter(recordings_adapter);
    }

    if (bound) {
      if (live_recordings == null) {
        live_recordings = service.getRecordingLog();
      }

      live_recordings.observe(this, rules -> recordings_adapter.updateFrom(rules));

    } else {
      recordings_adapter.clear();

      if (live_recordings != null) {
        live_recordings.removeObservers(this);
        live_recordings = null;
      }
    }
  }

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


  private RecordingsAdapter.Listener recordings_listener = new RecordingsAdapter.Listener() {
    @Override
    public void view(Recording recording) {
      // TODO
    }

    @Override
    public void share(Recording recording) {
      // TODO
    }
  };
}
