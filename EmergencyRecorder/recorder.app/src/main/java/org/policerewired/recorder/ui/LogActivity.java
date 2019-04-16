package org.policerewired.recorder.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jcodec.common.StringUtils;
import org.jcodec.common.io.IOUtils;
import org.policerewired.recorder.BuildConfig;
import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.ui.adapters.AuditRecordsAdapter;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.SharingUtils;
import org.policerewired.recorder.util.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

import static androidx.core.content.FileProvider.getUriForFile;

/**
 * Activity to display the audit log - allows a user to view and share media, or the log itself.
 */
public class LogActivity extends AbstractRecorderActivity {
  private static final String TAG = LogActivity.class.getSimpleName();

  @BindView(R.id.recycler_log) RecyclerView recycler_log;
  @BindView(R.id.fab_share) FloatingActionButton fab_share;

  private LiveData<List<AuditRecord>> live_recordings;
  private AuditRecordsAdapter recordings_adapter;

  private NamingUtils naming;
  private StorageUtils storage;
  private SharingUtils sharing;

  @SuppressWarnings("ConstantConditions")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    naming = new NamingUtils(this);
    storage = new StorageUtils(this);
    sharing = new SharingUtils(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.activity_log;
  }

  @Override
  protected void updateUI() {
    if (recordings_adapter == null) {
      recordings_adapter = new AuditRecordsAdapter(this, recordings_listener);
      recycler_log.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
      recycler_log.setAdapter(recordings_adapter);
    }

    if (bound) {
      if (live_recordings == null) {
        live_recordings = service.getAuditLog_live();
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
  public boolean onCreateOptionsMenu(Menu menu) {
    // group // item // order // title
    if (BuildConfig.DEBUG) {
      menu.add(Menu.NONE, R.string.menu_delete_entire_log, 0, R.string.menu_delete_entire_log);
    }
    menu.add(Menu.NONE, R.string.menu_export_entire_log, 0, R.string.menu_export_entire_log);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.string.menu_delete_entire_log:
        confirmDeleteLog();
        return true;

      case R.string.menu_export_entire_log:
        shareLog();
        return true;

      case android.R.id.home:
        finish();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Yes or no confirmation dialog to delete the entire audit log.
   * NB. This dialog should only be available in debug mode.
   */
  private void confirmDeleteLog() {
    new AlertDialog.Builder(this)
      .setTitle(R.string.confirm_delete_log_title)
      .setMessage(R.string.confirm_delete_log_message)
      .setPositiveButton(R.string.btn_delete_log, (dialog, which) -> {
        service.deleteEntireAuditLog();
        dialog.dismiss();
      })
      .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
      .show();
  }

  @OnClick(R.id.fab_share)
  public void share_click() {
    shareLog();
  }

  /**
   * Initiates sharing of the audit log file as a CSV email attachment.
   */
  private void shareLog() {
    try {
      File file = createAuditLogFile();

      Intent intent = new Intent(Intent.ACTION_SENDTO);
      intent.setData(Uri.parse("mailto:"));
      intent.putExtra(android.content.Intent.EXTRA_TITLE, getString(R.string.share_title_audit_log));
      intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title_audit_log));
      intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_text_audit_log));
      intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
      startActivity(Intent.createChooser(intent, getResources().getString(R.string.chooser_title_share_audit_log)));
    } catch (Exception e) {
      Log.e(TAG, "Exception during preparation of audit file.", e);
      informUser(R.string.toast_warning_exception_during_audit_file_share);
    }
  }

  /**
   * Listens to the recycler/adapter and detects requests from the user to share or view media
   */
  private AuditRecordsAdapter.Listener recordings_listener = new AuditRecordsAdapter.Listener() {

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
      try {
        File media_file = copyMediaFile(auditRecord);
        Uri contentUri = getUriForFile(LogActivity.this, sharing.getFileProviderAuthority(), media_file);

        String media_type = getString(auditRecord.type.description_id);
        String date = naming.getLongFormatDate(auditRecord.started);
        String subject = getString(R.string.share_subject_media_TYPE, media_type);
        String description = getString(R.string.share_description_media_TYPE_AT, media_type, date);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        // intent.setDataAndType(contentUri, auditRecord.type.mime_type);
        intent.setType(auditRecord.type.mime_type);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TITLE, subject);
        intent.putExtra(Intent.EXTRA_TEXT, description);

        // attachment
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, getString(R.string.chooser_title_share_media)));

      } catch (Exception e) {
        Log.e(TAG, "Exception during preparation of media file.", e);
        informUser(R.string.toast_warning_exception_during_media_file_share);
      }
    }
  };

  /**
   * Copies a file from the device media store to the temporary cache so it can be renamed and shared.
   * @param record an Audit record containing type and content Uri information about the media
   * @return a File pointing to the copied and cached media
   * @throws Exception if unable to create the copy for any reason
   */
  private File copyMediaFile(AuditRecord record) throws Exception {
    File outFile;
    switch (record.type) {
      case AudioRecording:
        outFile = sharing.generate_audio_file(record.started);
        break;
      case BurstModePhoto:
        outFile = sharing.generate_hybrid_photo_file(record.started);
        break;
      case BurstModeVideo:
        outFile = sharing.generate_hybrid_video_file(record.started);
        break;
      case Photo:
        outFile = sharing.generate_photo_file(record.started);
        break;
      case VideoRecording:
        outFile = sharing.generate_video_file(record.started);
        break;
      default:
        throw new IllegalArgumentException("Unsupported media type: " + record.type.name());
    }

    FileOutputStream outStream = new FileOutputStream(outFile);
    InputStream inStream = getContentResolver().openInputStream(Uri.parse(record.data));
    IOUtils.copy(inStream, outStream);
    return outFile;
  }

  /**
   * Generates a CSV edition of the audit log, stores it in the temporary cache
   * @return a File pointing to the stored audit log
   * @throws IOException if there's an issue creating the file
   */
  private File createAuditLogFile() throws IOException {
    File file = storage.tempAuditFile(".csv");
    List<AuditRecord> records = service.getAuditLog_static();

    List<String> entries = new LinkedList<>();
    String csv_entry = "\"%s\"";

    for (AuditRecord record : records) {
      String time = String.format(csv_entry, String.valueOf(record.started.getTime()));
      String date = String.format(csv_entry, naming.getShortDate(record.started));
      String type = String.format(csv_entry, getString(record.type.description_id));
      String data = String.format(csv_entry, record.data);
      String row = StringUtils.join2(new String[] { time,date,type,data } , ',');
      entries.add(row);
    }

    String[] entries_array = entries.toArray(new String[entries.size()]);
    String log_final = StringUtils.join2(entries_array, '\n');
    IOUtils.writeStringToFile(file, log_final);

    return file;
  }
}
