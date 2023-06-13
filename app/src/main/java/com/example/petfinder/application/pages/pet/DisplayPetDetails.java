package com.example.petfinder.application.pages.pet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.petfinder.application.DATABASE.Constants;
import com.example.petfinder.application.DATABASE.DatabaseHelper;
import com.example.petfinder.application.components.Location;
import com.example.petfinder.R;
import com.example.petfinder.application.components.Statistics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DisplayPetDetails extends AppCompatActivity {

    private CircularImageView petProfile;
    private TextView petName, petBreed, petSex, date, petWeight;
    private BottomNavigationView bottomNav;
    private String recordID;
    private DatabaseHelper dbhelper;
    String pet_id, name, breed, sex, age, weight, image;

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

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_petProfile:
                        break;
                    case R.id.nav_location:
                        startActivity(new Intent(DisplayPetDetails.this, Location.class));
                        break;
                    case R.id.nav_statistics:
                        startActivity(new Intent(DisplayPetDetails.this, Statistics.class));
                        break;
                }
                return true;
            }
        });

        Intent intent = getIntent();
        recordID = intent.getStringExtra("RECORD_ID");

        dbhelper = new DatabaseHelper(this);

        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
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
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRecordDetails() {
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.COLUMN_ID + "=\"" + recordID + "\"";
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                pet_id = ""+cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID));
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

                if (image.equals("null")){
                    petProfile.setImageResource(R.drawable.profile);
                }
                else {
                    petProfile.setImageURI(Uri.parse(image));
                }

            } while (cursor.moveToNext());
        }
        db.close();
    }
}