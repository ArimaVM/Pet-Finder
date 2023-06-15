package com.example.petfinder.pages.NotFound;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.petfinder.R;

public class NotFound extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_found);

        TextView mReason = findViewById(R.id.reason);
        mReason.setText(getIntent().getStringExtra("REASON"));
    }
}