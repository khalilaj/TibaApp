package com.quickiepos.example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserOptionsActivity extends AppCompatActivity {


      /*----------------------------------------------------------------------
        |  Class: UserOptionsActivity
        |
        |  Purpose: Give the two users options to go to their specific roles Activity
        |           Paediatrician or Parent
        |
        |  Note: Paediatrician will be directed to PaediatricianLogInActivity
        |        Parent will be directed to ParentLogInActivity
        |
        |
        *-------------------------------------------------------------------*/

    //Declare Activity Variables
    Button patient, paediatrician;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the layout of the Activity
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
                //Start the Activity
                startActivity(intent);
                finish();
            }
        });

    }
}