package com.dreamwalker.myapplication103.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.dreamwalker.myapplication103.R;
import com.dreamwalker.myapplication103.fragments.FirstFragment;
import com.dreamwalker.myapplication103.fragments.SecondFragment;
import com.github.paolorotolo.appintro.AppIntro;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class IntroActivity extends AppIntro {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        checkFirstRun();

        setIndicatorColor(Color.parseColor("#000000"), Color.parseColor("#000000"));
        setBarColor(Color.parseColor("#ffffff"));
        setSeparatorColor(Color.parseColor("#000000"));
        setColorDoneText(Color.parseColor("#000000"));
        setNextArrowColor(Color.parseColor("#000000"));

        addSlide(FirstFragment.Companion.newInstance(R.layout.fragment_first));
        addSlide(SecondFragment.Companion.newInstance(R.layout.fragment_second));
//        addSlide(FirstFragment.Companion.newInstance(R.layout.fragment_first));

        showSkipButton(false);
        setVibrate(true);
        setVibrateIntensity(30);
    }

    private void checkFirstRun(){
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        if (!isFirstRun){
            Intent newIntent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        sharedPreferences.edit().putBoolean("isFirstRun", false).apply();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        sharedPreferences.edit().putBoolean("isFirstRun", false).apply();
        Intent newIntent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(newIntent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }


}
