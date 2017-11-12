package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class WelcomeScreenActivity extends AppCompatActivity {
    // Splash Screen Timing Variable
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        // Beginning of handler method to control the intent
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent at work- This takes the user to LoginActivity
                Intent CustomerLoginDriveIntent = new Intent(WelcomeScreenActivity.this, CustomerLoginActivity.class);
                startActivity(CustomerLoginDriveIntent);
                finish();
            }

        }, SPLASH_TIME_OUT);
    }
}
