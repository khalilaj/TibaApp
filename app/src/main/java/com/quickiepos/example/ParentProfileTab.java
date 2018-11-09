package com.quickiepos.example;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class ParentProfileTab extends Fragment {

            /*-----------------------------------------------------------------------------
            |  Class: ParentProfileTab
            |
            |  Purpose: A tab that initializes the Profile fragment of the user to enable
            |            reading and editing of the user's details
            |
            |  Note: The following key methods will be used
            |          getUserInfo() : to get the user's information
            |          saveUserInformation() : to save the user's new details
            |
            |
            *---------------------------------------------------------------------------*/

    //Declare class variables
    private EditText user_nameField;
    private EditText user_phoneField;
    private ImageView user_profileImageField;
    private Button save_button;

    private FirebaseAuth mAuth;
    private DatabaseReference current_user_db;

    private String user_id;
    private String user_name;
    private String user_phone;
    private String user_email;
    private String user_profileImage;

    private ProgressDialog progressDialog;
    private Uri resultUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_registration, container, false);

        //Initialize fragment variables
        user_nameField = (EditText) view.findViewById(R.id.user_nameField);
        user_phoneField = (EditText) view.findViewById(R.id.user_phoneField);
        user_profileImageField = (ImageView) view.findViewById(R.id.profileImage);
        save_button = (Button) view.findViewById(R.id.confirm);
        progressDialog = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        //Create a database reference to get the logged in user details
        current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Parents").child(user_id);

        //Use the progressDialog so as to avoid user wait as Firebase loads
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getUserInfo();

        //Set an onClick listener on the image to enable the user to change profile details
        user_profileImageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                startActivityForResult(intent, 1);
            }
        });

        //The save button will change all the new details input by the user
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                saveUserInformation();
            }
        });


        return view;
    }

    /*-----------------------------------------------------------------------------
    |  Method: getUserInfo()
    |
    |  Purpose: A method used to get all the user's details from the Firebase database
    |
    |  Note: We use addValueEventListener() method to be able to get all the information from
    |        the database reference variable current_user_db
    |
    |
    *---------------------------------------------------------------------------*/

    private  void getUserInfo(){


        current_user_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    //Create a map data structure that will hold all the values from the database
                    // reference and place them on the specified EditTextFields.

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name")!= null){
                        user_name = map.get("name").toString();
                        user_nameField.setText(user_name);
                    }
                    if (map.get("phone")!= null){
                        user_phone = map.get("phone").toString();
                        user_phoneField.setText(user_phone);
                    }
                    if (map.get("email")!= null){
                        user_email = map.get("email").toString();
                    }
                    if (map.get("photoUrl") != null){
                        user_profileImage = map.get("photoUrl").toString();

                        Glide.with(getActivity()).load(user_profileImage).into(user_profileImageField);
                    }

                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Use details loaded!", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    /*-----------------------------------------------------------------------------
    |  Method: saveUserInformation()
    |
    |  Purpose: A method used to save the user's details to the Firebase database
    |
    |  Note: We use updateChildren() method to be able to update
    |        the database reference variable current_user_db
    |
    |
    *---------------------------------------------------------------------------*/

    private void saveUserInformation() {

        user_name = user_nameField.getText().toString();
        user_phone = user_phoneField.getText().toString();

        //Validate the user input details. Make sure the user inputs both user_phone and user_name
        if (user_name.equals("") || user_phone.equals("")){
            Toast.makeText(getActivity(), "Please enter all details", Toast.LENGTH_SHORT).show();
        }

        Map userInfo = new HashMap();
        userInfo.put("name", user_name);
        userInfo.put("email", user_email);
        userInfo.put("phone", user_phone);
        userInfo.put("type",  "Parent");


        current_user_db.updateChildren(userInfo);

        progressDialog.dismiss();
        Toast.makeText(getActivity(),"Successful changed user details", Toast.LENGTH_SHORT).show();


        //Checks to see if the user has uploaded a new image
        if (resultUri != null) {
            //Create a storage reference to the path you want to store the image
            final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("photoUrl").child(user_id);
            //Create an uploading task that will handle the upload
            UploadTask uploadTask = filepath.putFile(resultUri);

            //Upload the image
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    //Check if the task was successful and throw an exception if not
                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

                            current_user_db.updateChildren(newImage);

                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Successful changed user details", Toast.LENGTH_SHORT).show();

                            return;
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

            user_profileImageField.setImageURI(resultUri);

        }
    }
}