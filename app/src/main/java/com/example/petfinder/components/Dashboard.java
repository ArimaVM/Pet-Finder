package com.example.petfinder.components;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.DATABASE.Constants;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.bluetooth.BluetoothGattCallbackHandler;
import com.example.petfinder.container.DrawerNav;
import com.example.petfinder.container.RecordAdapter;
import com.example.petfinder.databinding.ActivityDashboardBinding;
import com.example.petfinder.pages.pet.AddPet;
import com.example.petfinder.pages.pet.ScanBluetooth;

public class Dashboard extends DrawerNav {

    ActivityDashboardBinding activityDashboardBinding;
    private RecyclerView recordsView, unlistedView;
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
        unlistedView = findViewById(R.id.recyclerView1);

        databaseHelper = new DatabaseHelper(this); // Initialize the databaseHelper object

        loadRecords(orderByNewest);

        //MAKE SURE USER IS DISCONNECTED TO BLUETOOTH
        if (!PetFinder.getInstance().getBluetoothObject().isNull()){
            BluetoothGatt gatt = PetFinder.getInstance().getBluetoothObject().getBluetoothGatt();
            gatt.disconnect();
            gatt.close();
            PetFinder.getInstance().deleteBluetoothObject();
            PetFinder.getInstance().removeCurrentMacAddress();
        }

        activityDashboardBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ScanBluetooth.class);
                startActivity(intent);
            }
        });
    }

    private void loadRecords(String orderBy) {
        currentOrderByStatus = orderBy;
        RecordAdapter recordAdapter = new RecordAdapter(Dashboard.this,
                databaseHelper.getAllRecords(orderBy), true);
        recordAdapter.setOnRefresh(new RecordAdapter.OnRefresh() {
            @Override
            public void onRefresh() {
                onResume();
            }
        });
        recordsView.setAdapter(recordAdapter);

        if (!PetFinder.getInstance().getUnlistedPets().isEmpty()) {
            findViewById(R.id.unlisted).setVisibility(View.VISIBLE);
            findViewById(R.id.recyclerView1).setVisibility(View.VISIBLE);
            RecordAdapter recordAdapter1 = new RecordAdapter(Dashboard.this,
                    PetFinder.getInstance().getUnlistedPets(), false);
            unlistedView.setAdapter(recordAdapter1);
        } else {
            findViewById(R.id.unlisted).setVisibility(View.GONE);
            findViewById(R.id.recyclerView1).setVisibility(View.GONE);
        }
    }

    private void searchRecords(String query){
        RecordAdapter recordAdapter = new RecordAdapter(Dashboard.this,
                databaseHelper.searchRecords(query), true);
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
        switch (item.getItemId()){
            case R.id.sort:
                sortOptionDialog();
                break;
            case R.id.deleteAll:
                openDeleteDialouge();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDeleteDialouge() {
        DeleteAlertDialogue.ShowDeleteAlertDialogue showDeleteAlertDialogue = new DeleteAlertDialogue().setDatabaseHelper(this).setToAll();
        showDeleteAlertDialogue.onActionFinished(new DeleteAlertDialogue.ShowDeleteAlertDialogue.ActionFinished() {
            @Override
            public void onActionFinished(Boolean value) {
                loadRecords(orderByNewest);
            }
        });
        showDeleteAlertDialogue.DeleteShow();
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

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast toast = Toast.makeText(this, "Click again to exit.", Toast.LENGTH_SHORT);
        toast.show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}