package com.quickiepos.example;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

public class ParentLocationTab2 extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    /*-----------------------------------------------------------------------------
    |  Class: ParentLocationTab
    |
    |  Purpose: A tab that will enable the parent to request for help and get assigned the
    |           a paediatrician closest to the Parent location
    |
    |  Note: GeoFire will be used to get the location and update it in the database
    |
    |
    *---------------------------------------------------------------------------*/

    //Declare class variables
    MapView mMapView;

   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize fragment view
        final View rootView = inflater.inflate(R.layout.parent_location_tab, container, false);


        mMapView = (MapView) rootView.findViewById(R.id.map1);
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
      |	  onMapReady : 
      |	  onConnected : When the map is called and everything is ready to start working.
      |	  onConnectionSuspended :
      |	  onConnectionFailed :
      |	  onLocationChanged :
      |
      *-------------------------------------------------------------------------------------------*/


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Create a request to get the user's location from second to second
        
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        

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
