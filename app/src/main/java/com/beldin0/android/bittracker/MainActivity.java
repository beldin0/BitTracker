package com.beldin0.android.bittracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity implements ServiceConnection {

    public static final String CURRENT_VALUE = "Current Value";
    public static final String AVERAGE_5 = "Average(5)";
    public static final String AVERAGE_10 = "Average(10)";
    public static final String AVERAGE_15 = "Average(15)";

    private BitTrackerService bt;
    private boolean mIsBound;
    private BroadcastReceiver updateUIReceiver;

    private String previousUpdate = "";
    private ValueArray values;
    private MyListAdapter adapter;
    private TextView textView;
    private ListView listView;
    private TextView notiView;
    private Map<String, Integer> oldOrder;
    private double boughtPrice;
    private double profit;
    private int txns=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BitTrackerService();
        oldOrder = new TreeMap<>();
        values = new ValueArray();
        values.add(new ValueEntry(CURRENT_VALUE, 0.00));
        values.add(new ValueEntry(AVERAGE_5, 0.00));
        values.add(new ValueEntry(AVERAGE_10, 0.00));
        values.add(new ValueEntry(AVERAGE_15, 0.00));
        adapter = new MyListAdapter(values);
        textView = (TextView) findViewById(R.id.txtLastUpdate);
        notiView = (TextView) findViewById(R.id.notifications);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        IntentFilter filter = new IntentFilter();

        filter.addAction("com.beldin0.BitTracker.action");

        updateUIReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                update(intent);
            }
        };
        registerReceiver(updateUIReceiver,filter);
    }

    private void update(Intent local) {
        Bundle b = local.getExtras();
        previousUpdate = b.getString("latest");
        textView.setText("Last Update: " + previousUpdate);
        fixOldOrder();
        values.clear();
        values.add(new ValueEntry(CURRENT_VALUE, b.getDouble("value")));
        values.add(new ValueEntry(AVERAGE_5, b.getDouble("av5"), Color.GREEN));
        values.add(new ValueEntry(AVERAGE_10, b.getDouble("av10"), Color.MAGENTA));
        values.add(new ValueEntry(AVERAGE_15, b.getDouble("av15"), Color.RED));
        Collections.sort(values);
        adapter.notifyDataSetChanged();
        compareOrders();
    }

    private void fixOldOrder() {
        for (int i=0; i<values.size(); i++) {
            oldOrder.put(values.get(i).getName(), i);
        }
    }

    private void compareOrders() {
        boolean shouldBuy = false;
        boolean shouldSell = false;

        if (oldOrder.get(AVERAGE_5) > oldOrder.get(AVERAGE_15)
                && values.getIndex(AVERAGE_5) < values.getIndex(AVERAGE_15)) {
            shouldBuy = true;
        };

        if (oldOrder.get(AVERAGE_5) < oldOrder.get(AVERAGE_10)
                && values.getIndex(AVERAGE_5) > values.getIndex(AVERAGE_10)) {
            shouldSell = true;
        };

        String noti = "";

        if (shouldBuy && boughtPrice == 0) {
            boughtPrice = values.getValue(CURRENT_VALUE);
            noti = "BUY: " + setDecimals(boughtPrice,2);
        }

        if (shouldSell && boughtPrice > 0) {
            double soldPrice = values.getValue(CURRENT_VALUE);
            double thisProfit = soldPrice - boughtPrice;
            profit += thisProfit;
            txns++;
            noti = "SELL: " + setDecimals(soldPrice,2) + " (make " + setDecimals(thisProfit,2) + ", total profit " + setDecimals(profit,2) + ")(" + txns + ")";
            boughtPrice = 0;
        }

        if (!"".equals(noti)) notiView.append("\n" + noti);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, BitTrackerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        BitTrackerService.MyBinder b = (BitTrackerService.MyBinder) binder;
        bt = b.getService();
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bt = null;
    }

    public String setDecimals (double value, int decimals) {
        return new DecimalFormat("#,###" + ((decimals>0)? ("." + new String(new char[decimals]).replace("\0","0")): "")).format(value);
    }

    public String setDecimals(double value)
    {
        return setDecimals(value, 4);
    }
}