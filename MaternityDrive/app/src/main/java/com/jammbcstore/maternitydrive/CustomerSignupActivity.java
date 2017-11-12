package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerSignupActivity extends AppCompatActivity {
    ProgressBar progressBar;
    // Globals
    private EditText mCustomerEmail, mCustomerPassword;
    private Button mCustomerSignupBtn, mbtn_reset_password, mCustomerLoginBtn;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);
        // Tool bar settings and support code
        Toolbar MaternityDriveToolBar = findViewById(R.id.MaternityDriveToolBar);
        setSupportActionBar(MaternityDriveToolBar);
        // Get instance
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(CustomerSignupActivity.this, MaternityDriveServicesActivity.class));
                    finish();

                }
            }
        };

        // Find View BY ID
        mCustomerEmail = findViewById(R.id.CustomerEmail);
        mCustomerPassword = findViewById(R.id.CustomerPassword);
        // Buttons
        mCustomerSignupBtn = findViewById(R.id.CustomerSignupBtn);
        mbtn_reset_password = findViewById(R.id.btn_reset_password);
        mCustomerLoginBtn = findViewById(R.id.CustomerLoginBtn);
        // Progress Bar
        progressBar = findViewById(R.id.progressBar);
        // Password Reset
        mbtn_reset_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mbtn_reset_password.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                } else {
                    // Change the background color here
                    mbtn_reset_password.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.OptionsTextColor));
                }
                return false;
            }
        });
        mbtn_reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CustomerSignupActivity.this, ResetPasswordActivity.class));
            }
        });
        // SignUp Development
        mCustomerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
        // Signup / Register
        mCustomerSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user input text from the EditText View
                final String CustomerEmail = mCustomerEmail.getText().toString();
                final String CustomerPassword = mCustomerPassword.getText().toString();
                // Validation
                if (TextUtils.isEmpty(CustomerEmail)) {
                    Toast.makeText(getApplicationContext(), R.string.enterEmail, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(CustomerPassword)) {
                    Toast.makeText(getApplicationContext(), R.string.enterPassword, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (CustomerPassword.length() < 8) {
                    Toast.makeText(getApplicationContext(), R.string.short_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                // Now we create the user using this function and pass in the (CustomerEmail,CustomerPassword )
                mAuth.createUserWithEmailAndPassword(CustomerEmail, CustomerPassword).addOnCompleteListener(CustomerSignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Make a toast message to the user THis will be when things went wrong
                        if (!task.isSuccessful()) {
                            Toast.makeText(CustomerSignupActivity.this, R.string.signUpError, Toast.LENGTH_SHORT).show();
                        }
                        // If the above is wrong every thing is okay then this code will be run.
                        // What we are doing here is to create the user i mean putting the user to the  database
                        else {
                            // Here we get the user id the current one and put it to database
                            String User_Id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(User_Id);
                            // So now set the database Reference to true so that the user is saved to the database
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    // Remove the firebaseAuthListener onStop method call
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}

