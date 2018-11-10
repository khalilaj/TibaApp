package com.quickiepos.example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //Declare Activity Variables
    private Button login_button;
    private EditText login_email, login_password;
    private ProgressDialog progressDialog;
    private String type;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get the String value from the intent
        Intent intent = getIntent();
        //Create a new bundle to get the passed values
        Bundle bundle = intent.getExtras();
        //Look for the String v alue that has been passed
        type = bundle.getString("type");


        //Initialize Activity Variables
        mAuth = FirebaseAuth.getInstance();
        login_email =(EditText)findViewById(R.id.login_email);
        login_password =(EditText)findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_button);
        progressDialog = new ProgressDialog(this);

        //Set Listener to check if a valid user has already logg0000000ed in
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Create a Firebase user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //Make sure there is a logged in user so as to be able move to the next activity
                if (user!=null){

                    /**Check whether the type of user
                     * if user_type == Parent sent to ParentMainActivity
                     * if user_type == Paediatrician sent to PaediatricianMainActivity
                     */

                    final DatabaseReference[] databaseReference = {FirebaseDatabase.getInstance().getReference()};
                    databaseReference[0].child("Users").child(type).child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if ( dataSnapshot.child("type").getValue().toString().equals("Parent")){
                                Intent intent = new Intent(MainActivity.this,ParentMainActivity.class);
                                startActivity(intent);
                                finish();
                            }else if(dataSnapshot.child("type").getValue().toString().equals("Paediatrician")){
                                Intent intent = new Intent(MainActivity.this,PaediatricianMainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        };


        //Set Listener to Login user
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get the inputs from the edit text
                final String email = login_email.getText().toString();
                final String password = login_password.getText().toString();

                //Validate the user input details. Make sure the user inputs both email and password
                if (email.equals("") && password.equals("")){
                    Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else {

                    //Use the progressDialog so as to avoid user wait as Firebase Validates the user
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    //Use mAuth to log in a signIn user
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();

                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();

                                final DatabaseReference[] databaseReference = {FirebaseDatabase.getInstance().getReference()};
                                databaseReference[0].child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if ( dataSnapshot.child("type").getValue().toString().equals("Parent")){
                                            Intent intent = new Intent(MainActivity.this,ParentMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else if(dataSnapshot.child("type").getValue().toString().equals("Paediatrician")){
                                            Intent intent = new Intent(MainActivity.this,PaediatricianMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        //Set Listener to direct user to the registrationOptions
        FloatingActionButton reg = findViewById(R.id.registration_button);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserOptionsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    /** Whenever the activity is started we must call the AuthStateListener to automatically sign in the user  */
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }


    /** Whenever the activity is stopped we must remove the AuthStateListener */
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }


}
