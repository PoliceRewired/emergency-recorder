package org.policerewired.recorder.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import org.policerewired.recorder.R;
import org.policerewired.recorder.ui.slides.BatteryOptimisationSlide;
import org.policerewired.recorder.ui.slides.OverlaySlide;
import org.policerewired.recorder.ui.slides.PermissionsSlide;

public class SetupActivity extends AppIntro2 {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 1. welcome screen
    SliderPage welcome = new SliderPage();
    welcome.setTitle(getString(R.string.intro_welcome_title));
    welcome.setDescription(getString(R.string.intro_welcome_description));
    welcome.setImageDrawable(R.mipmap.ic_launcher);
    welcome.setBgColor(getColor(R.color.colorPrimaryDark));

    SliderPage finished = new SliderPage();
    finished.setTitle(getString(R.string.intro_finished_title));
    finished.setDescription(getString(R.string.intro_finished_description));
    finished.setImageDrawable(R.mipmap.ic_launcher);
    finished.setBgColor(getColor(R.color.colorPrimaryDark));

    // TODO: configure slider pages
    // 2. permissions screen
    // 3. overlay permission
    // 4. battery optimisation exception
    // 5. congratulations + tips

    addSlide(AppIntroFragment.newInstance(welcome));
    addSlide(PermissionsSlide.newInstance());
    addSlide(OverlaySlide.newInstance());
    addSlide(BatteryOptimisationSlide.newInstance());
    addSlide(AppIntroFragment.newInstance(finished));

    setFlowAnimation();
    setWizardMode(true);
  }

  @Override
  public void onDonePressed(Fragment currentFragment) {
    super.onDonePressed(currentFragment);
    finish();
  }
}
