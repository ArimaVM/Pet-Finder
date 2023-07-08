package com.example.petfinder.container;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.petfinder.R;
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.components.Settings;
import com.google.android.material.navigation.NavigationView;

public class DrawerNav extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;

    public void setContentView(View view){
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_nav, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()){

            case R.id.nav_dashboard:
                startActivity(new Intent(DrawerNav.this, Dashboard.class));
                break;

            case R.id.nav_settings:
                startActivity(new Intent(DrawerNav.this, Settings.class));
                break;

            default:
                return true;
        }
        return true;
    }

    protected void allocateActivityTitle (String titlesString) {
        if (getSupportActionBar() !=null) {
            getSupportActionBar().setTitle(titlesString);
        }
    }
}