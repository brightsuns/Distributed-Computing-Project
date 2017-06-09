package com.davidproject.practice2;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void normalUse(View v){
        Intent normal = new Intent(this, SecondActivity.class);
        startActivity(normal);
        finish();
    }

    public void expertUse(View v){
        Intent expert = new Intent(this, VerifyActivity.class);
        startActivity(expert);
        finish();
    }
}
