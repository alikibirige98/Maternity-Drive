package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.jammbcstore.maternitydrive.R.id.AmbulanceDriverEmail;

public class AmbulanceDriverLoginActivity extends AppCompatActivity {
    // Declare Login Buttons and Edit Text to process login
    private EditText mAmbulanceDriverEmail, mAmbulanceDriverPassword;
    private Button mAmbulanceDriverLoginBtn, mAmbulanceDriverRegisterBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_driver_login);
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
                    // #if the user is logged in  this intent will take him to the next page the AmbulanceDriverActivity#
                    Intent intent = new Intent(AmbulanceDriverLoginActivity.this, AmbulanceDriverMapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        // Find views by id to process login for the ### EditText & ### Button
        mAmbulanceDriverEmail = findViewById(AmbulanceDriverEmail);
        mAmbulanceDriverPassword = findViewById(R.id.AmbulanceDriverPassword);
        mAmbulanceDriverLoginBtn = findViewById(R.id.AmbulanceDriverLoginBtn);
        mAmbulanceDriverRegisterBtn = findViewById(R.id.AmbulanceDriverRegisterBtn);
        // #STARTS HERE REGISTER #The OnclickListener to handle and relate to the mAuth Firebase Instance
        // Intent
        mAmbulanceDriverRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user input text from the EditText View
                final String AmbulanceDriverEmail = mAmbulanceDriverEmail.getText().toString();
                final String AmbulanceDriverPassword = mAmbulanceDriverPassword.getText().toString();
                // Now we create the user using this function and pass in the (AmbulanceDriverEmail,AmbulanceDriverPassword )
                mAuth.createUserWithEmailAndPassword(AmbulanceDriverEmail, AmbulanceDriverPassword).addOnCompleteListener(AmbulanceDriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Make a toast message to the user THis will be when things went wrong
                        if (!task.isSuccessful()) {
                            Toast.makeText(AmbulanceDriverLoginActivity.this, R.string.signUpError, Toast.LENGTH_SHORT).show();
                        }
                        // If the above is wrong every thing is okay then this code will be run.
                        // What we are doing here is to create the user i mean putting the user to the  database

                        else {
                            // Here we get the user id the current one and put it to database
                            String User_Id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(User_Id).child("AmbulanceDriverEmail");
                            // So now set the database Reference to true so that the user is saved to the database
                            current_user_db.setValue(AmbulanceDriverEmail);
                        }
                    }
                });
            }
        });
        // #ENDS HERE REGISTER #The OnclickListener to handle and relate to the mAuth Firebase Instance
        // #STARTS HERE LOGIN #The OnclickListener to handle and relate to the mAuth Firebase Instance
        mAmbulanceDriverLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user input text from the EditText View
                final String AmbulanceDriverEmail = mAmbulanceDriverEmail.getText().toString();
                final String AmbulanceDriverPassword = mAmbulanceDriverPassword.getText().toString();
                // Now we Login the user using this function and pass in the (AmbulanceDriverEmail,AmbulanceDriverPassword )
                mAuth.signInWithEmailAndPassword(AmbulanceDriverEmail, AmbulanceDriverPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Make a toast message to the user THis will be when things went wrong
                        if (!task.isSuccessful()) {
                            Toast.makeText(AmbulanceDriverLoginActivity.this, R.string.signInError, Toast.LENGTH_SHORT).show();
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

