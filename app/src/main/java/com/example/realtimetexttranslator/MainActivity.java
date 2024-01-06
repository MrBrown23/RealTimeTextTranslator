package com.example.realtimetexttranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goScanner(View view){
        Intent newActivity = new Intent(MainActivity.this,ScannerActivity.class);
        startActivity(newActivity);
    }
}