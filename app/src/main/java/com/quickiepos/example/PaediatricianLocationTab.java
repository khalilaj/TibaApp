package com.quickiepos.example;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

public class PaediatricianLocationTab extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    //Declare class variables
    MapView mMapView;

    private GoogleMap user_map;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize fragment view
        final View rootView = inflater.inflate(R.layout.parent_location_tab, container, false);


        mMapView = (MapView) rootView.findViewById(R.id.parent_map);
        mMapView.onCreate(savedInstanceState);
        // needed to get the map to display immediately
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }


    /*-----------------------------------------------------------------------------------------
      |  Function(s) onMapReady, onConnected, onConnectionSuspended, onConnectionSuspended
      |
      |  Purpose:  Map functions to periodically update the user's location
      |
      |  Note:
      |	  onMapReady : Get the map ready for the fragment
      |	  onConnected : When the map is called and everything is ready to start working.
      |	  onConnectionSuspended : When the location connection has been paused
      |	  onConnectionFailed : When the location connection has failed
      |	  onLocationChanged : Periodically updates the map when a location has been changed
      |
      *-------------------------------------------------------------------------------------------*/


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Create a request to get the user's location from second to second
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialize the map
        user_map = googleMap;

        //Permissions check
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        user_map.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient (){
        //Build a connection with the API client
       googleApiClient = new GoogleApiClient.Builder(getActivity())
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this).addApi(LocationServices.API)
               .build();
       googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Called every second to change the users location
        lastLocation = location;
        //Get the coordinates
        LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        //Move the camera based on the user's movement
        user_map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        user_map.animateCamera(CameraUpdateFactory.zoomTo(12));

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }


      /*------------------------------------------------------------------
        |  Function(s) onResume, onPause, onDestroy, onLowMemory
        |
        |  Purpose:  Adapt the mapView with changes that are going on with the fragment using the main
        |            fragment functions.
        |
        |  Note:
        |	  onResume : When the fragment has been resumed resume MapView
        |	  onPause : When the fragment has been pause pause the MapView
        |	  onDestroy : When the fragment has been destroyed also destroy the MapView
        |	  onLowMemory : When the phone is on low memory change the MapView to be more battery conscious
        |
        |
        *-------------------------------------------------------------------*/


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
