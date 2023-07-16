package com.example.petfinder.pages.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.petfinder.R;
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.pages.pet.Reports;

public class OnBoarding extends AppCompatActivity {

    public static final String COMPLETED_ONBOARDING_PREF_NAME = "Onboarding Completed";

    private ViewPager viewPager;
    private LinearLayout indicatorLayout;
    private int[] slideLayouts = {R.layout.activity_on_boarding_1, R.layout.activity_on_boarding_2,
            R.layout.activity_on_boarding_3, R.layout.activity_on_boarding_4, R.layout.activity_on_boarding_5,
            R.layout.activity_on_boarding_6, R.layout.activity_on_boarding_7};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        //VIEWPAGER
        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        SliderAdapter sliderAdapter = new SliderAdapter();
        viewPager.setAdapter(sliderAdapter);

        addIndicators();
        updateIndicators(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void addIndicators() {
        for (int i = 0; i < slideLayouts.length; i++) {
            ImageView indicator = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            indicator.setImageResource(R.drawable.indicator_inactive); // Set inactive indicator drawable
            indicatorLayout.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView indicator = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                indicator.setImageResource(R.drawable.indicator_active); // Set active indicator drawable
            } else {
                indicator.setImageResource(R.drawable.indicator_inactive); // Set inactive indicator drawable
            }
        }
    }

    public void finishTutorial(){
        SharedPreferences.Editor sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        sharedPreferencesEditor.putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true);
        sharedPreferencesEditor.apply();
        startActivity(new Intent(OnBoarding.this, Dashboard.class));
        finish();
    }

    private class SliderAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return slideLayouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(OnBoarding.this);
            ViewGroup layout = (ViewGroup) inflater.inflate(slideLayouts[position], container, false);

            switch (slideLayouts[position]) {
                case R.layout.activity_on_boarding_7:
                    layout.findViewById(R.id.continueToApp).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finishTutorial();
                        }
                    });
            }

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}