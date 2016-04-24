package com.logan20.droneforce;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.logan20.droneforceshared.dronePiloter.DroneFinder;


public class wearMain extends WearableActivity {
    private final static int REQUEST_CODE = 123;
    private DroneFinder droneHandler;
    private boolean hasPermissions;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();
        initBkg();
    }

    private void initBkg() {
        BoxInsetLayout mContainer = (BoxInsetLayout)findViewById(R.id.container);
        RelativeLayout mLayout = (RelativeLayout)findViewById(R.id.mainLinLayout);

        mContainer.setBackgroundColor(Color.parseColor("#80ffaa"));
        mLayout.setBackgroundColor(Color.WHITE);
    }

    public void setUp(View v){//this code will run only when user taps the line on the watch's screen
        permissions();//request user permissions first before doing anything
        listenForDrones();//set up activity that will listen for drones and connect to them
        setupProgress();//set new progress dialog
    }

    public void toggleAutoTakeoff(View v){
        if (droneHandler!=null){
            droneHandler.toggleAutoTakeoff();
        }
    }
    private void setupProgress() {
        progress = new ProgressDialog(this);//creates new progress dialog
        progress.setMessage("Please wait");//sets the message of the dialog
        progress.show();//shows the dialog
    }
    private void stopProgress(){
        if (progress!=null){
            progress.dismiss();//stop the progress bar
        }
    }

    /*@Override
    protected void onPause(){
        super.onPause();
        if (droneHandler!=null)
            droneHandler.stopAll();//stops the background tasks and threads in the other classes
    }*/

    private void init() {
        droneHandler = new DroneFinder(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(3000);
                    progress.dismiss();//exits the progress dialog
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForDrones() {
        if (hasPermissions==false){
            stopProgress();//removes the progress dialog
            Toast.makeText(this,"You have not granted all required permissions, please grant permissions",Toast.LENGTH_LONG);//display to the user to give permissions
        }
        else{
            init();//start the core of the progream
        }
    }

    private void permissions() {
        //check for permissions
        if (ContextCompat.checkSelfPermission(wearMain.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(wearMain.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(wearMain.this,Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            //show explaination if necessary
            if (ActivityCompat.shouldShowRequestPermissionRationale(wearMain.this,Manifest.permission.ACCESS_FINE_LOCATION)||ActivityCompat.shouldShowRequestPermissionRationale(wearMain.this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        new android.support.v7.app.AlertDialog.Builder(wearMain.this)
                                .setTitle("Request Permissions")
                                .setMessage("The app will not work without permissions necessary")
                                .show();
                        return null;
                    }
                }.execute();
            }
            else {
                //Request permissions
                ActivityCompat.requestPermissions(wearMain.this,new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH
                },REQUEST_CODE);
            }
        }
        else{
            //app has permissions
            Log.d("PERMISSIONS","All permissions are granted");
            hasPermissions=true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case REQUEST_CODE:
                if(permissions.length>0) {
                    boolean access=true;
                    for (int a=0;a<permissions.length;a++){//cycle through all permissions as all are needed
                        if (grantResults[a]!=PackageManager.PERMISSION_GRANTED){
                            access=false;
                            break;
                        }
                    }
                    if (!access){
                        Toast.makeText(this,"Please grant all permissions",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                Toast.makeText(this,"Please grant all permissions",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

}
