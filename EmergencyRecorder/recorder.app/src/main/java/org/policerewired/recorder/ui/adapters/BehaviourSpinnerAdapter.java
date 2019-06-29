package org.policerewired.recorder.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.policerewired.recorder.constants.Behaviour;

import java.util.List;

public class BehaviourSpinnerAdapter extends ArrayAdapter<Behaviour> {

  private Context context;

  public BehaviourSpinnerAdapter(@NonNull Context context, @NonNull List<Behaviour> behaviours) {
    super(context, android.R.layout.simple_spinner_item, behaviours);
    this.context = context;
    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Behaviour behaviour = getItem(position);

    View listItem = convertView;
    if(listItem == null) {
      listItem = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
    }

    TextView text = listItem.findViewById(android.R.id.text1);

    if (behaviour != null) {
      text.setText(behaviour.descriptionId);
    } else {
      text.setText("");
    }

    return listItem;
  }

  @Override
  public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Behaviour behaviour = getItem(position);

    View listItem = convertView;
    if(listItem == null) {
      listItem = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
    }

    TextView text = listItem.findViewById(android.R.id.text1);

    if (behaviour != null) {
      text.setText(behaviour.descriptionId);
    } else {
      text.setText("");
    }

    return listItem;
  }
}
