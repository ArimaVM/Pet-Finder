package com.example.petfinder.pages.pet;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.petfinder.DATABASE.reports.GPSReport;
import com.example.petfinder.DATABASE.reports.GPSReport.GPSMode;
import com.example.petfinder.DATABASE.reports.PedometerReport;
import com.example.petfinder.R;
import com.example.petfinder.application.PetFinder;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reports extends AppCompatActivity {

    private static class Mode {
        static final int DAILY = 1;
        static final int MONTHLY = 2;
    }

    private ViewPager viewPager;
    private LinearLayout indicatorLayout;
    private int[] slideLayouts = {R.layout.reports_pedometer, R.layout.reports_gps1, R.layout.reports_gps2, R.layout.reports_gps3};
    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_reports);

        Toolbar myToolbar = findViewById(R.id.pet_toolbar);
        setSupportActionBar(myToolbar);

        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_petProfile:
                        startActivity(new Intent(Reports.this, DisplayPetDetails.class));
                        finish();
                        break;
                    case R.id.nav_location:
                        startActivity(new Intent(Reports.this, Location.class));
                        finish();
                        break;
                }
                return true;
            }
        });

        //VIEWPAGER
        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        SliderAdapter sliderAdapter = new SliderAdapter();
        viewPager.setAdapter(sliderAdapter);

        addIndicators();
        updateIndicators(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void addIndicators() {
        for (int i = 0; i < slideLayouts.length; i++) {
            ImageView indicator = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            indicator.setImageResource(R.drawable.indicator_inactive); // Set inactive indicator drawable
            indicatorLayout.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            ImageView indicator = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                indicator.setImageResource(R.drawable.indicator_active); // Set active indicator drawable
            } else {
                indicator.setImageResource(R.drawable.indicator_inactive); // Set inactive indicator drawable
            }
        }
    }

    private class SliderAdapter extends PagerAdapter {

        TextView titleReports;
        TextView NoData;

        //variables for pedometer report
        BarChart mBarChart;
        Button daily, monthly;
        TextView pedometerData, pedometerConversion;

        //variables for gps report
        GPSReport gpsReport;

        //variables for reports gps 1
        TextView gpsData, gpsDataLabel;
        Button dailyGPS1, weeklyGPS1, monthlyGPS1;
        PieChart gpsPieChart;

        //variables for reports gps 2
        Button dailyGPS2, weeklyGPS2, monthlyGPS2;
        GoogleMap googleMap2;

        //variables for reports gps 3
        GoogleMap googleMap3;

        @Override
        public int getCount() {
            return slideLayouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(Reports.this);
            ViewGroup layout = (ViewGroup) inflater.inflate(slideLayouts[position], container, false);

            //ACCESS THE LAYOUTS HERE
            titleReports = layout.findViewById(R.id.titleReports);

            //reports_pedometer.xml
            switch (slideLayouts[position]) {
                case R.layout.reports_pedometer:
                    titleReports.setText(PetFinder.getInstance().getCurrentPetModel().getName() + "'s Daily Steps");
                    mBarChart = layout.findViewById(R.id.pedometerBarGraph);
                    daily = layout.findViewById(R.id.dailyReportsPedometer);
                    monthly = layout.findViewById(R.id.monthlyReportsPedometer);
                    monthly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            monthlyButtonClicked();
                        }
                    });
                    pedometerData = layout.findViewById(R.id.pedometerData);
                    pedometerConversion = layout.findViewById(R.id.pedometerConversion);
                    setPedometerBarChart(Mode.DAILY);

                    break;
                case R.layout.reports_gps1:
                    titleReports.setText(PetFinder.getInstance().getCurrentPetModel().getName() + "'s Geofence Report");
                    gpsReport = new GPSReport(Reports.this);
                    if (gpsReport.hasGeofence()) {
                        layout.findViewById(R.id.geofenceChart).setVisibility(View.VISIBLE);
                        layout.findViewById(R.id.noGeofence).setVisibility(View.GONE);


                        dailyGPS1 = layout.findViewById(R.id.dailyReportsGPS1);
                        weeklyGPS1 = layout.findViewById(R.id.weeklyReportsGPS1);
                        weeklyGPS1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                weeklyButtonClickedGPS1();
                            }
                        });
                        monthlyGPS1 = layout.findViewById(R.id.monthlyReportsGPS1);
                        monthlyGPS1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                monthlyButtonClickedGPS1();
                            }
                        });

                        gpsData = layout.findViewById(R.id.gpsData);
                        gpsDataLabel = layout.findViewById(R.id.gpsDataLabel);
                        gpsPieChart = layout.findViewById(R.id.gpsPieChart);
                        showGPSPieChart(GPSMode.DAILY);
                    } else {
                        layout.findViewById(R.id.noGeofence).setVisibility(View.VISIBLE);
                        layout.findViewById(R.id.geofenceChart).setVisibility(View.GONE);
                    }
                    break;
                case R.layout.reports_gps2:
                    titleReports.setText(PetFinder.getInstance().getCurrentPetModel().getName() + "'s Visited Areas");
                    NoData = layout.findViewById(R.id.NoData);
                    gpsReport = new GPSReport(Reports.this);

                    dailyGPS2 = layout.findViewById(R.id.dailyReportsGPS2);
                    weeklyGPS2 = layout.findViewById(R.id.weeklyReportsGPS2);
                    weeklyGPS2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            weeklyButtonClickedGPS2();
                        }
                    });
                    monthlyGPS2 = layout.findViewById(R.id.monthlyReportsGPS2);
                    monthlyGPS2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            monthlyButtonClickedGPS2();
                        }
                    });
                    MapView mapView = layout.findViewById(R.id.GoogleMaps2);
                    mapView.onCreate(savedInstanceState);
                    mapView.onResume();

                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap2 = googleMap;
                            showGPSMap1(GPSMode.DAILY);
                            Integer MAP_STYLE_CONSTANT = PetFinder.getInstance().getMapPreferences().getMapStyle();
                            if (MAP_STYLE_CONSTANT!=null) googleMap.setMapType(MAP_STYLE_CONSTANT);
                            else googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                    });

                    break;
                case R.layout.reports_gps3:
                    titleReports.setText(PetFinder.getInstance().getCurrentPetModel().getName() + "'s Route Today");
                    NoData = layout.findViewById(R.id.NoData);
                    gpsReport = new GPSReport(Reports.this);

                    MapView mapView2 = layout.findViewById(R.id.GoogleMaps3);
                    mapView2.onCreate(savedInstanceState);
                    mapView2.onResume();

                    mapView2.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap3 = googleMap;
                            showGPSMap2();
                            Integer MAP_STYLE_CONSTANT = PetFinder.getInstance().getMapPreferences().getMapStyle();
                            if (MAP_STYLE_CONSTANT!=null) googleMap.setMapType(MAP_STYLE_CONSTANT);
                            else googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                    });
                    break;
            }

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        //FUNCTIONS FOR EACH LAYOUT
        //reports_pedometer.xml
        private void dailyButtonClicked() {
            setPedometerBarChart(Mode.DAILY);

            daily.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            monthly.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            daily.setOnClickListener(null);
            monthly.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthlyButtonClicked();
                }
            });
        }
        private void monthlyButtonClicked() {
            setPedometerBarChart(Mode.MONTHLY);

            monthly.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            daily.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            monthly.setOnClickListener(null);
            daily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dailyButtonClicked();
                }
            });
        }
        private void setPedometerBarChart(int mode) {
            PedometerReport pedometerReport = new PedometerReport(Reports.this);

            ArrayList<BarEntry> entries = new ArrayList<>();
            final ArrayList<String> xAxisLabel = new ArrayList<>();

            Object[] todayData = pedometerReport.getToday();
            String conversionData = todayData[1] + ((Double) todayData[1] > 1 ? " meters" : " meter");
            pedometerData.setText(String.valueOf(todayData[0]));
            pedometerConversion.setText(conversionData);

            if (mode == Mode.DAILY) {
                List<Integer> valuesList = pedometerReport.getDaily();
                for (int i = 0; i < 7; i++) {
                    BarEntry barEntry = new BarEntry(i + 1, valuesList.get(i));
                    entries.add(barEntry);
                }

                xAxisLabel.add("Sun");
                xAxisLabel.add("Mon");
                xAxisLabel.add("Tue");
                xAxisLabel.add("Wed");
                xAxisLabel.add("Thu");
                xAxisLabel.add("Fri");
                xAxisLabel.add("Sat");
                xAxisLabel.add("");//empty label for the last vertical grid line on Y-Right Axis
            } else if (mode == Mode.MONTHLY) {
                List<Integer> valuesList = pedometerReport.getMonthly();
                for (int i = 0; i < 12; i++) {
                    BarEntry barEntry = new BarEntry(i + 1, valuesList.get(i));
                    entries.add(barEntry);
                }

                xAxisLabel.add("Jan");
                xAxisLabel.add("Feb");
                xAxisLabel.add("Mar");
                xAxisLabel.add("Apr");
                xAxisLabel.add("May");
                xAxisLabel.add("Jun");
                xAxisLabel.add("Jul");
                xAxisLabel.add("Aug");
                xAxisLabel.add("Sep");
                xAxisLabel.add("Oct");
                xAxisLabel.add("Nov");
                xAxisLabel.add("Dec");
                xAxisLabel.add("");//empty label for the last vertical grid line on Y-Right Axis
            }
            XAxis xAxis = mBarChart.getXAxis();
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setTextSize(14);
            xAxis.setDrawAxisLine(true);
            xAxis.setAxisLineColor(Color.BLACK);
            xAxis.setDrawGridLines(true);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setAxisMinimum(0 + 0.5f);
            xAxis.setAxisMaximum(xAxisLabel.size() - 0.5f);
            xAxis.setLabelCount(xAxisLabel.size(), true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setXOffset(0f);
            xAxis.setYOffset(0f);
            xAxis.setCenterAxisLabels(true);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < xAxisLabel.size()) {
                        return xAxisLabel.get(index);
                    }
                    return "";
                }
            });

            int highestPedometer = pedometerReport.getHighestPedometer();

            YAxis lAxis = mBarChart.getAxisLeft();
            lAxis.setTextColor(Color.BLACK);
            lAxis.setTextSize(14);
            lAxis.setDrawAxisLine(true);
            lAxis.setAxisLineColor(Color.BLACK);
            lAxis.setDrawGridLines(true);
            lAxis.setGranularity(1f);
            lAxis.setGranularityEnabled(true);
            lAxis.setAxisMinimum(0);
            lAxis.setAxisMaximum(highestPedometer);
            lAxis.setLabelCount((highestPedometer / 5) + 1, true);
            lAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

            YAxis rAxis = mBarChart.getAxisRight();
            rAxis.setAxisMinimum(0);
            rAxis.setDrawAxisLine(true);
            rAxis.setLabelCount(0, true);
            rAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });

            BarDataSet barDataSet = new BarDataSet(entries, mode == Mode.DAILY ? "Daily" : "Monthly");
            barDataSet.setColor(getResources().getColor(R.color.orange));
            barDataSet.setFormSize(15f);
            barDataSet.setDrawValues(true);
            barDataSet.setValueTextSize(12f);

            BarData data = new BarData(barDataSet);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            mBarChart.setData(data);
            mBarChart.setScaleEnabled(false);
            mBarChart.getLegend().setEnabled(false);
            mBarChart.setDrawBarShadow(false);
            mBarChart.getDescription().setEnabled(false);
            mBarChart.setPinchZoom(false);
            mBarChart.setDrawGridBackground(true);
            mBarChart.invalidate();
        }

        //reports_gps1.xml
        private void dailyButtonClickedGPS1() {
            showGPSPieChart(GPSMode.DAILY);

            dailyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            weeklyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            monthlyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            dailyGPS1.setOnClickListener(null);
            weeklyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weeklyButtonClickedGPS1();
                }
            });
            monthlyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthlyButtonClickedGPS1();
                }
            });
        }
        private void weeklyButtonClickedGPS1() {
            showGPSPieChart(GPSMode.WEEKLY);

            dailyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            weeklyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            monthlyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            weeklyGPS1.setOnClickListener(null);
            dailyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dailyButtonClickedGPS1();
                }
            });
            monthlyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthlyButtonClickedGPS1();
                }
            });
        }
        private void monthlyButtonClickedGPS1() {
            showGPSPieChart(GPSMode.MONTHLY);

            dailyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            weeklyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            monthlyGPS1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));

            monthlyGPS1.setOnClickListener(null);
            weeklyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weeklyButtonClickedGPS1();
                }
            });
            dailyGPS1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dailyButtonClickedGPS1();
                }
            });
        }

        private void showGPSPieChart(GPSMode gpsMode) {

            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            String label = "Geofence";

            //initializing data
            Map<String, Double> typeAmountMap = new HashMap<>();
            Double inside = gpsReport.getPercentageToday(gpsMode).inside();
            Double outside = gpsReport.getPercentageToday(gpsMode).outside();
            if (inside == 0 && outside == 0){
                typeAmountMap.put("No Data", 100.0);
            } else {
                typeAmountMap.put("Inside", inside * 100); //convert to %
                typeAmountMap.put("Outside", outside * 100); //convert to %
            }

            //initializing colors for the entries
            ArrayList<Integer> colors = new ArrayList<>();
            if (inside == 0 && outside == 0){
                colors.add(getResources().getColor(R.color.grey));
                gpsData.setText("N/A");
                gpsDataLabel.setText("No data for "+(gpsMode==GPSMode.DAILY?"today.":gpsMode==GPSMode.WEEKLY?"this week.":"this month."));
            } else if (typeAmountMap.get("Inside") < typeAmountMap.get("Outside")) {
                colors.add(getResources().getColor(R.color.purple_700));
                colors.add(getResources().getColor(R.color.orange));
                gpsData.setText(new DecimalFormat("#0.00").format(typeAmountMap.get("Outside")) + "%");
                gpsDataLabel.setText("of the time was spent OUTSIDE.");
            } else {
                colors.add(getResources().getColor(R.color.orange));
                colors.add(getResources().getColor(R.color.purple_700));
                gpsData.setText(new DecimalFormat("#0.00").format(typeAmountMap.get("Inside")) + "%");
                gpsDataLabel.setText("of the time was spent INSIDE.");
            }

            for (String type : typeAmountMap.keySet()) {
                pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
            }

            PieDataSet pieDataSet = new PieDataSet(pieEntries, label);
            pieDataSet.setValueTextSize(12f);
            pieDataSet.setColors(colors);

            PieData pieData = new PieData(pieDataSet);
            pieData.setDrawValues(false);

            gpsPieChart.getDescription().setEnabled(false);
            gpsPieChart.getLegend().setEnabled(false);

            gpsPieChart.setData(pieData);
            gpsPieChart.invalidate();
        }

        //reports_gps2.xml
        private void dailyButtonClickedGPS2() {
            showGPSMap1(GPSMode.DAILY);

            dailyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            weeklyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            monthlyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            dailyGPS2.setOnClickListener(null);
            weeklyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weeklyButtonClickedGPS2();
                }
            });
            monthlyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthlyButtonClickedGPS2();
                }
            });
        }
        private void weeklyButtonClickedGPS2() {
            showGPSMap1(GPSMode.WEEKLY);

            dailyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            weeklyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
            monthlyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            weeklyGPS2.setOnClickListener(null);
            dailyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dailyButtonClickedGPS2();
                }
            });
            monthlyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    monthlyButtonClickedGPS2();
                }
            });
        }
        private void monthlyButtonClickedGPS2() {
            showGPSMap1(GPSMode.MONTHLY);

            dailyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            weeklyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            monthlyGPS2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));

            monthlyGPS2.setOnClickListener(null);
            weeklyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weeklyButtonClickedGPS2();
                }
            });
            dailyGPS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dailyButtonClickedGPS2();
                }
            });
        }

        private void showGPSMap1(GPSMode gpsMode){
            GPSReport.GeodeticCentroid geodeticCentroid = gpsReport.getGeodeticCentroid(gpsMode);
            GPSReport.MarkerRadius geofence = geodeticCentroid.getGeofence();
            LatLng view = geodeticCentroid.getCenterView();
            if (view==null) { //if there is no `view` there may also be no data retrieved.
                view = geofence.getLatLng();
                NoData.setVisibility(View.VISIBLE);
            } else {
                NoData.setVisibility(View.GONE);
            }
            List<GPSReport.MarkerRadius> points = geodeticCentroid.getMarkers();


            googleMap2.clear();

            //setGeofence
            if (geofence.getLatLng()!=null){
                setMarker(googleMap2,
                        geofence.getLatLng(),
                        (double) geofence.getRadius(),
                        "#E6A326",
                        0.5,
                        R.color.teal_transparent_700,
                        R.color.teal_transparent_200);
            }
            //draw points
            for (GPSReport.MarkerRadius point : points) {
                setMarker(googleMap2,
                        point.getLatLng(),
                        point.getRadius()/10d,
                        "#6200EE",
                        1.0,
                        R.color.purple_700,
                        R.color.purple_200);
            }
            //set map view
            if (view!=null) googleMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(view, 20));

        }
        private void setMarker(GoogleMap googleMap, LatLng latLng, Double radius, String color, Double scale, Integer strokeColor, Integer fillColor){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng)
                    .icon(BitmapFromVector(changeDrawableColor(R.drawable.circle_marker, Color.parseColor(color)), scale))
                    .anchor(0.5f, 0.5f);

            googleMap.addMarker(markerOptions);
            googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(radius)
                    .strokeColor(getResources().getColor(strokeColor))
                    .fillColor(getResources().getColor(fillColor)));
        }

        private BitmapDescriptor BitmapFromVector(Drawable vectorDrawable, Double scaleFactor) {
            int width = (int) (vectorDrawable.getIntrinsicWidth() * scaleFactor);
            int height = (int) (vectorDrawable.getIntrinsicHeight() * scaleFactor);
            vectorDrawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        private Drawable changeDrawableColor(Integer drawableID, Integer color) {
            Drawable modifiedDrawable = getResources().getDrawable(drawableID).mutate();
            modifiedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return modifiedDrawable;
        }

        //reports_gps3.xml
        private void showGPSMap2(){
            GPSReport.MarkerConnections markerConnections = gpsReport.getMarkerConnections();
            GPSReport.MarkerRadius geofence = markerConnections.getGeofence();
            LatLng view = markerConnections.getCenterView();
            if (view==null) { //if there is no `view` there may also be no data retrieved.
                view = geofence.getLatLng();
                NoData.setVisibility(View.VISIBLE);
            } else {
                NoData.setVisibility(View.GONE);
            }
            List<List<LatLng>> points = markerConnections.getData();

            googleMap3.clear();

            //setGeofence
            if (geofence.getLatLng()!=null){
                setMarker(googleMap3,
                        geofence.getLatLng(),
                        (double) geofence.getRadius(),
                        "#E6A326",
                        0.5,
                        R.color.teal_transparent_700,
                        R.color.teal_transparent_200);
            }

            //draw lines
            LatLng end = null;
            int iteration = 0;
            for (List<LatLng> point : points) {

                iteration++;
                drawMarker(point);

                if (iteration==1){
                    end = point.get(point.size()-1);
                } else {
                    List<LatLng> connectingMarker = new ArrayList<>();
                    connectingMarker.add(end);
                    connectingMarker.add(point.get(0));
                    drawConnectingMarker(connectingMarker);
                    end = point.get(point.size()-1);
                }
            }

            //set map view
            if (view!=null) googleMap3.animateCamera(CameraUpdateFactory.newLatLngZoom(view, 20));

        }
        private void drawMarker(List<LatLng> l) {
            PolylineOptions options = new PolylineOptions();
            options.color(R.color.orange);

            for (int i = 0; i < l.size(); i++) {
                options.add(l.get(i));
                MarkerOptions marker = new MarkerOptions().position(l.get(i));
                marker.icon(BitmapFromVector(changeDrawableColor(R.drawable.circle_marker, Color.parseColor("#6200EE")), 0.3));
                googleMap3.addMarker(marker);
            }
            googleMap3.addPolyline(options);
        }
        private void drawConnectingMarker(List<LatLng> l){
            PolylineOptions options = new PolylineOptions();
            options.color(R.color.orange);
            options.pattern(Arrays.asList(new Dash(10), new Gap(5)));

            for (int i = 0; i < l.size(); i++) {
                options.add(l.get(i));
                MarkerOptions marker = new MarkerOptions().position(l.get(i));
                marker.icon(BitmapFromVector(changeDrawableColor(R.drawable.circle_marker, Color.parseColor("#6200EE")), 0.3));
                googleMap3.addMarker(marker);
            }
            googleMap3.addPolyline(options);
        }
    }
}