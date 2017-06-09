package com.davidproject.practice2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        try {
            Thread.sleep(1500);
        }catch(Exception e){
            e.printStackTrace();
        }

        finish();
    }
}

