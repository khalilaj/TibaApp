package com.quickiepos.example;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickiepos.example.historyRecyclerView.HistoryAdapter;
import com.quickiepos.example.historyRecyclerView.HistoryObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class ParentHistoryTab extends Fragment {

    private String userId;

    private RecyclerView historyRecyclerView;
    private ProgressDialog progressDialog;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.parent_history_tab, container, false);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setNestedScrollingEnabled(false);

        historyRecyclerView.setHasFixedSize(true);
        historyLayoutManager = new LinearLayoutManager(getActivity());

        historyRecyclerView.setLayoutManager(historyLayoutManager);
        historyAdapter = new HistoryAdapter(getDataSetHistory(), getActivity());
        historyRecyclerView.setAdapter(historyAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressDialog = new ProgressDialog(getActivity());
        //Use the progressDialog so as to avoid user wait as Firebase loads
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getUserHistoryIds();

        return view;
    }
    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Parents").child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Use details loaded!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;

                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }

                    HistoryObject obj = new HistoryObject(rideId, getDate(timestamp));
                    resultsHistory.add(obj);
                    historyAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Use details loaded!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();

    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }

}