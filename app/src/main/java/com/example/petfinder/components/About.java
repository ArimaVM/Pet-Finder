package com.example.petfinder.components;

import android.os.Bundle;

import com.example.petfinder.R;
import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.databinding.ActivityAboutBinding;

public class About extends DrawerNav {

    ActivityAboutBinding activityAboutBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        activityAboutBinding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(activityAboutBinding.getRoot());

        allocateActivityTitle("About");

    }
}