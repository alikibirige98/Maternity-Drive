package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class OptionsActivity extends AppCompatActivity {
    private ImageButton mOptionsBackBtn;
    private Button mPaymentBtn, mTripHistory, mHelpFeedBack, mProfile;
    private Button mSetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        // Find views by Id
        mOptionsBackBtn = findViewById(R.id.optionsBackBtn);
        mPaymentBtn = findViewById(R.id.PaymentBtn);
        mTripHistory = findViewById(R.id.TripHistory);
        mHelpFeedBack = findViewById(R.id.HelpFeedBack);
        mProfile = findViewById(R.id.Profile);
        mSetBtn = findViewById(R.id.SetBtn);
        // Setup Intents to drive to options set.
        // Back Button
        mOptionsBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, MaternityDriveServicesActivity.class));
                finish();
            }
        });
        //Settings
        mSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, MaternityDrive.class));
                finish();
            }
        });
        // Payment Options
        mPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, PaymentOptionsActivity.class));
                return;
            }
        });
        //Trip History
        mTripHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, TripHistoryActivity.class));
                return;
            }
        });
        //Profile
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, ProfileActivity.class));
                return;
            }
        });
        //Help Feed Back
        mHelpFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(OptionsActivity.this, HelpFeedBackActivity.class));
                return;
            }
        });

    }
}
