package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SpecialDriverOptionsActivity extends AppCompatActivity {
    private ImageButton mOptionsBackBtn;
    private Button mTripHistory, mSettings, mEarnings, mLogout;
    private Boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_driver_options);
        // Find views by Id
        mOptionsBackBtn = findViewById(R.id.optionsBackBtn);
        mTripHistory = findViewById(R.id.TripHistory);
        mSettings = findViewById(R.id.Settings);
        mEarnings = findViewById(R.id.Earnings);
        mLogout = findViewById(R.id.Logout);
        // Setup Intents to drive to options set.
        // Onclick
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(SpecialDriverOptionsActivity.this, MaternityDriverWelcomeScreen.class);
                startActivity(signOutIntent);
                finish();
                return;
            }
        });

        // Back Button
        mOptionsBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(SpecialDriverOptionsActivity.this, SpecialDriverMapActivity.class));
                finish();
            }
        });
        // Earnings
        mEarnings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(SpecialDriverOptionsActivity.this, SpecialDriverEarnsActivity.class));
                return;
            }
        });

        //Trip History
        mTripHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(SpecialDriverOptionsActivity.this, SpecialDriverTripHistoryActivity.class));
                return;
            }
        });

        //Settings
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
                startActivity(new Intent(SpecialDriverOptionsActivity.this, SpecialDriverSettingsActivity.class));
                finish();
                return;
            }
        });

    }

    // Control logout with out crash
    private void disconnectDriver() {
        // If the Driver has closed the app we reomve him
        //#Start# Save Updated Location using Geofire to fire baseDatabase.
        // User ID String and Database Variable  for controll in our target ref= all ambulance drivers Available
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("SpecialDriversAvailable");
        // #Start# GeoFire
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
        //#End# Save Updated Location using Geofire to fire baseDatabase.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut) {
            disconnectDriver();
        }

    }
}
