package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import org.jcodec.common.StringUtils;
import org.policerewired.recorder.R;
import org.policerewired.recorder.constants.Behaviour;
import org.policerewired.recorder.db.entity.Rule;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

public class HomeRuleSummaryAdapter extends CrudAdapter<HomeRuleSummaryAdapter.RuleSummary, HomeRuleSummaryAdapter.ViewHolder> {

  public static class RuleSummary {
    public String behaviour;
    public String numbers;
  }

  public HomeRuleSummaryAdapter(Context context, Listener<RuleSummary> listener) {
    super(context, listener);
  }

  @Override
  protected Class<RuleSummary> getItemClass() {
    return RuleSummary.class;
  }

  @Override
  protected SortedListAdapterCallback<RuleSummary> createListCallback() {
    return new SortedListAdapterCallback<RuleSummary>(this) {
      @Override
      public int compare(RuleSummary o1, RuleSummary o2) {
        return o1.behaviour.compareTo(o2.behaviour);
      }

      @Override
      public boolean areContentsTheSame(RuleSummary oldItem, RuleSummary newItem) {
        return
            oldItem.behaviour.equals(newItem.behaviour) &&
            oldItem.numbers.equals(newItem.numbers);
      }

      @Override
      public boolean areItemsTheSame(RuleSummary item1, RuleSummary item2) {
        return item1.behaviour.equals(item2.behaviour);
      }
    };
  }

  @Override
  protected ViewHolder createViewHolder(View view) {
    return new ViewHolder(view);
  }

  @Override
  protected int getEntryLayout() {
    return R.layout.entry_rules_summary_home;
  }

  public class ViewHolder extends CrudAdapter.ViewHolder {

    @BindView(R.id.text_behaviour) TextView behaviour;
    @BindView(R.id.text_numbers) TextView numbers;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }

    @Override
    protected void populateView() {
      RuleSummary rule = (RuleSummary) item;
      numbers.setText(rule.numbers);
      behaviour.setText(rule.behaviour);
    }
  }

  public static List<RuleSummary> generateSummaries(Context context, List<Rule> rules) {
    List<RuleSummary> summaries = new LinkedList<>();

    for (Behaviour behaviour : Behaviour.values()) {
      List<String> numbers_for_behaviour = new LinkedList<>();
      for (Rule rule : rules) {
        if (rule.behaviour == behaviour) {
          numbers_for_behaviour.add(rule.match);
        }
      }

      if (numbers_for_behaviour.size() > 0) {
        RuleSummary summary = new RuleSummary();
        summary.behaviour = context.getString(behaviour.descriptionId);
        String[] numbers_array = numbers_for_behaviour.toArray(new String[0]);
        summary.numbers = StringUtils.joinS(numbers_array, ", ");
        summaries.add(summary);
      }
    } // behaviours

    return summaries;
  }
}