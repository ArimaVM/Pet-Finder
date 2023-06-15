package com.example.petfinder.components;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.R;
import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.databinding.ActivityPerimeterBinding;
import com.example.petfinder.pages.device.AddDevice;

public class Perimeter extends DrawerNav {

    ActivityPerimeterBinding activityPerimeterBinding;
    RecyclerView deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perimeter);

        activityPerimeterBinding = ActivityPerimeterBinding.inflate(getLayoutInflater());
        setContentView(activityPerimeterBinding.getRoot());

        allocateActivityTitle("Devices");

        deviceList = findViewById(R.id.deviceRecyclerView);

        activityPerimeterBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Perimeter.this, AddDevice.class);
                startActivity(intent);
            }
        });
    }
}