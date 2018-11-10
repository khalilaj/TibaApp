package com.quickiepos.example;


import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class ParentLocationTab extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap map;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    MapView mMapView;
    private Marker consultMarker;
    private LinearLayout paediatricianInfo;
    private ImageView paediatricianProfileImage;
    private TextView paediatricianName, paediatricianPhone;
    private Button parentRequest;
    private LatLng consultLocation;
    private LatLng destinationLatLng;
    private Boolean requestBol = false;
    private String destination;
    private static final String TAG = ParentLocationTab.class.getSimpleName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.parent_location_tab, container, false);

        destinationLatLng = new LatLng(0.0,0.0);
        parentRequest = (Button)  rootView.findViewById(R.id.request);
        paediatricianInfo = (LinearLayout) rootView.findViewById(R.id.paediatricianInfo);
        paediatricianName = (TextView) rootView.findViewById(R.id.paediatricianName);
        paediatricianPhone = (TextView) rootView.findViewById(R.id.paediatricianPhone);
        paediatricianProfileImage = rootView.findViewById(R.id.paediatricianProfileImage);


        parentRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBol){
                    endConsult();
                }else{
                    requestBol = true;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("parentRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    consultLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    consultMarker = map.addMarker(new MarkerOptions().position(consultLocation).title("Consult Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickup_marker)));

                    parentRequest.setText("Getting your paediatrician ...");


                    getClosestPaediatrician();
                }
            }
        });

        mMapView = (MapView) rootView.findViewById(R.id.map1);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }

    private int radius = 1;
    private Boolean paediatricianFound = false;
    private String paediatricianFoundID;

    GeoQuery geoQuery;
    private void getClosestPaediatrician(){
        DatabaseReference paediatricianLocation = FirebaseDatabase.getInstance().getReference().child("paediatriciansAvailable");

        GeoFire geoFire = new GeoFire(paediatricianLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(consultLocation.latitude, consultLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!paediatricianFound && requestBol){
                    paediatricianFound = true;
                    paediatricianFoundID = key;

                    DatabaseReference paediatricianRef = FirebaseDatabase.getInstance().getReference().child("Users").child("paediatricians").child(paediatricianFoundID).child("parentRequest");
                    String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("parentRideId", parentId);
                    map.put("destination", destination);
                    paediatricianRef.updateChildren(map);

                    getPaediatricianLocation();
                    getPaediatricianInfo();
                    getHasRideEnded();
                    parentRequest.setText("Looking for paediatrician Location....");


                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!paediatricianFound)
                {
                    radius++;
                    getClosestPaediatrician();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker paediatricianMarker;
    private DatabaseReference paediatricianLocationRef;
    private ValueEventListener paediatricianLocationRefListener;

    private void getPaediatricianLocation(){
        paediatricianLocationRef = FirebaseDatabase.getInstance().getReference().child("paediatriciansWorking").child(paediatricianFoundID).child("l");
        paediatricianLocationRefListener = paediatricianLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng paediatricianLatLng = new LatLng(locationLat,locationLng);
                    if(paediatricianMarker != null){
                        paediatricianMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(consultLocation.latitude);
                    loc1.setLongitude(consultLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(paediatricianLatLng.latitude);
                    loc2.setLongitude(paediatricianLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        parentRequest.setText("paediatrician's Here");
                    }else{


                        parentRequest.setText("paediatrician Found: " + String.valueOf(distance));


                    }



                    paediatricianMarker = ParentLocationTab.this.map.addMarker(new MarkerOptions().position(paediatricianLatLng).title("your paediatrician").icon(BitmapDescriptorFactory.fromResource(R.drawable.doctor_icon)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getPaediatricianInfo(){
        paediatricianInfo.setVisibility(View.VISIBLE);

        DatabaseReference paediatricianDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("paediatricians").child(paediatricianFoundID);
        paediatricianDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name")!= null){
                        paediatricianName.setText(map.get("name").toString());

                    }
                    if (map.get("phone")!= null){

                        paediatricianPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("photoUrl") != null){


                        Glide.with(getActivity()).load(map.get("profileImageUrl").toString()).into(paediatricianProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private DatabaseReference consultHasEndedRef;
    private ValueEventListener paediatricianHasEndedRefListener;
    private void getHasRideEnded(){
        consultHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("paediatricians").child(paediatricianFoundID).child("parentRequest").child("parentConsultId");
        paediatricianHasEndedRefListener = consultHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{

                    endConsult();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private  void endConsult(){

        requestBol = false;
        geoQuery.removeAllListeners();
        paediatricianLocationRef.removeEventListener(paediatricianLocationRefListener);
        consultHasEndedRef.removeEventListener(paediatricianHasEndedRefListener);

        if (paediatricianFoundID != null){
            DatabaseReference paediatricianRef = FirebaseDatabase.getInstance().getReference().child("Users").child("paediatricians").child(paediatricianFoundID).child("parentRequest");
            paediatricianRef.removeValue();
            paediatricianFoundID = null;
        }

        paediatricianFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("parentRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(consultMarker != null){
            consultMarker.remove();
        }
        parentRequest.setText("Call for help ");
        paediatricianInfo.setVisibility(View.GONE);
        paediatricianPhone.setText("");
        paediatricianName.setText("");
        paediatricianProfileImage.setImageResource(R.mipmap.user_image);


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getActivity()!=null){
            lastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }



    @Override
    public void onStop() {
        super.onStop();
    }

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
