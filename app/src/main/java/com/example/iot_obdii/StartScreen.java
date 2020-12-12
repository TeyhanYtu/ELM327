package com.example.iot_obdii;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
    }


    public void goGraphFromStart(View view) {
        Intent intent = new Intent(StartScreen.this, GraphScreen.class);
        intent.putExtra("type", "speed");
        startActivity(intent);
    }


    public void goConsoleFromStart(View view) {
        Intent intent = new Intent(StartScreen.this, MainActivity.class);
        startActivity(intent);
    }

    public void connectWifi(View view){
        WifiManager wifiManager;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Wifi wifi = new Wifi(wifiManager);
        wifi.connectWifi();
    }
}