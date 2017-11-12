package com.jammbcstore.maternitydrive;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static com.jammbcstore.maternitydrive.R.id.CustomerEmail;

public class ResetPasswordActivity extends AppCompatActivity {
    // Globals
    private ImageButton moptionsBackBtn;
    private EditText mCustomerEmail;
    private Button mbtn_reset_password;
    private ProgressBar progressBar;
    //Firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        // Tool bar settings and support code
        Toolbar MaternityDriveToolBar = findViewById(R.id.MaternityDriveToolBar);
        setSupportActionBar(MaternityDriveToolBar);
        // Find Views By Id
        moptionsBackBtn = findViewById(R.id.optionsBackBtn);
        mCustomerEmail = findViewById(CustomerEmail);
        mbtn_reset_password = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        //Backbutton
        moptionsBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Password Reset Button
        mbtn_reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the user input text from the EditText View
                final String CustomerEmail = mCustomerEmail.getText().toString().trim();
                if (TextUtils.isEmpty(CustomerEmail)) {
                    Toast.makeText(getApplication(), R.string.registeredID, Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(CustomerEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, R.string.sentYou, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, R.string.resetFailded, Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });


    }
}
