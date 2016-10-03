package com.logan20.droneforceshared.droneforceshared;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by kwasi on 7/18/2016.
 */
public class BatteryLevelMonitor {
    private Context context;
    private IntentFilter intentFilter;
    private Intent batteryStatus;
    private Thread thread; //thread to handle battery calculations
    private final long TIMEOUT = 10000;//time elapsed before printing battery percent

    public BatteryLevelMonitor(Context c){
        this.context=c;
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null,intentFilter);
        init();
        startThread();

    }

    public void startThread() {
        if (thread!=null){
            if (!thread.isAlive()){
                if (!thread.isInterrupted()){
                    thread.start();
                }
            }
        }
    }

    public void stopThread(){
        if (thread!=null){
            thread.interrupt();
        }
    }

    private void init() {
        thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    float batteryPct = level*100 / (float)scale;
                    Log.d("BATTERY",String.format("Current percent: %.2f",batteryPct));

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


}
