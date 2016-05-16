package com.badlogic.invaders.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, BLEDeviceScanActivity.class);
        startActivity(intent);
        finish(); //Close once we end the splash screen
    }

}
