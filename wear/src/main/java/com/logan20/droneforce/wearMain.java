package com.logan20.droneforce;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class wearMain extends WearableActivity {
    private final long WAIT_PERIOD = 10000;
    private DroneHandler droneHandler;
    protected CharSequence[] droneList;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

    }

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
                                new AlertDialog.Builder(wearMain.this)
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
