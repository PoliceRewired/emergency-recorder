package org.policerewired.recorder.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.what3words.javawrapper.What3WordsV3;
import com.what3words.javawrapper.request.Coordinates;
import com.what3words.javawrapper.response.ConvertTo3WA;

import org.policerewired.recorder.R;

public class AbstractWhat3WordsTask extends AsyncTask<AbstractWhat3WordsTask.Params, AbstractWhat3WordsTask.Progress, AbstractWhat3WordsTask.Result> {
  private static final String TAG = AbstractWhat3WordsTask.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private Context context;

  protected AbstractWhat3WordsTask(Context context) {
    this.context = context;
  }

  @Override
  protected AbstractWhat3WordsTask.Result doInBackground(AbstractWhat3WordsTask.Params... params) {
    AbstractWhat3WordsTask.Params param = params[0];
    if (param.location == null) {
      return new AbstractWhat3WordsTask.Result(false, null);
    }

    try {
      What3WordsV3 api = new What3WordsV3(context.getString(R.string.api_key_w3w));

      ConvertTo3WA words = api.convertTo3wa(new Coordinates(param.location.getLatitude(), param.location.getLongitude()))
        .language("en")
        .execute();

      return new AbstractWhat3WordsTask.Result(true, words);

    } catch (Exception e) {
      Log.e(TAG, "Unable to geocode using location.", e);
      return new AbstractWhat3WordsTask.Result(true,null);
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
    public ConvertTo3WA words;

    public Result(boolean attempted, ConvertTo3WA words) {
      this.attempted = attempted;
      this.words = words;
    }
  }
}
