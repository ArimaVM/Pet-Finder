package com.example.petfinder.components;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petfinder.R;
import com.example.petfinder.pages.pet.DisplayPetDetails;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    PieChart pieChart;
    LineChart lineChart;
    private BottomNavigationView bottomNav;
    List<PieEntry> pieEntryList;
    List<LineDataSet> lineEntryList;

    private boolean isConnected = false;
    FusedLocationProviderClient fusedLocationProviderClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        isConnected = getIntent().getBooleanExtra("isConnected", false);

        TextView bluetoothWarning = findViewById(R.id.bluetoothWarning);
        if (!isConnected) {
            bluetoothWarning.setVisibility(View.VISIBLE);
        } else {
            bluetoothWarning.setVisibility(View.GONE);
        }

        pieChart = findViewById(R.id.pieChart);
        pieEntryList = new ArrayList<>();
        setupPieChart();

        lineChart = findViewById(R.id.lineChart);
        lineEntryList = new ArrayList<>();
        setupLineChart();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent;
            switch (item.getItemId()){
                case R.id.nav_petProfile:
                    intent = new Intent(Statistics.this, DisplayPetDetails.class);
                    intent.putExtra("isConnected", isConnected);
                    startActivity(intent);
                    break;
                case R.id.nav_location:
                    intent = new Intent(Statistics.this, Location.class);
                    intent.putExtra("isConnected", isConnected);
                    startActivity(intent);
                    break;
            }
            return true;
        });

    }

    private void setupPieChart() {
        pieEntryList.add(new PieEntry(20, "Inside"));
        pieEntryList.add(new PieEntry(10, "Outside"));

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "Pet Perimeter");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.white));
        pieData.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);

        pieChart.setCenterText("Perimeter");
        pieChart.invalidate();
    }

    private void setupLineChart(){
        ArrayList<Entry> dataValues = new ArrayList<>();
        dataValues.add(new Entry(0, 10));
        dataValues.add(new Entry(5, 20));
        dataValues.add(new Entry(10, 30));
        dataValues.add(new Entry(15, 40));
        dataValues.add(new Entry(20, 50));
        dataValues.add(new Entry(25, 60));
        dataValues.add(new Entry(30, 70));

        LineDataSet lineDataSet = new LineDataSet(dataValues, "Frequency");
        LineData lineData = new LineData(lineDataSet);
        lineChart.getDescription().setEnabled(false);

        lineChart.setData(lineData);
        pieChart.invalidate();
    }
}