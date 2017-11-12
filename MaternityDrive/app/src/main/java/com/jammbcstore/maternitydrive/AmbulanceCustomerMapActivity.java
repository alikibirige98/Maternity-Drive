package com.jammbcstore.maternitydrive;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import java.util.Map;

import static com.jammbcstore.maternitydrive.R.id.map;

public class AmbulanceCustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    final int LOCATION_REQUEST_CODE = 1;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    GeoQuery geoQuery;
    // Global Variables
    private GoogleMap mMap;
    private Button mRequestAmbulanceBtn;
    private ImageButton mOptionsBtn;
    private LatLng pickUpLocation;
    private Boolean requestBol = false;
    private Marker pickUpMarker;
    private String Destination;
    // Get driver Info
    private LinearLayout mDriverInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhoneNumber, mDriverPlateNumber;
    //#Build Start# GetClosestDriver Function to process requests
    // Global Variables
    private int radius = 1;
    private Boolean AmbulanceDriverFound = false;
    private String AmbulanceDriverFoundID;
    //#Build Start# Get driver Location
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AmbulanceCustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        //Logout Setup and Ambulance Request Button
        mRequestAmbulanceBtn = findViewById(R.id.RequestAmbulanceBtn);
        //Get Customer Info to driver Idz
        mDriverInfo = findViewById(R.id.DriverInfo);
        mDriverProfileImage = findViewById(R.id.DriverProfileImage);
        mDriverName = findViewById(R.id.DriverName);
        mDriverPhoneNumber = findViewById(R.id.DriverPhoneNumber);
        mDriverPlateNumber = findViewById(R.id.DriverPlateNumber);

        // mOptionsBtn
        mOptionsBtn = findViewById(R.id.optionsBtn);
        mOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent OptionsIntent = new Intent(AmbulanceCustomerMapActivity.this, OptionsActivity.class);
                startActivity(OptionsIntent);
                return;
            }
        });

        // Request Ambulance
        mRequestAmbulanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol) {
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    if (AmbulanceDriverFoundID != null) {
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(AmbulanceDriverFoundID).child("CustomerRequest");
                        // Question
                        driverRef.removeValue();
                        AmbulanceDriverFoundID = null;
                    }
                    AmbulanceDriverFound = false;
                    radius = 1;
                    // Remove from database
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AmbulanceRequests");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    // Remove Pick Up Marker
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    mRequestAmbulanceBtn.setText(R.string.RequestAmbulance);
                    // Remove Driver Info
                    mDriverInfo.setVisibility(View.GONE);
                    mDriverName.setText("");
                    mDriverPhoneNumber.setText("");
                    mDriverPlateNumber.setText("");
                    mDriverProfileImage.setImageResource(R.drawable.ic_default_user);

                } else {
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AmbulanceRequests");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    // Creating a marker
                    pickUpLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick up here").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickupfinaluse)));
                    // Change Button text
                    mRequestAmbulanceBtn.setText(R.string.Get);
                    // GetClosestDriver Function to process requests
                    getClosestDriver();
                }
            }
        });

        //PlaceAutoComplete
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                Destination = place.getName().toString();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });

    }

    private void getClosestDriver() {
        // #Grab driver location && the GeofireQuery to track drivers in the radius#
        DatabaseReference AmbulanceDriverLocation = FirebaseDatabase.getInstance().getReference().child("AmbulanceDriversAvailable");
        GeoFire geoFire = new GeoFire(AmbulanceDriverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //#Runs if driver is found#
                if (!AmbulanceDriverFound && requestBol) {
                    AmbulanceDriverFound = true;
                    AmbulanceDriverFoundID = key;
                }
                //Notice Driver About Request
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(AmbulanceDriverFoundID).child("CustomerRequest");
                String CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap Map = new HashMap();
                Map.put("AmbulanceRaidID", CustomerID);
                Map.put("Destination", Destination);
                driverRef.updateChildren(Map);
                //Get driver Location on Customer's Map
                getDriverLocation();
                getDriverInfo();
                mRequestAmbulanceBtn.setText(R.string.DriverLocation);
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
                if (!AmbulanceDriverFound && requestBol) {
                    // move to next radius area
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    } //#Build End# GetClosestDriver Function to process requests

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("AmbulanceDriversWorking").child(AmbulanceDriverFoundID).child("l");
        // Add value event listener to track location.
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequestAmbulanceBtn.setText(R.string.DriverFound);
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
                        mRequestAmbulanceBtn.setText(R.string.DriverHere);
                    } else {
                        mRequestAmbulanceBtn.setText(R.string.CancelRequest + R.string.DriverDistance + String.valueOf(distance));
                    }
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapimageambulance)));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //Get Driver info
    private void getDriverInfo() {
        // CustomerDataBase
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(AmbulanceDriverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    // Driver Name
                    if (map.get("Name") != null) {
                        mDriverName.setText(map.get("Name").toString());
                    }
                    // Driver Phone Number
                    if (map.get("Phone") != null) {
                        mDriverPhoneNumber.setText(map.get("Phone").toString());
                    }
                    // Driver Plate Number
                    if (map.get("PlateNumber") != null) {
                        mDriverPlateNumber.setText(map.get("PlateNumber").toString());
                    }
                    // Profile Image
                    if (map.get("profileImageUrI") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrI").toString()).into(mDriverProfileImage);
                    }

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
            ActivityCompat.requestPermissions(AmbulanceCustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Location and update to database second by second.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        // Set priority Accuracy if Priority= High Accuracy it takes alot of phone Battery
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Pass in the variables we created GoogleApiClient, LastLocation and LocationRequest
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AmbulanceCustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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

    @Override
    protected void onStop() {
        super.onStop();
    }


}
