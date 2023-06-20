package com.example.petfinder.components;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.R;
import com.example.petfinder.pages.user.UserInfo;
import com.example.petfinder.databinding.ActivitySettingsBinding;

public class Settings extends DrawerNav {

    ActivitySettingsBinding activitySettingsBinding;
    RelativeLayout userInfo, about, credits, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());

        allocateActivityTitle("Settings");


        about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, About.class);
                startActivity(intent);
            }
        });

        credits = findViewById(R.id.credits);
        credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Credits.class);
                startActivity(intent);
            }
        });
    }
}