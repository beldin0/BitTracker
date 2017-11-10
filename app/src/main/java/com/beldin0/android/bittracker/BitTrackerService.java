package com.beldin0.android.bittracker;


import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BitTrackerService extends Service {

    public static final String CURRENT_VALUE = "Current Value";
    public static final String AVERAGE_5 = "5-Point Average";
    public static final String AVERAGE_10 = "10-Point Average";
    public static final String AVERAGE_15 = "15-Point Average";

    private final IBinder mBinder = new MyBinder();
    public static Runnable runnable = null;
    private Handler handler = null;
    private HandlerThread handlerThread = null;

    private static final String DEFAULT_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private static int UPDATE_DELAY = 1000 * 3; // 3 seconds delay

    private URL apiURL;
    private double[] price;
    private boolean end;
    private String firstUpdate = "";
    private String lastUpdate;
    private int count;
    private ArrayList<ValueEntry> values;
    private Map<String, Integer> oldOrder = new TreeMap<>();
    private double boughtPrice;
    private double profit;
    private int txns = 0;
    private ArrayList<String> notifications = new ArrayList<>();
    private boolean UIConnected;

    public void setUIConnected(boolean UIConnected) {
        this.UIConnected = UIConnected;
    }

    public BitTrackerService() {
        this(DEFAULT_URL);
    }

    public BitTrackerService(String url) {
        try {
            apiURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            apiURL = null;
        }
        price = new double[15];
        lastUpdate = "";
        count = 0;
        end = false;
    }

    @Override
    public void onCreate() {
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        handler = new Handler(looper);
        runnable = new Runnable() {

            @Override
            public void run() {
                go();
                handler.postDelayed(runnable, UPDATE_DELAY);
            }
        };
        handler.postDelayed(runnable, UPDATE_DELAY);
    }


    public void stop () {
        end = true;
    }

    public void go() {
        getNewPrice();
        if (values!= null) fixOldOrder();
        values = new ArrayList<>();
        values.add(new ValueEntry(CURRENT_VALUE, latest()));
        if (!(average(5)==0)) values.add(new ValueEntry(AVERAGE_5, average(5), Color.GREEN));
        if (!(average(10)==0)) values.add(new ValueEntry(AVERAGE_10, average(10), Color.MAGENTA));
        if (!(average(15)==0)) values.add(new ValueEntry(AVERAGE_15, average(15), Color.RED));
        Collections.sort(values);
        String tmpNoti = compareOrders();
        if (!("".equals(tmpNoti))) {
            notifications.add(tmpNoti);
            if (notifications.size() > 10) {
                notifications.remove(0);
            }
        }

        if (UIConnected) {
            Intent local = new Intent();
            local.putExtra("latest", lastUpdate);
            local.putExtra("notifications", notifications);
            local.putParcelableArrayListExtra("values", values);
            local.setAction("com.beldin0.BitTracker.action");
            this.sendBroadcast(local);
        } else {
            // TODO raise a notification with the text from tmpNoti
        }
    }

    private void getNewPrice() {
        String jsonString;
        try {
            jsonString = getJSON();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObject obj = getObjectFromJSON(jsonString);
        JsonObject bpi = (JsonObject) obj.get("bpi");
        JsonObject usd = (JsonObject) bpi.get("USD");
        JsonObject time = (JsonObject) obj.get("time");
        add(usd.get("rate_float").getAsDouble());
        lastUpdate = (time.get("updateduk").getAsString());
        if ("".equals(firstUpdate)) firstUpdate = (time.get("updateduk").getAsString());
    }

    public boolean isFull() {
        return (count>=15);
    }

    public void add(double d) {
        if (isFull()) count = 14;
        for (int i=count; i>0; i--) {
            price [i] = price[i-1];
        }
        price[0] = d;
        count++;
        if(isFull() && UPDATE_DELAY < 1000 * 60) UPDATE_DELAY = 1000 * 60;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public int getCount() {
        return count;
    }

    public double latest() {
        return price[0];
    }

    public double average(int num) {
        return average(num,0);
    }

    public double average (int num, int startAt) {
        if (count == 0 || num < 1) return 0;
        if (num > count) return 0;
        return sum(num, startAt) / num;
    }

    private double sum(int number) {
        return sum(number, 0);
    }

    private double sum(int number, int startAt) {
        if (number<1 || startAt < 0 || startAt > count || number > count) return 0;
        if (number + startAt > count) startAt = count - number;
        if (number > count) number = count;
        double total = 0;
        for (int i=startAt+1; i<=number; i++) {
            total += price[i-1];
        }
        return total;
    }

    private JsonObject getObjectFromJSON(String json) {

        JsonObject jObj = null;

        // try parse the string to a JSON object
        try {
            jObj = new JsonParser().parse(json).getAsJsonObject();
        } catch (JsonParseException e) {
            System.out.println("error on parse data in jsonparser.java");
        }
        return jObj;

    }

    private String getJSON() throws Exception {
        String json = "";
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(apiURL.openStream()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            json = in.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            in.close();
        }
        return json;
    }

    private String compareOrders() {
        if(oldOrder==null || oldOrder.size()<15 || values==null) return "";

        boolean shouldBuy = false;
        boolean shouldSell = false;
        String noti = "";

        if (oldOrder.get(AVERAGE_5) > oldOrder.get(AVERAGE_15)
                && getIndex(AVERAGE_5) < getIndex(AVERAGE_15)) {
            shouldBuy = true;
        } else if (oldOrder.get(AVERAGE_5) < oldOrder.get(AVERAGE_10)
                && getIndex(AVERAGE_5) > getIndex(AVERAGE_10)) {
            shouldSell = true;
        }

        if (shouldBuy && boughtPrice == 0) {
            boughtPrice = getValue(CURRENT_VALUE);
            noti = lastUpdate + "\n" + "BUY: " + setDecimals(boughtPrice,2);
        }else if (shouldSell && boughtPrice > 0) {
            double soldPrice = getValue(CURRENT_VALUE);
            double thisProfit = soldPrice - boughtPrice;
            profit += thisProfit;
            txns++;
            noti = lastUpdate + "\n" + "SELL: " + setDecimals(soldPrice,2) + " (make " + setDecimals(thisProfit,2) + ", total profit " + setDecimals(profit,2) + ")(" + txns + ")";
            boughtPrice = 0;
        }

        return noti;
    }

    public int getIndex (String name) {
        return values.indexOf(new ValueEntry(name));
    }

    public double getValue (String name) {
        return values.get(getIndex(name)).getValue();
    }

    private void fixOldOrder() {
        for (int i=0; i<values.size(); i++) {
            oldOrder.put(values.get(i).getName(), i);
        }
    }

    public ArrayList<ValueEntry> getValues() {
        return values;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public String getFirstUpdate() {
        return firstUpdate;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        go();
        return Service.START_STICKY;
    }

    public String setDecimals (double value, int decimals) {
        return new DecimalFormat("#,###" + ((decimals>0)? ("." + new String(new char[decimals]).replace("\0","0")): "")).format(value);
    }

    public String setDecimals(double value)
    {
        return setDecimals(value, 4);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        BitTrackerService getService() {
            return BitTrackerService.this;
        }
    }
}