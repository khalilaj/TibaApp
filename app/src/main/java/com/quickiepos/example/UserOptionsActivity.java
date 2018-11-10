package com.quickiepos.example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserOptionsActivity extends AppCompatActivity {

    //Declare Activity Variables
    Button patient, paediatrician;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_options);


        //Initialize Activity Variables
        patient = (Button) findViewById(R.id.parent_button);
        paediatrician = (Button) findViewById(R.id.paediatrician_button);

        //patient Button
        patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create new intent to send the user to the Registration Activity
                Intent intent = new Intent(UserOptionsActivity.this, ParentLogInActivity.class);
                //Create a bundle object to pass the type of user to the registration activity
                Bundle bundle = new Bundle();
                //Pass the type of user as a string to be able to differentiate the type of user
                bundle.putString("type","patient");
                //Put the bundle in the intent
                intent.putExtras(bundle);
                //Start the Activity
                startActivity(intent);
                finish();
            }
        });

        //Paediatrician Button
        paediatrician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create new intent to send the user to the Registration Activity
                    Intent intent = new Intent(UserOptionsActivity.this, PaediatricianLogInActivity.class);
                //Create a bundle object to pass the type of user to the registration activity
                Bundle bundle = new Bundle();
                //Pass the type of user as a string to be able to differentiate the type of user
                bundle.putString("type","paediatrician");
                //Put the bundle in the intent
                intent.putExtras(bundle);
                //Start the Activity
                startActivity(intent);
                finish();
            }
        });

    }
}