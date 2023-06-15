package com.example.petfinder.pages.pet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.components.Location;
import com.example.petfinder.R;
import com.example.petfinder.components.Statistics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DisplayPetDetails extends AppCompatActivity implements PetFinder.DataObserver {

    private CircularImageView petProfile;
    private TextView petName, petBreed, petSex, date, petWeight, MACAddress;
    private BottomNavigationView bottomNav;
    private String recordID;
    private DatabaseHelper dbhelper;
    String pet_id, name, breed, sex, age, weight, image;
    private boolean isConnected = false;
    private BluetoothGatt bluetoothGatt;
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

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent;
            switch (item.getItemId()){
                case R.id.nav_petProfile:
                    break;
                case R.id.nav_location:
                    intent = new Intent(DisplayPetDetails.this, Location.class);
                    intent.putExtra("isConnected", isConnected);
                    startActivity(intent);
                    break;
                case R.id.nav_statistics:
                    intent = new Intent(DisplayPetDetails.this, Statistics.class);
                    intent.putExtra("isConnected", isConnected);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        Intent intent = getIntent();
        recordID = intent.getStringExtra("ID");

        dbhelper = new DatabaseHelper(this);

        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
        setSupportActionBar(myToolbar);

        handler = new Handler();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothGattCallbackHandler = new BluetoothGattCallbackHandler(this, handler);

        if (bluetoothAdapter.isEnabled()) {
            isConnected = true;
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(recordID);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallbackHandler);
        } else isConnected = false;

        PetFinder myApp = PetFinder.getInstance();
        myApp.registerObserver(this);
        myApp.setIsConnected(isConnected);
        myApp.setBluetoothGatt(bluetoothGatt);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editPet) {
            Intent intent = new Intent(DisplayPetDetails.this, EditPet.class);
            intent.putExtra("isEditMode", true);
            intent.putExtra("ID", pet_id);
            intent.putExtra("NAME", name);
            intent.putExtra("BREED", breed);
            intent.putExtra("SEX", sex);
            intent.putExtra("BDATE", age);
            intent.putExtra("WEIGHT", weight);
            intent.putExtra("IMAGE", image);
            intent.putExtra("isConnected", isConnected);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("Range")
    private void showRecordDetails() {
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_ID + "=\"" + recordID + "\"";
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                pet_id = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_ID));
                name = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PETNAME));
                breed ="" +cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BREED));
                sex = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SEX));
                age = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AGE));
                weight = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_WEIGHT));
                image = ""+cursor.getString(cursor.getColumnIndex(Constants.COLUMN_IMAGE));

                petName.setText(name);
                petBreed.setText(breed);
                petSex.setText(sex);
                date.setText(age);
                petWeight.setText(weight);
                MACAddress.setText(pet_id);

                if (image.equals("null")){
                    petProfile.setImageResource(R.drawable.profile);
                } else {
                    petProfile.setImageURI(Uri.parse(image));
                }

            } while (cursor.moveToNext());
        }
        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PetFinder myApp = PetFinder.getInstance();
        myApp.unregisterObserver(this);
    }

    @Override
    public void onDataUpdated(boolean newData) {
    }
}