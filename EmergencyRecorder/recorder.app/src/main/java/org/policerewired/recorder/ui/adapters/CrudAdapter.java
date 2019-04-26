package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.ButterKnife;

/**
 * Abstract adapter that can easily represent a collection of Items, with listener and callbacks
 * for basic CRUD manipulations (create/update/delete).
 */
@SuppressWarnings("unchecked")
public abstract class CrudAdapter<Item, VH extends CrudAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

  protected Listener<Item> listener;
  protected Context context;
  protected SortedList<Item> items;
  protected SortedListAdapterCallback<Item> list_callback;

  protected CrudAdapter(Context context, Listener<Item> listener) {
    super();
    this.context = context;
    this.listener = listener;
    this.list_callback = createListCallback();
    this.items = new SortedList<>(getItemClass(), list_callback);
  }

  protected abstract Class<Item> getItemClass();

  protected abstract SortedListAdapterCallback<Item> createListCallback();

  public void clear() {
    items.clear();
  }

  public void updateFrom(List<Item> list) {
    items.beginBatchedUpdates();
    items.clear();
    items.addAll(list);
    items.endBatchedUpdates();
  }

  public void add(Item item) {
    items.add(item);
  }

  public void remove(Item item) {
    items.remove(item);
  }

  public void update(Item item) {
    int index = items.indexOf(item);
    items.updateItemAt(index, item);
  }

  @NonNull
  @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(getEntryLayout(), null);
    return createViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull VH holder, int position) {
    Item item = items.get(position);
    holder.setItem(item);
  }

  protected abstract VH createViewHolder(View view);

  @LayoutRes
  protected abstract int getEntryLayout();

  @Override
  public int getItemCount() {
    return items.size();
  }

  public abstract class ViewHolder extends RecyclerView.ViewHolder {

    protected Item item;

    protected ViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void setItem(Item item) {
      this.item = item;
      populateView();
    }

    protected abstract void populateView();

    protected void onClickDelete() {
      listener.onDeleteRequest(item);
    }

    protected void onClickEdit() {
      listener.onEditRequest(item);
    }

    protected void onUpdate() {
      listener.onUpdateRequest(item);
    }
  }

  public interface Listener<Item> {
    void onDeleteRequest(Item item);
    void onEditRequest(Item item);
    void onUpdateRequest(Item item);
  }

}
