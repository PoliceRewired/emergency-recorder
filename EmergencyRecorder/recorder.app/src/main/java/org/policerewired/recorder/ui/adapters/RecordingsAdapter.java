package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.Recording;
import org.policerewired.recorder.util.NamingUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Adapter for a collection of Recordings, allowing the user view the log from a RecyclerView.
 */
public class RecordingsAdapter extends CrudAdapter<Recording, RecordingsAdapter.ViewHolder> {

  private NamingUtils naming;
  private Listener recording_listener;

  public RecordingsAdapter(Context context, Listener listener) {
    super(context, null);
    this.naming = new NamingUtils(context);
    this.recording_listener = listener;
  }

  @Override
  protected Class<Recording> getItemClass() {
    return Recording.class;
  }

  @Override
  protected SortedListAdapterCallback<Recording> createListCallback() {
    return new SortedListAdapterCallback<Recording>(this) {
      @Override
      public int compare(Recording o1, Recording o2) {
        return o1.started.compareTo(o2.started);
      }

      @Override
      public boolean areContentsTheSame(Recording oldItem, Recording newItem) {
        return
          oldItem.started.equals(newItem.started) &&
          oldItem.type.equals(newItem.type) &&
          oldItem.recordingId.equals(newItem.recordingId) &&
          (oldItem.data != null && oldItem.data.equals(newItem.data));
      }

      @Override
      public boolean areItemsTheSame(Recording item1, Recording item2) {
        return item1.recordingId.equals(item2.recordingId);
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

    @BindView(R.id.text_event) TextView event;
    @BindView(R.id.text_detail) TextView detail;
    @BindView(R.id.text_date) TextView date;
    @BindView(R.id.icon_share) ImageView icon_share;
    @BindView(R.id.icon_view) ImageView icon_view;

    // PopupMenu popup;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    @Override
    protected void populateView() {
      Recording rule = (Recording) item;
      event.setText(rule.type.name());
      detail.setText(rule.data);
      date.setText(naming.getConciseDate(rule.started));

      //popup = new PopupMenu(icon_menu.getContext(), icon_menu);
      //popup.getMenu().add(Menu.NONE, R.string.menu_edit_rule, 10, R.string.menu_edit_rule);
      //popup.getMenu().add(Menu.NONE, R.string.menu_delete_rule, 20, R.string.menu_delete_rule);

      /*
      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          switch (item.getItemId()) {
            case R.string.menu_edit_rule:
              onClickEdit();
              return true;
            case R.string.menu_delete_rule:
              onClickDelete();
              return true;
            default:
              return false;
          }
        }
      });
      */
    }

    @OnClick(R.id.icon_share)
    public void share_click() {
      // TODO recording_listener
    }

    @OnClick(R.id.icon_view)
    public void view_click() {
      // TODO recording_listener
    }
  }

  public interface Listener {
    void view(Recording recording);
    void share(Recording recording);
  }
}