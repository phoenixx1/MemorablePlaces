package com.nstg.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location") ;
            }
        }
    }

    public void centerMapOnLocation(Location location, String title) {
        LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
//        if(title != "Your Location"){
        mMap.addMarker(new MarkerOptions().position(user).title(title));
//    }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user,10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();

        if(intent.getIntExtra("PlaceNumber",0) == 0){

            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                centerMapOnLocation(lastKnownLocation,"Your Location") ;

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);

            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("PlaceNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("PlaceNumber",0)).longitude);

            centerMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("PlaceNumber",0)));
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
        String address = "";
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddress !=null){
                if(listAddress.get(0).getThoroughfare() != null){
                    if(listAddress.get(0).getSubThoroughfare() != null){
                        address = listAddress.get(0).getSubThoroughfare()+" ";
                    }
                    address = listAddress.get(0).getThoroughfare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(address == ""){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            address = simpleDateFormat.format(Calendar.getInstance().getTime());
        }


        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.nstg.memorableplaces", Context.MODE_PRIVATE);

        try {

            ArrayList<String> latitude = new ArrayList<>();
            ArrayList<String> longitude = new ArrayList<>();


            for(LatLng coordinates : MainActivity.locations){
                latitude.add(Double.toString(coordinates.latitude));
                longitude.add(Double.toString(coordinates.longitude));

            }
            sharedPreferences.edit().putString("latitude",ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("longitude",ObjectSerializer.serialize(longitude)).apply();
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MainActivity.arrayAdapter.notifyDataSetChanged();

    }
}
