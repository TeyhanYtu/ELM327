package com.example.iot_obdii;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    File file;
    Calendar calendar;
    TextView speedText,voltText, fuelStatusText, coolantTempText, dateTXT, rangeTXT,fuelRatetXT;
    Integer curCoolant = 0 ,curSpeed = 0, curRPM = 0;
    Double curVolt = 0.0, curFuel = 0.0;
    private ProgressBar progressBarRPM;
    private ProgressBar progressBarSpeed;
    Double T_s = 0.06;
    Double speed_Integral = 0.0;
    Double cur_z1_s = 0.0;
    Double int_z1_s = 0.0;
    Double fuelInit = 0.0, fuelFinal = 0.0;
    Double avFuel = 0.0, avKM = 0.0;
    Double initFuel = 0.0;
    Integer avCounter = 0;
    Double harcanan = 0.0, totalKM = 0.0;
    Boolean isMove = false;
    Boolean isfuel = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkExternalMedia();

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        file = new File(dir, "xxxxxxxxxxxxxxxxxxxxxxxx.txt");

        writeToSDFile("sa");

        progressBarRPM = (ProgressBar) findViewById(R.id.progressBarRPM);
        progressBarSpeed = (ProgressBar) findViewById(R.id.progressBarSpeed);
        speedText = findViewById(R.id.textView_speed);
        voltText =findViewById(R.id.textVolt);
        fuelStatusText =findViewById(R.id.gas_tank);
        coolantTempText =findViewById(R.id.engine_temperature);
        dateTXT = findViewById(R.id.dateText);
        rangeTXT = findViewById(R.id.range);
        fuelRatetXT = findViewById(R.id.burn_rate);
        setDate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public Double avarageKM(int speedInt){
        Double speed = (double)speedInt ;
        speed_Integral = (T_s*(speed + cur_z1_s) + 2 * int_z1_s) / 2;
        cur_z1_s = speed;
        int_z1_s = speed_Integral;
        return speed_Integral/475.0;
    }

    public  void setSpeed(String mspeed){
        curSpeed = Integer.parseInt(mspeed);
        totalKM = avarageKM(curSpeed);
        String m = String.format("%.2f",totalKM);
        m = m+"KM";
        rangeTXT.setText(m);
        if(mspeed.matches("0")) {
            isMove = false;
        }else{
            isMove = true;
        }
        speedText.setText(mspeed);
        int speed = Integer.parseInt(mspeed);
        int kayma = 0;
        speed +=kayma;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBarSpeed.setProgress(speed,true);
        }else{
            progressBarSpeed.setProgress(speed);
        }
        speedText.setText(mspeed);
    }
    public  void setRPM(String rpm){
        curRPM = Integer.parseInt(rpm);
        dataBase();
        int kayma = 0;
        int rpmInt = Integer.parseInt(rpm);
        rpmInt = (rpmInt * 3) / 100 + kayma;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBarRPM.setProgress(rpmInt,true);
        }else{
            progressBarRPM.setProgress(rpmInt);
        }
    }

    public  void setVolt(String mvolt){
        voltText.setText(mvolt);
        mvolt=mvolt.replace("V"," ").trim();
        try {
            curVolt = Double.parseDouble(mvolt);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();

        }
        // Toast.makeText(this, curVolt.toString(), Toast.LENGTH_SHORT).show();
    }

    public  void setFuelStatus(String m){
        Double x = Double.parseDouble(m);

        if(isfuel)
        {
            initFuel = x;
            isfuel = false;
        }

        harcanan =+  initFuel - x ;
        String y = String.format("%.2f",x);
        fuelStatusText.setText(y +" L");
        if(isMove) {
            Double fuelRate = (harcanan / totalKM);
            String fuelR = String.format("%.2f",fuelRate);
            fuelRatetXT.setText(fuelR);
        }
        try {
            curFuel = x;
        }catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public  void setcoolantTemp(String m){
        m = m+" C";
        coolantTempText.setText(m);

        try {
            curCoolant = Integer.parseInt(m);
        }catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

            dataBaseRare();
    }

    public void init(View view){
        //Context context = this.getApplicationContext();

        Log.i(DISPLAY_SERVICE, "Button clicked : " + view.getId());

        Ytask task = new Ytask(this);
        task.execute();
    }

    public void dataBase(){
        try{
            SQLiteDatabase database = openOrCreateDatabase("Data", MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS data (speed INTEGER, rpm INTEGER)");
            database.execSQL("INSERT INTO data (speed, rpm) VALUES ("+curSpeed+", "+curRPM+")");

            String s = "time : " + calendar.getTimeInMillis() + "speed : " + curSpeed + "RPM : " + curRPM;
            writeToSDFile(s);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void dataBaseRare(){
        try{
            SQLiteDatabase database = openOrCreateDatabase("Data", MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS rare (volt FLOAT, fuel INTEGER, coolant INTEGER)");
            database.execSQL("INSERT INTO rare (volt, fuel, coolant) VALUES ("+curVolt+", "+curFuel+", "+curCoolant+")");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void graphIntent(View view){
        Intent intent = new Intent(MainActivity.this,GraphScreen.class);
        intent.putExtra("type","speed");
        startActivity(intent);
    }
    public void setDate(){
        Calendar calendar = Calendar.getInstance();
        String s = calendar.getTime().toString();
        String [] token = s.split(" ");
        String date = token[2]+ " " + token[1] + " " + token[5];
        String [] tokenDate = token[3].split(":");
        String timeInDay = tokenDate[0]+ ":" + tokenDate[1];
        System.out.println(timeInDay);
        System.out.println(date);
        this.dateTXT.setText(date);
    }

    private void writeToSDFile(String s){
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);

            bufferedWriter.write(s);
            bufferedWriter.close();
            /*
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(s);
            pw.flush();
            pw.close();
            f.close();*/
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

}