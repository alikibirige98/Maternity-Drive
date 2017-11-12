package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MaternityDriveServicesActivity extends AppCompatActivity {
    //Declare Button Variables
    private Button mMaternityDriveAmbulanceService, mMaternityDriveShoppingService, mMaternityDriveSpecialService, mMaternityDriveMaidService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maternity_drive_services);
        // Find Views By Id
        mMaternityDriveAmbulanceService = findViewById(R.id.MaternityDriveAmbulanceService);
        mMaternityDriveShoppingService = findViewById(R.id.MaternityDriveShoppingService);
        mMaternityDriveSpecialService = findViewById(R.id.MaternityDriveSpecialService);
        mMaternityDriveMaidService = findViewById(R.id.MaternityDriveMaidService);
        // #Start# Setonclick Listener to drive to Respective activities
        mMaternityDriveAmbulanceService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AmbulanceCustomerMapActivity Intent
                Intent AmbulanceCustomerMapActivity = new Intent(MaternityDriveServicesActivity.this, AmbulanceCustomerMapActivity.class);
                startActivity(AmbulanceCustomerMapActivity);
                finish();
                return;
            }
        });
        mMaternityDriveShoppingService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CustomerShoppingActivity Intent
                Intent CustomerShoppingActivity = new Intent(MaternityDriveServicesActivity.this, MaternityDriveServicesActivity.class);
                startActivity(CustomerShoppingActivity);
                finish();
                return;
            }
        });
        mMaternityDriveSpecialService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SpecialCustomerMapActivity Intent
                Intent SpecialCustomerMapActivity = new Intent(MaternityDriveServicesActivity.this, SpecialCustomerMapActivity.class);
                startActivity(SpecialCustomerMapActivity);
                finish();
                return;
            }
        });
        mMaternityDriveMaidService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MidWifeCustomerMapActivity Intent
                Intent MidWifeCustomerMapActivity = new Intent(MaternityDriveServicesActivity.this, MidwifeCustomerMapActivity.class);
                startActivity(MidWifeCustomerMapActivity);
                finish();
                return;
            }
        });
        // #End# Setonclick Listener to drive to Respective activities
    }
}
