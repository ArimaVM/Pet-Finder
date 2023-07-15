package com.example.petfinder.components;

import android.os.Bundle;

import com.example.petfinder.R;
import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.databinding.ActivityCreditsBinding;

public class Credits extends DrawerNav {

    ActivityCreditsBinding activityCreditsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        activityCreditsBinding = ActivityCreditsBinding.inflate(getLayoutInflater());
        setContentView(activityCreditsBinding.getRoot());

        allocateActivityTitle("About");
    }
}