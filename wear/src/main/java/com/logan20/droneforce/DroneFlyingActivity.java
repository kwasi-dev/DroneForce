package com.logan20.droneforce;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.logan20.droneforceshared.droneforceshared.DroneFinder;
import com.logan20.droneforceshared.droneforceshared.DroneFoundListener;
import com.logan20.droneforceshared.droneforceshared.DroneState;
import com.logan20.droneforceshared.droneforceshared.DroneStateListener;
import com.logan20.droneforceshared.droneforceshared.Pilot;
import com.logan20.droneforceshared.droneforceshared.PilotListener;
import com.logan20.droneforceshared.droneforceshared.SensorClass;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;

import java.util.List;
import java.util.Locale;

public class DroneFlyingActivity extends Activity implements DroneFoundListener, DroneStateListener,PilotListener {
    private DroneFinder droneFinder;
    private ARDiscoveryDevice drone;
    private ARDeviceController controller;
    private ARDeviceControllerListener controllerListener;
    private TextView batteryView;
    private TextView nameView;
    private TextView stateView;
    private TextView actionView;
    private String TAG;
    private Pilot pilot;
    private SensorClass sensorClass;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_drone);
        batteryView =(TextView) findViewById(R.id.tv_droneBattery);
        nameView = (TextView) findViewById(R.id.tv_droneName);
        stateView = (TextView) findViewById(R.id.tv_droneState);
        actionView =  (TextView) findViewById(R.id.tv_droneAction);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TAG = getClass().getName();
        if (hasPermissions()){
            droneFinder = new DroneFinder(this);
            Toast.makeText(this,getResources().getString(R.string.drone_finder),Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)== PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED){
            return true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("This app needs location access so that the drone will be able to send signals properly to the device. Please grant access.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(DroneFlyingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, WearMainActivity.REQUEST_CODE_LOC);
                        }
                    })
                    .show();
        }

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
            new AlertDialog.Builder(this)
                    .setTitle("Bluetooth permission")
                    .setMessage("Depending on the drone bluetooth may be needed to communicate, please grant bluetooth permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(DroneFlyingActivity.this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},WearMainActivity.REQUEST_CODE_BLE);
                        }
                    })
                    .show();
        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){
            new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Depending on the internet may be needed to communicate, please grant permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(DroneFlyingActivity.this,new String[]{Manifest.permission.INTERNET},WearMainActivity.REQUEST_CODE_BLE);
                        }
                    })
                    .show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case WearMainActivity.REQUEST_CODE_LOC:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            case WearMainActivity.REQUEST_CODE_BLE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    @Override
    public void onDroneFound(final List<String> list) {
        new AlertDialog.Builder(this)
                .setTitle("Choose a drone:")
                .setItems(list.toArray(new CharSequence[list.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            pilot = null;
                            drone=droneFinder.createDrone(which);//get the drone drone selected
                            controller = new ARDeviceController(drone);//set up the controller
                            controllerListener = new DroneState(DroneFlyingActivity.this);//listener that will relay the drone state changes
                            controller.addListener(controllerListener);//control movement using this class
                            droneFinder.stopFindDrones();
                            nameView.setText(String.format(Locale.getDefault(),"%s : %s",getResources().getString(R.string.drone_name),list.get(which)));
                            if (controller!=null){
                                controller.start();
                                pilot = new Pilot(droneFinder.getDroneType(which),controller, (DroneState) controllerListener);
                                sensorClass = new SensorClass((SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE),pilot);
                            }

                        } catch (ARControllerException e) {
                            e.printStackTrace();

                        }
                    }
                })
                .show();

    }

    @Override
    public void onStateChange(String message, final String value) {
        switch (message){
            case "battery":
                batteryView.post(new Runnable() {
                    @Override
                    public void run() {
                        batteryView.setText(String.format(Locale.getDefault(),"%s : %s",getResources().getString(R.string.drone_battery),value));
                    }
                });
                break;
            case "state":
                stateView.post(new Runnable() {
                    @Override
                    public void run() {
                        stateView.setText(String.format(Locale.getDefault(),"%s : %s",getResources().getString(R.string.drone_state),value));

                    }
                });
                break;
            default:
                Log.d(TAG, "onStateChange: unknown state "+message);
        }
    }

    @Override
    public void onMovementChanged(String newMovement) {
        actionView.setText(String.format(Locale.getDefault(),"%s : %s",getResources().getString(R.string.drone_action),newMovement));
    }

    public void autoTakeoff(View v){
        if (pilot!=null){
            pilot.autoTakeOff();
        }
    }

    public void emergencyLand(View v){
        if (pilot!=null){
            pilot.emergencyLand();
        }
    }
}
