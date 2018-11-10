package com.quickiepos.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PaediatricianProfileTab extends Fragment {
    private EditText mNameField;
    private EditText mPhoneField;
    private Button mConfirm;


    private ImageView mProfileImage;


    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userId;
    private String mName;
    private String mPhone;
    private String mProfileImageUrl;

    private Uri resultUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_parent_registration, container, false);

        mNameField = (EditText) view.findViewById(R.id.name);
        mPhoneField = (EditText) view.findViewById(R.id.phone);

        mProfileImage = (ImageView) view.findViewById(R.id.profileImage);

        mConfirm = (Button) view.findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Paediatricians").child(userId);


        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });


        return view;
    }
    private  void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name")!= null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if (map.get("phone")!= null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if (map.get("profileImageUrl") != null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();

                        Glide.with(getActivity()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {

        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("type",  "Paediatrician");


        mCustomerDatabase.updateChildren(userInfo);

        if (resultUri != null) {
            final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("photoUrl").child(userId);

            UploadTask uploadTask = filepath.putFile(resultUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        if (downloadUrl != null) {
                            Map newImage = new HashMap();
                            newImage.put("photoUrl", downloadUrl.toString());

                            mCustomerDatabase.updateChildren(newImage);

                            return;
                        } else {
                        }
                    }
                }

            });
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK){

            final Uri imageUri = data.getData();
            resultUri = imageUri;

            mProfileImage.setImageURI(resultUri);


        }
    }
}