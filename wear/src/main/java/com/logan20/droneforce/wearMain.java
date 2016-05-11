package com.logan20.droneforce;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class wearMain extends WearableActivity {
    private final int REQUEST_CODE_LOC = 123;
    private final int REQUEST_CODE_BLE = 124;
    private DroneFinder droneHandler;
    private boolean hasPermLoc=true,hasPermBle=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        permissions();//request user permissions first before doing anything
        initBkg(); //sets up the color scheme of the layout
    }
    @Override
    protected void onStop(){
        if (droneHandler!=null) {
            droneHandler.stopAll();
        }
        super.onStop();
    }
    private void initBkg() {
        BoxInsetLayout mContainer = (BoxInsetLayout)findViewById(R.id.container);//gets the main container
        RelativeLayout mLayout = (RelativeLayout)findViewById(R.id.buttonRelLayout);//gets the main linear layout
        mContainer.setBackgroundColor(Color.parseColor("#80FFAA"));//sets the color of the container to be green
        mLayout.setBackgroundColor(Color.WHITE);//sets the color of the linear layout tobe white
    }

    public void setUp(View v){//this code will run only when user taps the line on the watch's screen

        if (hasPermBle){
            listenForDrones();//set up activity that will listen for drones and connect to them
            v.setClickable(false);
            v.setBackgroundResource(R.drawable.connect_disabled);
        }
        else{
            new AlertDialog.Builder(this)
                    .setMessage("Bluetooth not accessible, please allow the application to access bluetooth services")
                    .setPositiveButton(android.R.string.ok,null)
                    .show();
        }
    }
    private void init() {
        droneHandler = new DroneFinder(this,this);
    }

    private void listenForDrones() {
        if (hasPermLoc){
            init();//start the core of the program
        }
    }

    private void permissions() {
        //Permission check 

        //location
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            hasPermLoc=false;
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Access needed");
            builder.setMessage("This app needs location access so that the drone will be able to send signals properly to the device. Please grant access.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    ActivityCompat.requestPermissions(wearMain.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOC);
                }
            });
            builder.show();
        }
        //bluetooth
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
            hasPermBle=false;
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth permission")
                    .setMessage("Depending on the drone bluetooth may be needed to communicate, please grant bluetooth permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(wearMain.this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},REQUEST_CODE_BLE);
                        }
                    })
                    .show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case REQUEST_CODE_LOC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GRANT", "Coarse location granted");
                    hasPermLoc = true;
                } else {
                    Log.d("Grant", "Location access denied");
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("App will now exit. Permissions needed")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_CODE_BLE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GRANT", "Bluetooth permission granted");
                    hasPermBle = true;
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage("You will not be able to connect to any bluetooth drones, press OK to continue")
                            .setTitle("Permission")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;

        }
    }
}
