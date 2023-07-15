package com.example.petfinder.pages.pet;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.container.PetModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class DisplayPetDetails extends AppCompatActivity
                                implements BluetoothGattCallbackHandler.DescriptorWriteCallback{

    private CircularImageView petProfile;
    private TextView petName, petBreed, petSex, date, petWeight, MACAddress, age_textview,
            petAllergiesDisplay, petTreatsDisplay, petMedDisplay, petVetDisplay, petContactDisplay;
    private BottomNavigationView bottomNav;
    private String recordID, image;
    private DatabaseHelper dbhelper;
    private PetModel petModel;
    private boolean isConnected = false;
    private BluetoothGatt bluetoothGatt;
    private PetFinder petFinder;
    private BluetoothGattCallbackHandler bluetoothGattCallbackHandler;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pet_details);

        petProfile = findViewById(R.id.petImage);
        petName = findViewById(R.id.petNameDisplay);
        petBreed = findViewById(R.id.petBreedDisplay);
        petSex = findViewById(R.id.petSexDisplay);
        date = findViewById(R.id.petBdateDisplay);
        petWeight = findViewById(R.id.petWeightDisplay);
        MACAddress = findViewById(R.id.deviceConnected);
        age_textview = findViewById(R.id.petAgeDisplay);
        petAllergiesDisplay = findViewById(R.id.petAllergiesDisplay);
        petTreatsDisplay = findViewById(R.id.petTreatsDisplay);
        petMedDisplay = findViewById(R.id.petMedDisplay);
        petVetDisplay = findViewById(R.id.petVetDisplay);
        petContactDisplay = findViewById(R.id.vetContactDisplay);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_location:
                    startActivity(new Intent(DisplayPetDetails.this, Location.class));
                    finish();
                    break;
                case R.id.nav_reports:
                    startActivity(new Intent(DisplayPetDetails.this, Reports.class));
                    finish();
                    break;
            }
            return true;
        });

        petFinder = PetFinder.getInstance();

        recordID = petFinder.getCurrentMacAddress();
        dbhelper = new DatabaseHelper(this);

        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
        setSupportActionBar(myToolbar);

        handler = new Handler();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(this, handler);
        bluetoothGattCallbackHandler.setDescriptorWriteCallback(this);

        if (bluetoothAdapter.isEnabled()) {
            @SuppressLint("MissingPermission") Runnable connectToBT = () -> {
                isConnected = true;
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(recordID);
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
                bluetoothGattCallbackHandler.setGatt(bluetoothGatt);
                //also triggers the onWait override below.
            };

            if (petFinder.getBluetoothObject().isNull()) {
                connectToBT.run();
            } else if (!Objects.equals(petFinder.getBluetoothObject().getBluetoothGatt().getDevice().getAddress(),
                    recordID)){
                connectToBT.run();
            }
        } else {
            isConnected = false;
            petFinder.deleteBluetoothObject();
        }

        // Set the custom back arrow as the navigation icon
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Set a click listener on the navigation icon
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        showRecordDetails();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        if (!petFinder.getBluetoothObject().isNull()) {
            petFinder.getBluetoothObject().getBluetoothGatt().disconnect();
            petFinder.deleteBluetoothObject();
            petFinder.removeCurrentMacAddress();
        }
        startActivity(new Intent(DisplayPetDetails.this, Dashboard.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editPet) {
            petFinder.setCurrentPetModel(petModel);
            startActivity(new Intent(DisplayPetDetails.this, EditPet.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("Range")
    private void showRecordDetails() {
        petModel = dbhelper.getRecordDetails(recordID);

        image = petModel.getImage();
        if (image.equals("null")) petProfile.setImageResource(R.drawable.profile);
        else petProfile.setImageURI(Uri.parse(image));

        MACAddress.setText(petModel.getMAC_ADDRESS());
        petName.setText(petModel.getName());
        petBreed.setText(petModel.getBreed());
        petSex.setText(petModel.getSex());
        age_textview.setText(String.format(petModel.getAge()>1?"%d Years Old":"%d Year Old", petModel.getAge()));
        date.setText(petModel.getBirthdate());
        petWeight.setText(String.format("%dkg", petModel.getWeight()));
        if (!petModel.getAllergies().isEmpty()) petAllergiesDisplay.setText(petModel.getAllergies());
        if (!petModel.getTreats().isEmpty()) petTreatsDisplay.setText(petModel.getTreats());
        if (!petModel.getMedications().isEmpty()) petMedDisplay.setText(petModel.getMedications());
        if (!petModel.getVetName().isEmpty()) petVetDisplay.setText(petModel.getVetName());
        if (!petModel.getVetContact().isEmpty()) petContactDisplay.setText(petModel.getVetContact());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onWait() {
        if (petFinder.bluetoothObject.isNull()) {
            petFinder.setBluetoothObject(bluetoothGatt,
                    bluetoothGattCallbackHandler.getCharacteristic(),
                    bluetoothGattCallbackHandler);
        }
    }
}