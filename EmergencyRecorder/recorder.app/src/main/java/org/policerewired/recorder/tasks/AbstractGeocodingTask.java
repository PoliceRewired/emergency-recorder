package org.policerewired.recorder.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Geocodes a given location, and returns the closest human readable address.
 */
public abstract class AbstractGeocodingTask extends AsyncTask<AbstractGeocodingTask.Params, AbstractGeocodingTask.Progress, AbstractGeocodingTask.Result> {
  private static final String TAG = AbstractGeocodingTask.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private Context context;

  protected AbstractGeocodingTask(Context context) {
    this.context = context;
  }

  @Override
  protected Result doInBackground(Params... params) {
    Params param = params[0];
    if (param.location == null) {
      return new Result(false, null);
    }

    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

    try {
      List<Address> addresses = geocoder.getFromLocation(param.location.getLatitude(), param.location.getLongitude(), 5);
      return new Result(true, addresses);

    } catch (Exception e) {
      Log.w(TAG, "Unable to geocode using location.", e);
      return new Result(true,null);
    }
  }

  public static class Params {
    public Location location;
    public Params(Location location) {
      this.location = location;
    }
  }

  public static class Progress { }

  public static class Result {
    public boolean attempted;
    public List<Address> addresses;

    public Result(boolean attempted, List<Address> addresses) {
      this.attempted = attempted;
      this.addresses = addresses;
    }
  }
}
