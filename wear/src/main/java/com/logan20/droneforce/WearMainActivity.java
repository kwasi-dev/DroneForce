package com.logan20.droneforce;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WearMainActivity extends WearableActivity {
    private String TAG;
    public static final int REQUEST_CODE_LOC = 100;
    //public static final int REQUEST_CODE_WIFI= 101;
    public static final int REQUEST_CODE_BLE= 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TAG = getClass().getSimpleName();
        populateList();
    }

    private void populateList() {
        ListView menuList =(ListView)findViewById(R.id.lv_main_menu);
        menuList.setAdapter(ArrayAdapter.createFromResource(getApplicationContext(),R.array.main_menu_option_list,android.R.layout.simple_list_item_1));
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (adapterView.getItemAtPosition(i).toString()){
                    case "Connect to drone":
                        startActivity(new Intent(WearMainActivity.this,DroneFlyingActivity.class));
                        break;
                    case "View Sensor Information":
                        startActivity(new Intent(WearMainActivity.this,SensorReadingActivity.class));
                        break;
                    case "AR Drone":
                        startActivity(new Intent(WearMainActivity.this,ArConnectActivity.class));
                        break;
                    default:
                        Log.d(TAG, "onItemClick: Unknown item "+adapterView.getItemAtPosition(i).toString());
                }
            }
        });
    }
}
