package com.quickiepos.example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

    private EditText login_email, login_password;
    private Button login_button;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Initialize Activity Variables
        mAuth = FirebaseAuth.getInstance();
        login_email =(EditText)findViewById(R.id.login_email);
        login_password =(EditText)findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_button);
        progressDialog = new ProgressDialog(this);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                        Intent intent = new Intent(PaediatricianLogInActivity.this,  PaediatricianMainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        //Set Listener to direct user to register
        FloatingActionButton reg = findViewById(R.id.registration_button);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = login_email.getText().toString();
                final String password = login_password.getText().toString();

                if (email.equals("") && password.equals("")){
                    Toast.makeText(PaediatricianLogInActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {

                    //Use the progressDialog so as to avoid user wait as Firebase Validates the user
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(PaediatricianLogInActivity.this, new OnCompleteListener<AuthResult>() {
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

                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Paediatricians").child(user_id);
                                current_user_db.setValue(map);

                                Toast.makeText(PaediatricianLogInActivity.this,"Successful Registration", Toast.LENGTH_SHORT).show();

                            }
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
                if (email.equals("") && password.equals("")){
                    Toast.makeText(PaediatricianLogInActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {

                    //Use the progressDialog so as to avoid user wait as Firebase Validates the user
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(PaediatricianLogInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();

                                Toast.makeText(PaediatricianLogInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else {
                                progressDialog.dismiss();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PaediatricianLogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(PaediatricianLogInActivity.this, UserOptionsActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }



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