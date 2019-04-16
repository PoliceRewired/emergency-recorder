package org.policerewired.recorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.policerewired.recorder.service.RecorderService;

public class StandaloneOutgoingCallReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {

      // retrieve either reformatted or raw phone number
      String number = getResultData();
      if (number == null) { number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER); }

      Intent call = new Intent(context, RecorderService.class);
      call.setAction(RecorderService.ACTION_CALL_RECEIVED);
      call.putExtra(RecorderService.EXTRA_NUMBER, number);
      context.startService(call);
    }
  }
}
