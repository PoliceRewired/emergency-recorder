package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.ThumbnailHelper;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Adapter for a collection of Recordings, allowing the user view simple media from a RecyclerView.
 */
public class HomeHistoryAdapter extends CrudAdapter<AuditRecord, HomeHistoryAdapter.ViewHolder> {

  private NamingUtils naming;
  private Listener audit_record_listener;

  public HomeHistoryAdapter(Context context, Listener listener) {
    super(context, null);
    this.naming = new NamingUtils(context);
    this.audit_record_listener = listener;
  }

  @Override
  protected Class<AuditRecord> getItemClass() {
    return AuditRecord.class;
  }

  @Override
  protected SortedListAdapterCallback<AuditRecord> createListCallback() {
    return new SortedListAdapterCallback<AuditRecord>(this) {
      @Override
      public int compare(AuditRecord o1, AuditRecord o2) {
        return -o1.started.compareTo(o2.started); // most recent first
      }

      @Override
      public boolean areContentsTheSame(AuditRecord oldItem, AuditRecord newItem) {
        return
          oldItem != null && newItem != null &&
            (oldItem.started.equals(newItem.started) &&
              oldItem.type.equals(newItem.type) &&
              oldItem.recordingId.equals(newItem.recordingId) &&
              (oldItem.data != null && oldItem.data.equals(newItem.data)));
      }

      @Override
      public boolean areItemsTheSame(AuditRecord item1, AuditRecord item2) {
        return item1 != null && item2 != null && item1.recordingId.equals(item2.recordingId);
      }
    };
  }

  @Override
  protected ViewHolder createViewHolder(View view) {
    return new ViewHolder(view);
  }

  @Override
  protected int getEntryLayout() {
    return R.layout.entry_log_home;
  }

  public class ViewHolder extends CrudAdapter.ViewHolder {

    @BindView(R.id.icon_event) ImageView icon;
    @BindView(R.id.image_preview) ImageView preview;

    AuditRecord record;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    @Override
    protected void populateView() {
      record = (AuditRecord) item;
      icon.setImageResource(record.type.icon_id);
      icon.getDrawable().setTint(context.getColor(record.type.colour_id));

      preview.setVisibility(record.type.is_media ? INVISIBLE : GONE);
      preview.post(() -> {
        switch (record.type) {
          case BurstModePhoto:
          case Photo:
            preview.setImageBitmap(ThumbnailHelper.getImageThumbnail(context, Uri.parse(record.data)));
            break;
          case BurstModeVideo:
          case VideoRecording:
            preview.setImageBitmap(ThumbnailHelper.getVideoThumbnail(context, Uri.parse(record.data)));
            break;
          case AudioRecording:
            preview.setImageResource(R.drawable.ic_record_voice_over_black_24dp);
            break;
          default:
            preview.setImageResource(R.drawable.ic_error_outline_black_24dp);
            break;
        }
        preview.setVisibility(record.type.is_media ? VISIBLE : GONE);
      });
    }

    @OnClick(R.id.image_preview)
    public void preview_click() {
      view_media();
    }

    public void view_media() {
      audit_record_listener.view(record);
    }
  }

  public interface Listener {
    void view(AuditRecord auditRecord);
    void share(AuditRecord auditRecord);
  }
}