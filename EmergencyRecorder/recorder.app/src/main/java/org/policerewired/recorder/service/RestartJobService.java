package org.policerewired.recorder.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import org.policerewired.recorder.EmergencyRecorderApp;

public class RestartJobService extends JobService {
  private static final String TAG = RestartJobService.class.getSimpleName();

  private static final int MINUTELY = 60*1000;
  private static final int PERIOD = MINUTELY;

  private static final int RESTART_RECORDER_APP_JOB_ID = 1;

  public static void initialise(Context context) {
    ComponentName cn = new ComponentName(context, RestartJobService.class);
    long period = PERIOD;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      period = JobInfo.getMinPeriodMillis();
    }

    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    jobScheduler.schedule(new JobInfo.Builder(RESTART_RECORDER_APP_JOB_ID, cn)
      .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
      .setPersisted(true)
      .setPeriodic(period)
      .build());
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    switch (params.getJobId()) {
      case RESTART_RECORDER_APP_JOB_ID:
        Log.d(TAG, "Restart recorder app job started.");

        EmergencyRecorderApp.startRecorderService(this);
        jobFinished(params, true);
        break;

      default:
        Log.w(TAG, "Unrecognised job id: " + params.getJobId());
    }

    return false; // false indicates completion, true for long-running in other threads.
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.d(TAG, "Restart service job halted.");
    return true; // true indicates a request to try again.
  }
}
