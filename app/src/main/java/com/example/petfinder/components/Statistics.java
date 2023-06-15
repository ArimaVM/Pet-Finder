package com.example.petfinder.application.components;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petfinder.R;
import com.example.petfinder.application.pages.pet.DisplayPetDetails;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Statistics extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_petProfile:
                        startActivity(new Intent(Statistics.this, DisplayPetDetails.class));
                        break;
                    case R.id.nav_location:
                        break;
                    case R.id.nav_statistics:
                        startActivity(new Intent(Statistics.this, Statistics.class));
                        break;
                }
                return true;
            }
    });
}}
