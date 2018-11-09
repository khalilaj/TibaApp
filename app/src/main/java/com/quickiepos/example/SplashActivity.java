package com.quickiepos.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {


      /*------------------------------------------------------------------
        |  Class: SplashActivity
        |
        |  Purpose: Initialize the splash screen as the application is loading during start up
        |
        |  Note:The splash screen is loaded to take advantage of the
        |       time it take the android phone to load the application
        |
        |
        *-------------------------------------------------------------------*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //After the splash screen has been loaded the user will be directed to the UserOptionsActivity class
        Intent intent = new Intent(this, UserOptionsActivity.class);
        startActivity(intent);
        finish();
    }
}
