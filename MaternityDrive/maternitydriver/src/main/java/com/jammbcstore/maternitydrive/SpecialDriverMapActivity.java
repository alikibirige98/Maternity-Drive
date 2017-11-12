package com.jammbcstore.maternitydrive;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import static com.jammbcstore.maternitydrive.R.id.map;

public class SpecialDriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    // Map Use Permission Request
    final int LOCATION_REQUEST_CODE = 1;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    // #Build Start# get Assigned Customer Pick Up Location
    // Globals
    Marker pickupMaker;
    private GoogleMap mMap;
    private Button mLogout;
    private String CustomerId = "";
    private LinearLayout mCustomerInfo;
    private ImageView mCustomerProfileImage;
    private TextView mCustomerName, mCustomerPhoneNumber, mCustomerDestination;
    private DatabaseReference assignedCustomerPickUpRef;
    private ValueEventListener assignedCustomerPickUpRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        // Permission Ask
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SpecialDriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
        //Get Customer Info to driver Idz
        mCustomerInfo = findViewById(R.id.CustomerInfo);
        mCustomerProfileImage = findViewById(R.id.CustomerProfileImage);
        mCustomerName = findViewById(R.id.CustomerName);
        mCustomerPhoneNumber = findViewById(R.id.CustomerPhoneNumber);
        mCustomerDestination = findViewById(R.id.CustomerDestination);
        // Get Assigned Customer for the driver.
        getAssignCustomer();
    }

    // #Build Start# get assignedCustomer method.
    private void getAssignCustomer() {
        String DriverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Special").child(DriverID).child("CustomerRequest").child("SpecialRaidID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check for Customer Ride Request towards a driver.
                if (dataSnapshot.exists()) {
                    CustomerId = dataSnapshot.getValue().toString();
                    //Get assigned customer pick up location
                    getAssignCustomerPickUpLocation();
                    getAssignCustomerDestination();
                    getAssignCustomerInfo();
                } else {
                    //if the driver has been cancelled of dataSnapshot.exits() returns false
                    CustomerId = "";
                    //Remove markers
                    if (pickupMaker != null) {
                        pickupMaker.remove();
                    }
                    //Remove from DataBase
                    if (assignedCustomerPickUpRefListener != null) {
                        assignedCustomerPickUpRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }
                    mCustomerInfo.setVisibility(View.VISIBLE);
                    mCustomerName.setText("");
                    mCustomerPhoneNumber.setText("");
                    mCustomerDestination.setText(R.string.DefaultDestination);
                    mCustomerProfileImage.setImageResource(R.drawable.ic_default_user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }// #Build End# get assignedCustomer method.
    // #Build End# get Assign Customer Pick Up Location

    // getAssignCustomerPickUpLocation()
    private void getAssignCustomerPickUpLocation() {
        assignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("SpecialRequests").child(CustomerId).child("l");
        assignedCustomerPickUpRefListener = assignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check for Customer Ride Request towards a driver.
                if (dataSnapshot.exists() && !CustomerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    // Add marker to the map
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    pickupMaker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Pick up").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    // #Build Start# getAssignCustomerDestination().
    private void getAssignCustomerDestination() {
        String DriverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Special").child(DriverID).child("CustomerRequest").child("Destination");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check for Customer Ride Request towards a driver.
                if (dataSnapshot.exists()) {
                    String Destination = dataSnapshot.getValue().toString();
                    // User entered Destination
                    mCustomerDestination.setText(R.string.Destination + Destination);

                } else {
                    // User Has not entered Destination
                    mCustomerDestination.setText(R.string.DefaultDestination);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }// #Build End# getAssignCustomerDestination().

    //Get user info
    private void getAssignCustomerInfo() {
        // CustomerDataBase
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(CustomerId);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    // Customer Name
                    if (map.get("Name") != null) {
                        mCustomerName.setText(map.get("Name").toString());
                    }
                    // Customer Phone Number
                    if (map.get("Phone") != null) {
                        mCustomerPhoneNumber.setText(map.get("Phone").toString());
                    }
                    // Profile Image
                    if (map.get("profileImageUrI") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrI").toString()).into(mCustomerProfileImage);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SpecialDriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    // #buildGoogleApiClient Function or Method#
    protected synchronized void buildGoogleApiClient() {
        // Setting up the Api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Allows us to use the APi
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            mLastLocation = location;
            // #Get latitude and Longitude cordinates to trace location # >>> Google Maps
            // Pass in location variable and getLat or Long method function
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // Move the camera alongside user's locations (Pass in the latlng variable) animate zoom to index smaller the value the closer
            // the map zoom to the ground
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            //#Start# Save Updated Location using Geofire to fire baseDatabase.
            // User ID String and Database Variable  for controll in our target ref= all ambulance drivers Available
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("SpecialDriversAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("SpecialDriversWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);
            // #Start# GeoFire
            switch (CustomerId) {
                case "":
                    // if the driver is working we remove him from available and opposite
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }
            //#End# Save Updated Location using Geofire to fire baseDatabase.
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Location and update to database second by second.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        // Set priority Accuracy if Priority= High Accuracy it takes alot of phone Battery
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //Pass in the variables we created GoogleApiClient, LastLocation and LocationRequest
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(map);
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.grant_permission, Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }
}
