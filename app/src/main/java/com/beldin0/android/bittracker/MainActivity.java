package com.beldin0.android.bittracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements ServiceConnection {

    private BitTrackerService bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = new Intent(this, BitTrackerService.class);
        startService(intent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BitTrackerService();
        updateScreen();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.beldin0.BitTracker.action");
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                update(intent);
            }

        },filter);
    }

    private void update(Intent local) {
        ArrayList<ValueEntry> tempValues = local.getParcelableArrayListExtra("values");
        Bundle b = local.getExtras();
        String previousUpdate = b.getString("latest");
        ArrayList<String> tempNoti = b.getStringArrayList("notifications");
        updateScreen(previousUpdate, tempNoti, tempValues);
    }

    private void updateScreen() {
        updateScreen(bt.getLastUpdate(), bt.getNotifications(), bt.getValues());
    }

    private void updateScreen(String previousUpdate, ArrayList<String> tempNoti, ArrayList<ValueEntry> values) {
        updateNotifications(tempNoti);
        ((TextView) findViewById(R.id.txtLastUpdate)).setText(String.format("Last Update: %s", previousUpdate));
        ((ListView) findViewById(R.id.list)).setAdapter(new MyListAdapter(values));
    }

    private void updateNotifications(ArrayList<String> notifications) {
        if (notifications == null || notifications.size()==0) return;
        TextView notiView = (TextView) findViewById(R.id.notifications);
        notiView.setText("");
        for (int i=0; i<notifications.size(); i++) {
            notiView.append(String.format("%s\n", notifications.get(i)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BitTrackerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        bt.setUIConnected(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bt.setUIConnected(false);
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        BitTrackerService.MyBinder b = (BitTrackerService.MyBinder) binder;
        bt = b.getService();
        bt.setUIConnected(true);
        String first = bt.getFirstUpdate();
        Toast.makeText(MainActivity.this, String.format("Connected: %s", first), Toast.LENGTH_SHORT).show();
        updateScreen();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bt.setUIConnected(false);
        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        //bt = null;
    }

}