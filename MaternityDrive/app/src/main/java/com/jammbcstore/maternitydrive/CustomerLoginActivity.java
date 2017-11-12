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

public class CustomerLoginActivity extends AppCompatActivity {
    // Declare Login Buttons and Edit Text to process login
    private EditText mCustomerEmail, mCustomerPassword;
    private Button mCustomerLoginBtn, mCustomerGetRegisteredBtn, mbtn_reset_password;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        // Tool bar settings and support code
        Toolbar MaternityDriveToolBar = findViewById(R.id.MaternityDriveToolBar);
        setSupportActionBar(MaternityDriveToolBar);
        // Get the current login status/ instance
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Check for user status
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // #if the user is logged in  this intent will take him to the next page the MaternityDriveServices#
                    Intent intent = new Intent(CustomerLoginActivity.this, MaternityDriveServicesActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
        // Find views by id to process login for the ### EditText & ### Button
        mCustomerEmail = findViewById(R.id.CustomerEmail);
        mCustomerPassword = findViewById(R.id.CustomerPassword);
        mCustomerLoginBtn = findViewById(R.id.CustomerLoginBtn);
        mbtn_reset_password = findViewById(R.id.btn_reset_password);
        mCustomerGetRegisteredBtn = findViewById(R.id.CustomerGetRegisteredBtn);
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
                startActivity(new Intent(CustomerLoginActivity.this, ResetPasswordActivity.class));
            }
        });
        // Sign in User
        // onTouch
        mCustomerGetRegisteredBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mCustomerGetRegisteredBtn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                } else {
                    // Change the background color here
                    mCustomerGetRegisteredBtn.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.OptionsTextColor));
                }
                return false;
            }
        });
        // Intent
        mCustomerGetRegisteredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CustomerLoginActivity.this, CustomerSignupActivity.class));
            }
        });
        // #STARTS HERE LOGIN #The OnclickListener to handle and relate to the mAuth Firebase Instance
        //  On touch
        mCustomerLoginBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // reset the background color here
                    mCustomerLoginBtn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDarkCorrect));
                } else {
                    // Change the background color here
                    mCustomerLoginBtn.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                }
                return false;
            }
        });
        mCustomerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user input text from the EditText View
                final String CustomerEmail = mCustomerEmail.getText().toString().trim();
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
                progressBar.setVisibility(View.VISIBLE);
                // Now we Login the user using this function and pass in the (CustomerEmail,CustomerPassword )
                //Authenticate User

                mAuth.signInWithEmailAndPassword(CustomerEmail, CustomerPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Make a toast message to the user THis will be when things went wrong
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(CustomerLoginActivity.this, R.string.signInError, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        // #ENDS HERE LOGIN #The OnclickListener to handle and relate to the mAuth Firebase Instance
    }

    // ########### THIS IS DONE OUT SIDE THE ONCREATE METHOD ########### ///
    // Start the firebaseAuthListener on onStart method call
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



