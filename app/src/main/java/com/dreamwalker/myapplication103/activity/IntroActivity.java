package com.dreamwalker.myapplication103.activity;

import android.graphics.Color;
import android.os.Bundle;

import com.dreamwalker.myapplication103.R;
import com.dreamwalker.myapplication103.fragments.FirstFragment;
import com.github.paolorotolo.appintro.AppIntro;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        addSlide(FirstFragment.Companion.newInstance(R.layout.fragment_first));
        addSlide(FirstFragment.Companion.newInstance(R.layout.fragment_second));
        addSlide(FirstFragment.Companion.newInstance(R.layout.fragment_first));

        showSkipButton(false);
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }


}
