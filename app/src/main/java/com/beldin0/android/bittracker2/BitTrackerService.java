package com.beldin0.android.bittracker2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class BitTrackerService extends Service {

    private static final int RUNNING_NOTIFICATION = 1;
    private static final int ALERT_NOTIFICATION = 2;
    private static final int MAX_NUMBER_OF_PRICES = 20;

    private final IBinder mBinder = new MyBinder();
    private static Runnable runnable = null;
    private Handler handler = null;

    private static final String DEFAULT_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private static int UPDATE_DELAY = 1000 * 3; // 3 seconds delay

    private URL apiURL;
    private final double[] arrayOfPrices;
    private String dateAndTimeOfFirstPricePoint = "";
    private String dateAndTimeOfMostRecentPricePoint = "";
    private int count;
    private final ArrayList<String> notifications = new ArrayList<>();
    private boolean UIConnected;

    private final BitTrackerAlgorithm algorithm;

    public void setUIConnected(boolean UIConnected) {
        this.UIConnected = UIConnected;
        if (UIConnected) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(RUNNING_NOTIFICATION);
            }
        } else {
            startForeground(RUNNING_NOTIFICATION, buildNotification(String.format("Running... Last value: %s",setDecimals(latest(),2))));
        }
    }

    public BitTrackerService() {
        this(DEFAULT_URL);
    }

    public BitTrackerService(String url) {
        algorithm = new SimpleAverageAlgorithm();
        arrayOfPrices = new double[MAX_NUMBER_OF_PRICES];
        count = 0;
        try {
            apiURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            apiURL = null;
        }
    }

    @Override
    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
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

    private void go() {
        getNewPrice();
        String tmpNotification = algorithm.compareOrders(dateAndTimeOfMostRecentPricePoint, arrayOfPrices);
        if (!("".equals(tmpNotification))) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(ALERT_NOTIFICATION, buildNotification(tmpNotification));
            }
            notifications.add(tmpNotification);
            if (notifications.size() > 10) {
                notifications.remove(0);
            }
        }

        if (UIConnected) {
            Intent local = new Intent();
            local.putExtra("latest", dateAndTimeOfMostRecentPricePoint);
            local.putExtra("notifications", notifications);
            local.putParcelableArrayListExtra("values", algorithm.getValuesToBeShownInUI());
            local.setAction("com.beldin0.BitTracker.action");
            this.sendBroadcast(local);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(RUNNING_NOTIFICATION);
                notificationManager.notify(RUNNING_NOTIFICATION, buildNotification(String.format("Running... Last value: %s",setDecimals(latest(),2))));
            }

        }
    }

    private Notification buildNotification(String notificationText) {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("BitTracker")
                        .setContentText(notificationText)
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        return mBuilder.build();
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
        dateAndTimeOfMostRecentPricePoint = (time.get("updateduk").getAsString());
        if ("".equals(dateAndTimeOfFirstPricePoint)) dateAndTimeOfFirstPricePoint = dateAndTimeOfMostRecentPricePoint;
    }

    private boolean isFull() {
        return (count>= MAX_NUMBER_OF_PRICES);
    }

    public void add(double d) {
        if (isFull()) {
            count = MAX_NUMBER_OF_PRICES - 1;
        }
        System.arraycopy(arrayOfPrices, 0, arrayOfPrices, 1, count);
        arrayOfPrices[0] = d;
        count++;
        if(isFull() && UPDATE_DELAY < 1000 * 60) UPDATE_DELAY = 1000 * 60;
    }

    public String getDateAndTimeOfMostRecentPricePoint() {
        return dateAndTimeOfMostRecentPricePoint;
    }

    public int getCount() {
        return count;
    }

    public double latest() {
        return arrayOfPrices[0];
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
        String json;
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(apiURL.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("".getBytes())));
        }

        try {
            json = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            json = "";
        } finally {
            in.close();
        }
        return json==null? "" : json;
    }

    public ArrayList<ValueEntry> getValuesToBeShownInUI() {
        return algorithm.getValuesToBeShownInUI();
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public String getFirstUpdate() {
        return dateAndTimeOfFirstPricePoint;
    }

    private String setDecimals(double value, int decimals) {
        return new DecimalFormat("#,###" + ((decimals>0)? ("." + new String(new char[decimals]).replace("\0","0")): "")).format(value);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        go();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {
        BitTrackerService getService() {
            return BitTrackerService.this;
        }
    }
}