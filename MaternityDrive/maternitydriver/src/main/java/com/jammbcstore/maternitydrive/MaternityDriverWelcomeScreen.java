package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MaternityDriverWelcomeScreen extends AppCompatActivity {
    // Button variables
    private Button mAmbulanceDriver, mMidwifeDriver, mSpecialDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maternity_driver_welcome_screen);
        // Get Driver Buttons By Id
        mAmbulanceDriver = findViewById(R.id.AmbulanceDriver);
        mMidwifeDriver = findViewById(R.id.MidwifeDriver);
        mSpecialDriver = findViewById(R.id.SpecialDriver);
        // Onclick Listener to drive Driver to his respective Login Activity
        // #Ambulance Driver Intent#
        // Intent
        mAmbulanceDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MaternityDriverWelcomeScreen.this, AmbulanceDriverLoginActivity.class);
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(intent);
                finish();
                return;
            }
        });
        // #Midwife Driver Intent#
        //Intent
        mMidwifeDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MaternityDriverWelcomeScreen.this, MidwifeDriverLoginActivity.class);
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(intent);
                finish();
                return;
            }
        });
        // #Special Driver Intent#
        //Intent
        mSpecialDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MaternityDriverWelcomeScreen.this, SpecialDriverLoginActivity.class);
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
