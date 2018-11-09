package com.quickiepos.example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PaediatricianLogInActivity extends AppCompatActivity {

      /*----------------------------------------------------------------------
        |  Class: PaediatricianLogInActivity
        |
        |  Purpose: Enable the Paediatrician user to either sign in or sign up to Tiba App
        |
        |  Note: Firebase will be used for this application with the following key object(s) and methods
        |
        |        FirebaseAuth: Firebase object used to initialize the authentication of a user
        |        AuthStateListener: Firebase method used to listen for user authentication on the device
        |        createUserWithEmailAndPassword: Firebase method used to sign up a new user
        |        signInWithEmailAndPassword: Firebase method used to sign in a new user
        |
        |
        *-------------------------------------------------------------------*/

    //Declare Activity Variables
    private EditText login_email, login_password;
    private Button login_button;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the layout of the Activity
        setContentView(R.layout.activity_login);

        //Initialize Activity Variables
        mAuth = FirebaseAuth.getInstance();
        login_email =(EditText)findViewById(R.id.login_email);
        login_password =(EditText)findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_button);
        progressDialog = new ProgressDialog(this);

        //Initialize the AuthStateListener to check if a valid user has signed in and redirect to PaediatricianMainActivity
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(PaediatricianLogInActivity.this, PaediatricianMainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        //Set Listener on the floatingAction Bar to direct user to Sign up
        FloatingActionButton sign_up_button = findViewById(R.id.sign_up_button);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = login_email.getText().toString();
                final String password = login_password.getText().toString();

                if (email.equals("") || password.equals("")){
                    Toast.makeText(PaediatricianLogInActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {

                    //Use the progressDialog so as to avoid user wait as Firebase Validates the user
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    //Sign up the user using the Firebase method : createUserWithEmailAndPassword
                    mAuth.createUserWithEmailAndPassword(email, password).
                            //Using the Firebase method addOnCompleteListener to check if the task was successful
                                    addOnCompleteListener(PaediatricianLogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        progressDialog.dismiss();

                                        Toast.makeText(PaediatricianLogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        String user_id = mAuth.getCurrentUser().getUid();
                                        //Set the user details in a map data structure so as to send it to the Firebase DB
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("email", email.toString());
                                        map.put("name", email.toString());
                                        map.put("phone", "");
                                        map.put("type", "Paediatrician");
                                        map.put("photoUrl", "");

                                        //Create a Firebase database reference so as to set where exactly the map value will be stored in the database
                                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Paediatricians").child(user_id);
                                        current_user_db.setValue(map);

                                        Toast.makeText(PaediatricianLogInActivity.this,"Successful Registration", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }).
                            //Using the Firebase method addOnFailureListener to handle any exception when signing in
                            addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(PaediatricianLogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    });
                }
            }
        });

        //Set a listener to Log In user
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = login_email.getText().toString();
                final String password = login_password.getText().toString();

                //Validate the user input details. Make sure the user inputs both email and password
                if (email.equals("") || password.equals("")){
                    Toast.makeText(PaediatricianLogInActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {

                    //Use the progressDialog so as to avoid user wait as Firebase Validates the user
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    //Sign in the user using the Firebase method : signInWithEmailAndPassword
                    mAuth.signInWithEmailAndPassword(email, password).
                            //Using the Firebase method addOnCompleteListener to check if the task was successful
                                    addOnCompleteListener(PaediatricianLogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //Toast exception if the task was not successful
                                    if (!task.isSuccessful()) {
                                        progressDialog.dismiss();

                                        Toast.makeText(PaediatricianLogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else {
                                        progressDialog.dismiss();
                                    }
                                }
                            }).
                            //Using the Firebase method addOnFailureListener to handle any exception from signing in
                                    addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PaediatricianLogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }

            }
        });

        //Initialize action bar to enable the user to navigate back to the previous activity
        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    //Initialize action bar menu and set an onClick on the menu items using onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent_icon activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(PaediatricianLogInActivity.this, UserOptionsActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

      /*-----------------------------------------------------------------------
        |  Function(s) onStart, onStop
        |
        |  Purpose:  To set and remove the authStateListener when the activity is started.
        |             this is to redirect an already signed in user to automatically log in.
        |
        |  Note:
        |	  onStart : starts the authStateListener to listener to signed in users
        |	  onStop : stop the authStateListener from listening to signed in users
        |
        |
        *---------------------------------------------------------------------*/

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}