package com.logan20.droneforcemobile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.logan20.droneforceshared.droneforceshared.ARdroneAPI.ARDrone;
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

public class MainActivity extends AppCompatActivity implements DroneFoundListener, DroneStateListener,PilotListener {

    private static final long CONNECT_TIMEOUT = 4000;
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

    public static final int REQUEST_CODE_LOC = 100;
    public static final int REQUEST_CODE_BLE= 101;
    public static final int REQUEST_CODE_WIFI= 102;
    public static final int REQUEST_CODE_WIFI2= 103;
    public static final int REQUEST_CODE_WIFI3= 104;
    public static final int REQUEST_CODE_WIFI4= 105;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryView =(TextView) findViewById(R.id.tv_droneBattery);
        nameView = (TextView) findViewById(R.id.tv_droneName);
        stateView = (TextView) findViewById(R.id.tv_droneState);
        actionView =  (TextView) findViewById(R.id.tv_droneAction);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TAG = getClass().getName();
        if (hasPermissions()){
           // droneFinder = new DroneFinder(this);
            Toast.makeText(this,getResources().getString(R.string.drone_finder),Toast.LENGTH_LONG).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        // Create ARDrone object,
                        // connect to drone and initialize it.
                        ARDrone drone = new ARDrone();
                        drone.connect();
                        drone.clearEmergencySignal();
                        Log.d(TAG, "run: connecting");
                        // Wait until drone is ready
                        Thread.sleep(10000);
                        drone.waitForReady(CONNECT_TIMEOUT);
                        Log.d(TAG, "run: trimming");
                        // do TRIM operation
                        drone.trim();

                        Log.d(TAG, "run: taking off");
                        // Take off
                        System.err.println("Taking off");
                        drone.takeOff();

                        // Fly a little :)
                        Thread.sleep(5000);

                        // Land
                        System.err.println("Landing");
                        drone.land();

                        // Give it some time to land
                        Thread.sleep(2000);

                        // Disconnect from the done
                        drone.disconnect();

                    } catch(Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }



    private boolean hasPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)== PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_WIFI_STATE)== PackageManager.PERMISSION_GRANTED){
            return true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOC);

            /*new AlertDialog.Builder(this)
                    .setTitle("Location Permission")
                    .setMessage("This app needs location access so that the drone will be able to send signals properly to the device. Please grant access.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOC);
                        }
                    })
                    .show();*/
        }

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
           /* new AlertDialog.Builder(this)
                    .setTitle("Bluetooth permission")
                    .setMessage("Depending on the drone bluetooth may be needed to communicate, please grant bluetooth permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},REQUEST_CODE_BLE);
                        }
                    })
                    .show();*/
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.BLUETOOTH_ADMIN},REQUEST_CODE_BLE);

        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){
            /*new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Depending on the internet may be needed to communicate, please grant permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.INTERNET},REQUEST_CODE_WIFI);
                        }
                    })
                    .show();*/
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.INTERNET},REQUEST_CODE_WIFI);

        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_WIFI_STATE)!=PackageManager.PERMISSION_GRANTED){
            /*new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Depending on the internet may be needed to communicate, please grant permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_WIFI_STATE},REQUEST_CODE_WIFI2);
                        }
                    })
                    .show();*/
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_WIFI_STATE},REQUEST_CODE_WIFI2);

        }

       // if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)!=PackageManager.PERMISSION_GRANTED){
            /*new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Depending on the internet may be needed to communicate, please grant permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE},REQUEST_CODE_WIFI3);
                        }
                    })
                    .show();*/
           // ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE},REQUEST_CODE_WIFI3);

       //}

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CHANGE_WIFI_STATE)!=PackageManager.PERMISSION_GRANTED){
            /*new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Depending on the internet may be needed to communicate, please grant permissions")
                    .setPositiveButton(android.R.string.ok,null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CHANGE_WIFI_STATE},REQUEST_CODE_WIFI4);
                        }
                    })
                    .show();*/
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CHANGE_WIFI_STATE},REQUEST_CODE_WIFI4);

        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case REQUEST_CODE_LOC:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted LOCATIOn!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                   // finish();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_CODE_BLE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted bluetooth!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                   // finish();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_CODE_WIFI:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted wifi!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    //finish();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_CODE_WIFI2:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted wifi state!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                 //   finish();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_CODE_WIFI3:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setPositiveButton(android.R.string.ok, null)
                            .setMessage("Permissions need to be granted wifi multicast state!")
                            .setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                //    finish();
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
                            controllerListener = new DroneState(MainActivity.this);//listener that will relay the drone state changes
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
