package com.example.petfinder.components;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.container.RecordAdapter;
import com.example.petfinder.databinding.ActivityDashboardBinding;
import com.example.petfinder.pages.pet.AddPet;

public class Dashboard extends DrawerNav {

    ActivityDashboardBinding activityDashboardBinding;
    private RecyclerView recordsView;
    private DatabaseHelper databaseHelper;
    String orderByNewest = Constants.COLUMN_ADDED_TIMESTAMP + " DESC";
    String orderByOldest = Constants.COLUMN_ADDED_TIMESTAMP + " ASC";
    String orderByTitleAsc = Constants.COLUMN_PETNAME + " ASC";
    String orderByTitleDesc = Constants.COLUMN_PETNAME + " DESC";

    String currentOrderByStatus = orderByNewest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardBinding.getRoot());

        allocateActivityTitle("Dashboard");

        recordsView = findViewById(R.id.recyclerView);

        databaseHelper = new DatabaseHelper(this); // Initialize the databaseHelper object

        loadRecords(orderByNewest);

        activityDashboardBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, AddPet.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    private void loadRecords(String orderBy) {
        currentOrderByStatus = orderBy;
        RecordAdapter recordAdapter = new RecordAdapter(Dashboard.this,
                databaseHelper.getAllRecords(orderBy));
        recordsView.setAdapter(recordAdapter);
    }

    private void searchRecords(String query){
        RecordAdapter recordAdapter = new RecordAdapter(Dashboard.this,
                databaseHelper.searchRecords(query));
        recordsView.setAdapter(recordAdapter);
    }

    protected void onResume() {
        super.onResume();
        loadRecords(currentOrderByStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRecords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchRecords(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id ==R.id.sort){
            sortOptionDialog();
        }
        else if (id ==R.id.deleteAll){
            databaseHelper.deleteAllData();
            onResume();
        }

        return super.onOptionsItemSelected(item);


    }

    private void sortOptionDialog() {
        String[] options = {"Title Ascending", "Title Descending", "Newest", "Oldest"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which ==0){
                            loadRecords(orderByTitleAsc);
                        }
                        else if (which ==1) {
                            loadRecords(orderByTitleDesc);
                        }
                        else if (which ==2) {
                            loadRecords(orderByNewest);
                        }
                        else if (which ==3) {
                            loadRecords(orderByOldest);
                        }
                    }
                })
                .create().show();
    }
}