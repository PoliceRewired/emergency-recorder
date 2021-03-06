package org.policerewired.recorder.constants;

import android.content.Context;

import org.jcodec.common.StringUtils;
import org.policerewired.recorder.R;
import org.policerewired.recorder.db.entity.Rule;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods for parsing the default rules, and creating new rules.
 */
public class BaseData {

  public static Behaviour baseBehaviour = Behaviour.OpenBubbleCamStartBurstMode;

  /**
   * Extract rules from resource array
   * @return an array of rules to pre-populate the database
   */
  public static Rule[] getRules(Context context) {
    String[] items = context.getResources().getStringArray(R.array.call_matches);
    List<Rule> rules = new LinkedList<>();
    for (String item : items) {
      String[] parts = StringUtils.splitS(item, ",");
      rules.add(new Rule(parts[0], parts[1], Behaviour.valueOf(parts[2]), true));
    }
    return rules.toArray(new Rule[rules.size()]);
  }

  public static Rule createNewRule() {
    return new Rule(null, null, baseBehaviour, false);
  }

}
