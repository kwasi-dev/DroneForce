package com.logan20.droneforce;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.logan20.droneforceshared.dronePiloter.DroneFinder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class wearMain extends WearableActivity {
    private final long WAIT_PERIOD = 10000;
    private DroneFinder droneHandler;
    protected CharSequence[] droneList;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private final static int REQUEST_CODE = 123;
    private DroneFinder droneFinder;
    private boolean hasPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
        //request user permissions first before doing anything
        permissions();
        listenForDrones();
    }

    private void init() {
        droneHandler = new DroneFinder(this);
    }

    private void listenForDrones() {
        if (hasPermissions==false){
            Toast.makeText(this,"You have not granted all required permissions, please grant permissions",Toast.LENGTH_LONG);
            finish();
        }
        else{
            droneFinder=new DroneFinder(this);
        }
    }

    private void permissions() {
        if (ContextCompat.checkSelfPermission(wearMain.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(wearMain.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(wearMain.this,Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            //show explaination
            if (ActivityCompat.shouldShowRequestPermissionRationale(wearMain.this,Manifest.permission.ACCESS_FINE_LOCATION)||ActivityCompat.shouldShowRequestPermissionRationale(wearMain.this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        new android.support.v7.app.AlertDialog.Builder(wearMain.this)
                                .setTitle("Request Permissions")
                                .setMessage("The app will not work without permissions")
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
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }
}
