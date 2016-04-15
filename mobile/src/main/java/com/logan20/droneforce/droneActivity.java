package com.logan20.droneforce;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class droneActivity extends AppCompatActivity {
    private final long WAIT_PERIOD = 10000;
    protected CharSequence[] droneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone);

        //init();
        //updateDroneList();
    }
/*
    private void init() {
        droneHandler = new DroneHandler(this);
    }

    private void updateDroneList() {
        final long startTime = Math.abs(System.currentTimeMillis()%1000000);
        final ProgressDialog dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    droneList=droneHandler.getDroneList();
                    if (droneList==null){
                        long currentTime = Math.abs(System.currentTimeMillis()%1000000);
                        System.out.println(currentTime + "  "+startTime+ "  "+(startTime-currentTime));
                        if (Math.abs(currentTime-startTime)>WAIT_PERIOD) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "No drones found", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        }
                        else{
                            try{
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                new AlertDialog.Builder(droneActivity.this)
                                        .setTitle("Choose a drone")
                                        .setItems(droneList, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                droneHandler.setActiveDrone(which);
                                            }
                                        })
                                        .show();
                            }
                        });
                        break;
                    }
                }
            }
        }).start();
    }*/
}
