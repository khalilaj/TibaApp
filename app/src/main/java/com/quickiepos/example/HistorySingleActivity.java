package com.quickiepos.example;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity   {
    private String consultId;

    private TextView consultLocation;
    private TextView consultDate;

    private TextView parentName;
    private TextView parentPhone;
    private ImageView parentImage;

    private TextView paediatricianName;
    private TextView paediatricianPhone;
    private ImageView paediatricianImage;

    private DatabaseReference historyRideInfoDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        consultId = getIntent().getExtras().getString("consultId");


        consultLocation = (TextView) findViewById(R.id.consultLocation);
        consultDate = (TextView) findViewById(R.id.consultDate);

        parentName = (TextView) findViewById(R.id.parentName);
        parentPhone = (TextView) findViewById(R.id.parentPhone);
        parentImage = (ImageView) findViewById(R.id.parentImage);

        paediatricianName = (TextView) findViewById(R.id.paediatricianName);
        paediatricianPhone = (TextView) findViewById(R.id.paediatricianPhone);
        paediatricianImage = (ImageView) findViewById(R.id.paediatricianImage);

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(consultId);

        getConsultInformation();



    }

    private void getConsultInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){

                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        if (map.get("parent")!= null) {
                            String parentId = map.get("parent").toString();
                            DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Parents").child(parentId);
                            mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        if(map.get("name") != null){
                                            String name = map.get("name").toString();
                                            parentName.setText(map.get("name").toString());
                                        }
                                        if(map.get("phone") != null){
                                            parentPhone.setText(map.get("phone").toString());
                                        }
                                        if(map.get("photoUrl") != null){
                                            Glide.with(getApplication()).load(map.get("photoUrl").toString()).into(parentImage);
                                        }
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }

                        if (map.get("paediatrician")!= null) {
                            String pedId = map.get("paediatrician").toString();
                            DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Paediatricians").child(pedId);
                            mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        if(map.get("name") != null){
                                            String name = map.get("name").toString();
                                            paediatricianName.setText(map.get("name").toString());
                                        }
                                        if(map.get("phone") != null){
                                            paediatricianPhone.setText(map.get("phone").toString());
                                        }
                                        if(map.get("photoUrl") != null){
                                            Glide.with(getApplication()).load(map.get("photoUrl").toString()).into(paediatricianImage);
                                        }
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }


                        if (child.getKey().equals("timestamp")){
                            consultDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("consultLocation")){
                            consultLocation.setText(child.getValue().toString());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HistorySingleActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

}