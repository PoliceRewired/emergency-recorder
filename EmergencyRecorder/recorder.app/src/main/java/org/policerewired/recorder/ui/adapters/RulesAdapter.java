package org.policerewired.recorder.ui.adapters;


import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.Rule;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import butterknife.BindView;
import butterknife.OnClick;

public class RulesAdapter extends CrudAdapter<Rule, RulesAdapter.ViewHolder> {

  public RulesAdapter(Context context, Listener<Rule> listener) {
    super(context, listener);
  }

  @Override
  protected Class<Rule> getItemClass() {
    return Rule.class;
  }

  @Override
  protected SortedListAdapterCallback<Rule> createListCallback() {
    return new SortedListAdapterCallback<Rule>(this) {
      @Override
      public int compare(Rule o1, Rule o2) {
        return o1.match.compareTo(o2.match);
      }

      @Override
      public boolean areContentsTheSame(Rule oldItem, Rule newItem) {
        return
          oldItem.match.equals(newItem.match) &&
            oldItem.behaviour == newItem.behaviour &&
            oldItem.name.equals(newItem.name) &&
            oldItem.ruleId.equals(newItem.ruleId);
      }

      @Override
      public boolean areItemsTheSame(Rule item1, Rule item2) {
        return item1.ruleId.equals(item2.ruleId);
      }
    };
  }

  @Override
  protected ViewHolder createViewHolder(View view) {
    return new ViewHolder(view);
  }

  @Override
  protected int getEntryLayout() {
    return R.layout.entry_rule;
  }

  public class ViewHolder extends CrudAdapter.ViewHolder {

    @BindView(R.id.text_number) TextView number;
    @BindView(R.id.text_name) TextView name;
    @BindView(R.id.text_behaviour) TextView behaviour;
    @BindView(R.id.icon_menu) ImageView icon_menu;

    PopupMenu popup;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    @Override
    protected void populateView() {
      Rule rule = (Rule) item;
      number.setText(rule.match);
      name.setText(rule.name);
      behaviour.setText(rule.behaviour.descriptionId);

      popup = new PopupMenu(icon_menu.getContext(), icon_menu);
      popup.getMenu().add(Menu.NONE, R.string.menu_edit_rule, 10, R.string.menu_edit_rule);
      popup.getMenu().add(Menu.NONE, R.string.menu_delete_rule, 20, R.string.menu_delete_rule);

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
    }

    @OnClick(R.id.icon_menu)
    public void menu_click() { popup.show(); }

  }
}