package com.jammbcstore.maternitydrive;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    // Globals
    private EditText mNameField, mPhoneField, mPlateNumberField;
    private Button mConfrim, mBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID;
    private String mName, mPhone, mprofileImageUrI, mPlateNumber;
    private ImageView mProfileImage;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Find views By Id
        mNameField = findViewById(R.id.AmbulanceDriverName);
        mPhoneField = findViewById(R.id.AmbulanceDriverPhone);
        mPlateNumberField = findViewById(R.id.AmbulanceDriverPlateNumber);
        mProfileImage = findViewById(R.id.ProfileImage);
        mConfrim = findViewById(R.id.AmbulanceDriverProfileConfirmBtn);
        mBack = findViewById(R.id.AmbulanceDriverProfileBackBtn);
        // Get Database
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        getUserInfo();
        // Profile Image
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get gallery Intent
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                // Request code == 1
                startActivityForResult(intent, 1);
            }
        });
        // Set onclick Listener
        mConfrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                return;

            }
        });
        // Onclick listener
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });


    }

    //Get user info
    private void getUserInfo() {
        // CustomerDataBase
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    // Ambulance Driver Name
                    if (map.get("Name") != null) {
                        mName = map.get("Name").toString();
                        mNameField.setText(mName);
                    }
                    // Ambulance Driver Phone Number
                    if (map.get("Phone") != null) {
                        mPhone = map.get("Phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    // Ambulance Driver Plate Number
                    if (map.get("PlateNumber") != null) {
                        mPlateNumber = map.get("PlateNumber").toString();
                        mPlateNumberField.setText(mPlateNumber);
                    }
                    // Profile Image
                    if (map.get("profileImageUrI") != null) {
                        mprofileImageUrI = map.get("profileImageUrI").toString();
                        Glide.with(getApplication()).load(mprofileImageUrI).into(mProfileImage);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // Save user Info
    private void saveUserInformation() {
        // Name and Phone
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mPlateNumber = mPlateNumberField.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("Name", mName);
        userInfo.put("Phone", mPhone);
        userInfo.put("PlateNumber", mPlateNumber);
        // Save to the Database
        mDriverDatabase.updateChildren(userInfo);

        // ProfileImage, Get image Uri fromits location.
        if (resultUri != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Compress Image, change it to array using byte[] data= baos.toByteArray a way in which imgz saved to firbage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            // save to FireBase and check it's success n if y add it to the user respectively.
            UploadTask uploadTask = filePath.putBytes(data);
            // On Failure
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            // On Success
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //save Image
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrI", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Get uri passed in by data.getData() and make it profile image using glide library setImageUri();
            final Uri ImageUri = data.getData();
            resultUri = ImageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }

}
