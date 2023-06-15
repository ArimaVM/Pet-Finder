package com.example.petfinder.pages.device;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.components.Perimeter;
import com.example.petfinder.pages.pet.AddPet;
import com.example.petfinder.pages.pet.EditPet;
import com.google.android.material.textfield.TextInputEditText;

public class AddDevice extends AppCompatActivity {

    TextInputEditText dName, latitude, longitude;
    DatabaseHelper databaseHelper;
    Button scanBT;
    private  String deviceName, lat, longi, btName, btAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        dName = findViewById(R.id.deviceName);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        scanBT = findViewById(R.id.scanButton);
        databaseHelper = new DatabaseHelper(this);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set the custom back arrow as the navigation icon
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Set a click listener on the navigation icon
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            storeData();
            startActivity(new Intent(AddDevice.this, Perimeter.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeData() {
        deviceName = ""+dName.getText().toString().trim();
        lat = ""+latitude.getText().toString().trim();
        longi = ""+longitude.getText().toString().trim();
        btName = "";
        btAddress = "";

        Intent intent = new Intent(AddDevice.this, EditDevice.class);
        intent.putExtra("isEditMode", false); // Set the edit mode to false, as it's a new device
        intent.putExtra("DEVICENAME", deviceName);
        intent.putExtra("LATITUDE", lat);
        intent.putExtra("LONGITUDE", longi);

        String timestamp = ""+System.currentTimeMillis();
        long id = databaseHelper.storeDeviceData(
                ""+deviceName,
                ""+lat,
                ""+longi,
                ""+btName,
                ""+btAddress);
        Toast.makeText(this, "Device Added against Id: "+id, Toast.LENGTH_SHORT).show();
    }
}