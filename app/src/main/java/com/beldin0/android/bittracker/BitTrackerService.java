package com.beldin0.android.bittracker;


import android.app.Service;
import android.content.Intent;
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

public class BitTrackerService extends Service {

    private final IBinder mBinder = new MyBinder();
    public static Runnable runnable = null;
    private Handler handler = null;
    private HandlerThread handlerThread = null;

    private static final String DEFAULT_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private static int UPDATE_DELAY = 1000 * 3; // 3 seconds delay

    private URL apiURL;
    private double[] price;
    private boolean end;
    private String lastUpdate;
    private int count;

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
        Intent local = new Intent();
        local.setAction("com.beldin0.BitTracker.action");
        local.putExtra("latest", lastUpdate);
        local.putExtra("value", latest());
        local.putExtra("av5", average(5));
        local.putExtra("av10", average(10));
        local.putExtra("av15", average(15));
        this.sendBroadcast(local);
    }

    private void getNewPrice() {
        String jsonString="";
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
    }

    public boolean isFull() {
        return (count==15);
    }

    public void add(double d) {
        if (count>=15) count = 14;
        for (int i=count; i>0; i--) {
            price [i] = price[i-1];
        }
        price[0] = d;
        if(++count==15 && UPDATE_DELAY < 1000 * 60) UPDATE_DELAY = 1000 * 60;
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
        // Log.d("sum", number + " " + startAt + " " + count);
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
            // Log.e("JSON Parser", "Error parsing data " + e.toString());
            System.out.println("error on parse data in jsonparser.java");
        }

        // return JSON String
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

        String inputLine = "";
        try {
            json = in.readLine();
            // Log.d("getJSON", json);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            in.close();
        }
        // Log.d("json",json);
        return json;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        go();
        return Service.START_STICKY;
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