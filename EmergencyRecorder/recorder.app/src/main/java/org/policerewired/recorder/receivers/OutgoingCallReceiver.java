package org.policerewired.recorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutgoingCallReceiver extends BroadcastReceiver {

  private Listener listener;

  public OutgoingCallReceiver(Listener listener) {
    this.listener = listener;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {

      // retrieve either reformatted or raw phone number
      String phoneNumber = getResultData();
      if (phoneNumber == null) { phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER); }

      listener.onCall(phoneNumber);
    }
  }

  public interface Listener {
    void onCall(String number);
  }

}
