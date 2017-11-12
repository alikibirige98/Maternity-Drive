package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private Button mAbout, mLogout;
    private Boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Find Views By ID
        mAbout = findViewById(R.id.About);
        mLogout = findViewById(R.id.Logout);
        //Logout Intent
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;
                disconnectCustomer();
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(SettingsActivity.this, CustomerLoginActivity.class);
                startActivity(signOutIntent);
                finish();
            }
        });
        //About us
        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, AboutMaternityDriveActivity.class);
                startActivity(intent);
                return;
            }
        });

    }

    // Control logout with out crash
    private void disconnectCustomer() {
        // If the Driver has closed the app we reomve him
        //#Start# Save Updated Location using Geofire to fire baseDatabase.
        // User ID String and Database Variable  for controll in our target ref= all ambulance drivers Available
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Customers");
        // #Start# GeoFire
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
        //#End# Save Updated Location using Geofire to fire baseDatabase.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut) {
            disconnectCustomer();
        }
    }
}

