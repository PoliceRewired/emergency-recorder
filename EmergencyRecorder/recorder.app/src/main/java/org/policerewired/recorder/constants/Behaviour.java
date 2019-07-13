package org.policerewired.recorder.constants;

import androidx.annotation.StringRes;

import org.policerewired.recorder.R;

/**
 * Representing an action to take when an outgoing call is detected and matched.
 */
public enum Behaviour {
  Nothing(R.string.behaviour_Nothing),
  OpenBubbleCam(R.string.behaviour_OpenBubbleCam),
  OpenBubbleCamStartBurstMode(R.string.behaviour_OpenBubbleCam_StartBurstMode),
  OpenBubbleCamStartVideoMode(R.string.behaviour_OpenBubbleCam_StartVideo);

  @StringRes
  public int descriptionId;

  Behaviour(@StringRes  int descriptionId) {
    this.descriptionId = descriptionId;
  }

}
