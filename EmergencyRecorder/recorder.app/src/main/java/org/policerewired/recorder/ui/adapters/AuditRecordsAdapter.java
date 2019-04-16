package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.util.NamingUtils;
import org.policerewired.recorder.util.ThumbnailHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Adapter for a collection of Recordings, allowing the user view the log from a RecyclerView.
 */
public class AuditRecordsAdapter extends CrudAdapter<AuditRecord, AuditRecordsAdapter.ViewHolder> {

  private NamingUtils naming;
  private Listener audit_record_listener;

  public AuditRecordsAdapter(Context context, Listener listener) {
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
    return R.layout.entry_log;
  }

  public class ViewHolder extends CrudAdapter.ViewHolder {

    @BindView(R.id.icon_event) ImageView icon;
    @BindView(R.id.text_event) TextView event;
    @BindView(R.id.text_detail) TextView detail;
    @BindView(R.id.text_date) TextView date;
    @BindView(R.id.image_preview) ImageView preview;
    @BindView(R.id.icon_menu) ImageView icon_menu;

    PopupMenu popup;
    AuditRecord record;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    @Override
    protected void populateView() {
      record = (AuditRecord) item;
      icon.setImageResource(record.type.icon_id);
      icon.getDrawable().setTint(context.getColor(record.type.colour_id));

      event.setText(record.type.description_id);
      detail.setText(record.data);
      date.setText(naming.getShortDate(record.started));

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

      popup = new PopupMenu(icon_menu.getContext(), icon_menu);
      popup.getMenu().add(Menu.NONE, R.string.menu_view_media, 10, R.string.menu_view_media);
      popup.getMenu().add(Menu.NONE, R.string.menu_share_media, 20, R.string.menu_share_media);

      popup.setOnMenuItemClickListener(item -> {
        switch (item.getItemId()) {
          case R.string.menu_view_media:
            view_media();
            return true;
          case R.string.menu_share_media:
            share_media();
            return true;
          default:
            return false;
        }
      });
    }

    @OnClick(R.id.image_preview)
    public void preview_click() {
      view_media();
    }

    @OnClick(R.id.icon_menu)
    public void menu_click() {
      popup.show();
    }

    public void share_media() {
      audit_record_listener.share(record);
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