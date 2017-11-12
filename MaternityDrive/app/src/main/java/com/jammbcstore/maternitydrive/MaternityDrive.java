package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MaternityDrive extends AppCompatActivity {
    private Button mLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maternity_drive);
        // Logout button
        mLogout = findViewById(R.id.Logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(MaternityDrive.this, CustomerLoginActivity.class);
                startActivity(signOutIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MaternityDrive.this, CustomerLoginActivity.class));
        finish();
    }
}
