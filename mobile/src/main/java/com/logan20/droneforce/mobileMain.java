package com.logan20.droneforce;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.logan20.droneforceshared.dronePiloter.DroneFinder;



public class mobileMain extends AppCompatActivity {
    private final static int REQUEST_CODE = 123;
    private DroneFinder droneFinder;
    private boolean hasPermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        hasPermissions =false;

        //request user permissions first before doing anything
        permissions();
        listenForDrones();

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
        if (ContextCompat.checkSelfPermission(mobileMain.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(mobileMain.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(mobileMain.this,Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            //show explaination
            if (ActivityCompat.shouldShowRequestPermissionRationale(mobileMain.this,Manifest.permission.ACCESS_FINE_LOCATION)||ActivityCompat.shouldShowRequestPermissionRationale(mobileMain.this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        new AlertDialog.Builder(mobileMain.this)
                            .setTitle("Request Permissions")
                            .setMessage("The app will not work without permissions")
                            .show();
                        return null;
                    }
                }.execute();
            }
            else {
                //Request permissions
                ActivityCompat.requestPermissions(mobileMain.this,new String[]{
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mobile_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //code for the buttons on main mo bile page
    public void showSensor(View v){
        startActivity(new Intent(this, sensorActivity.class));
    }

    public void handleDrone(View v){ startActivity(new Intent(this, droneActivity.class));}

    public void rescanDrones(View v){
        Log.d("RESCAN","Rescanning for drones");
    }

}
