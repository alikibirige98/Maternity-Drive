package com.jammbcstore.maternitydrive;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.util.HashMap;
import java.util.List;

public class MidwifeCustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    GeoQuery geoQuery;
    // Global Variables
    private GoogleMap mMap;
    private Button mLogout, mRequestMidwifeBtn;
    private ImageButton mOptionsBtn;
    private LatLng pickUpLocation;
    private Boolean requestBol = false;
    private Marker pickUpMarker;
    //#Build Start# GetClosestDriver Function to process requests
    // Global Variables
    private int radius = 1;
    private Boolean MidwifeDriverFound = false;
    private String MidwifeDriverFoundID;
    //#Build Start# GetdriverLocation
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midwife_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Logout Setup and Ambulance Request Button
        mRequestMidwifeBtn = findViewById(R.id.RequestMidwifeBtn);
        mLogout = findViewById(R.id.MidwifeCustomerLogoutBtn);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(MidwifeCustomerMapActivity.this, CustomerLoginActivity.class);
                startActivity(signOutIntent);
                finish();
                return;
            }
        });
        // mOptionsBtn
        mOptionsBtn = findViewById(R.id.optionsBtn);
        mOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent OptionsIntent = new Intent(MidwifeCustomerMapActivity.this, OptionsActivity.class);
                startActivity(OptionsIntent);
                finish();
                return;
            }
        });
        // Request Midwife Button
        mRequestMidwifeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol) {
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    if (MidwifeDriverFoundID != null) {
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(MidwifeDriverFoundID);
                        driverRef.setValue(true);
                        MidwifeDriverFoundID = null;
                    }
                    MidwifeDriverFound = false;
                    radius = 1;
                    // Remove from database
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("MidwifeRequests");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    // Remove Pick Up Marker
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
                } else {
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("MidwifeRequests");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    // Creating a marker
                    pickUpLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick up here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickupfinaluse)));
                    // Change Button text
                    mRequestMidwifeBtn.setText(R.string.midwife);
                    // GetClosestDriver Function to process requests
                    getClosestDriver();
                }
            }
        });

    }

    private void getClosestDriver() {
        // #Grab driver location && the GeofireQuery to track drivers in the radius#
        DatabaseReference AmbulanceDriverLocation = FirebaseDatabase.getInstance().getReference().child("MidwifeDriversAvailable");
        GeoFire geoFire = new GeoFire(AmbulanceDriverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //#Runs if driver is found#
                if (!MidwifeDriverFound && requestBol) {
                    MidwifeDriverFound = true;
                    MidwifeDriverFoundID = key;
                }
                //Notice Driver About Request
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(MidwifeDriverFoundID);
                String CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap Map = new HashMap();
                Map.put("MidwifeRaidID", CustomerID);
                driverRef.updateChildren(Map);
                //Get driver Location on Customer's Map
                getDriverLocation();
                mRequestMidwifeBtn.setText(R.string.DriverLocation);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //#Runs if driver is not close, it moves to next radius area#
                if (!MidwifeDriverFound && requestBol) {
                    // move to next radius area
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }//#Build End# GetClosestDriver Function to process requests

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("MidwifeDriversWorking").child(MidwifeDriverFoundID).child("l");
        // Add value event listener to track location.
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequestMidwifeBtn.setText(R.string.DriverFound);
                    //Record tracks on the map for the lng and lat
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    // Add marker to the map
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    // Get Distance between driver and customer
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLatitude(pickUpLocation.longitude);
                    //Driver loc
                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLatitude(driverLatLng.longitude);
                    // Distance Equation and customer driver arrival notice
                    float distance = loc1.distanceTo(loc2);
                    if (distance < 100) {
                        mRequestMidwifeBtn.setText(R.string.DriverHere);
                    } else {
                        mRequestMidwifeBtn.setText(R.string.CancelRequest + R.string.DriverDistance + String.valueOf(distance));
                    }
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapimagemidwife)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //#Build End# GetdriverLocation
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
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
        mLastLocation = location;
        // #Get latitude and Longitude cordinates to trace location # >>> Google Maps
        // Pass in location variable and getLat or Long method function
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Move the camera alongside user's locations (Pass in the latlng variable) animate zoom to index smaller the value the closer
        // the map zoom to the ground
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

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
    protected void onStop() {
        super.onStop();
    }
}
