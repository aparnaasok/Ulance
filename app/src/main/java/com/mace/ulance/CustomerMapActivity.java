package com.mace.ulance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap nMap;
    GoogleApiClient ngoogleApiClient;
    Location nlastlocation;
    LocationRequest nlocationRequest;
    private Button nlogout;
    private Button nrequest,nSettings;
    private LatLng pickupLocation;
    private Boolean requestBol=false;
    private Marker pickupMarker;
    LocationManager locationManager;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            {ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);}
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.


                mapFragment.getMapAsync(this);







        nlogout=(Button) findViewById(R.id.logout);
        nrequest=(Button)findViewById(R.id.request);
        nSettings=(Button) findViewById(R.id.settings );
        nlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent= new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        nrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(requestBol){
                    requestBol=false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    if(driverFoundID!=null){
                        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child(driverFoundID);
                        driverRef.setValue(true);
                        driverFoundID=null;
                    }
                    driverFound=false;
                    radius=1;

                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire= new GeoFire(ref);
                    geoFire.removeLocation(userId);
                    if(pickupMarker!=null){
                        pickupMarker.remove();
                    }
                    nrequest.setText("Call Ambulance");

                }
                else{
                    requestBol=true;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire= new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(nlastlocation.getLatitude(), nlastlocation.getLongitude()));



                    pickupLocation=new LatLng(nlastlocation.getLatitude(), nlastlocation.getLongitude());



                    pickupMarker=nMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.man)));
                    nrequest.setText("Getting your Ambulance...");
                    getClosestDriver();
                }



            }
        });
          /* nSettings.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                Intent intent=new Intent(CustomerMapActivity.this,CustomerSettingsActivity.class);
                startActivity(intent);
                return;
               }
           });*/
    }
    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
           // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, (android.location.LocationListener) this);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private int radius=1;
    private Boolean driverFound=false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire=new GeoFire(driverLocation);
        geoQuery= geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol) {
                    driverFound=true;
                    driverFoundID=key;
                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("customerRideId",customerId);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    nrequest.setText("looking fot driver location");


                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                if(!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }


            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker nDriverMarker;
    private  DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol){
                    List<Object>map=(List<Object>) dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    nrequest.setText("Driver found");
                    if (map.get(0)!=null){
                        locationLat= Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(0)!=null){
                        locationLng= Double.parseDouble(map.get(1).toString());

                    }
                    LatLng driverLatLng=new LatLng(locationLat,locationLng);
                    if (nDriverMarker!=null){
                        nDriverMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2=new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance =loc1.distanceTo(loc2);
                    if(distance<100)
                        nrequest.setText("Driver is here");
                    else
                        nrequest.setText("Driver Found: "+String.valueOf(distance));


                    nDriverMarker=nMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ambulance)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {  ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);}
        buildGoogleApiClient();
        nMap.setMyLocationEnabled(true);
    }
    protected synchronized  void buildGoogleApiClient() {
        ngoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        ngoogleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        nlastlocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        nMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        nMap.animateCamera(CameraUpdateFactory.zoomTo(11));



    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        nlocationRequest=new LocationRequest();
        nlocationRequest.setInterval(1000);
        nlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            return;

        LocationServices.FusedLocationApi.requestLocationUpdates(ngoogleApiClient, nlocationRequest, this);



    }
    @Override
    public void onConnectionSuspended(int i) {


    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult ConnectionResult) {

    }
    final int LOCATION_REQUEST_CODE=1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please provide the permission",Toast.LENGTH_LONG).show();
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