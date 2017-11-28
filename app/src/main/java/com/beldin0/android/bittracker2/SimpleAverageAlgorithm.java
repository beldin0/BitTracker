package com.beldin0.android.bittracker2;

import android.graphics.Color;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

class SimpleAverageAlgorithm implements BitTrackerAlgorithm {

    private static final String CURRENT_VALUE = "Current Value";
    private static final String AVERAGE_5 = "5-Point Average";
    private static final String AVERAGE_10 = "10-Point Average";
    private static final String AVERAGE_15 = "15-Point Average";

    private ArrayList<ValueEntry> valuesToBeShownInUI;
    private double boughtPrice;
    private double profit;
    private double[] price;
    private int count;
    private int numberOfTransactionsMade = 0;

    public ArrayList<ValueEntry> getValuesToBeShownInUI() {
        return valuesToBeShownInUI;
    }

    public String compareOrders(String dateAndTimeOfMostRecentPricePoint, double[] arrayOfPrices) {
        setCount(arrayOfPrices);
        valuesToBeShownInUI = new ArrayList<>();
        valuesToBeShownInUI.add(new ValueEntry(CURRENT_VALUE, arrayOfPrices[0]));
        if (!(average(5)==0)) valuesToBeShownInUI.add(new ValueEntry(AVERAGE_5, average(5), Color.GREEN));
        if (!(average(10)==0)) valuesToBeShownInUI.add(new ValueEntry(AVERAGE_10, average(10), Color.YELLOW));
        if (!(average(15)==0)) valuesToBeShownInUI.add(new ValueEntry(AVERAGE_15, average(15), Color.RED));
        Collections.sort(valuesToBeShownInUI);

        if(average(15,1) == 0 || valuesToBeShownInUI ==null) return "";

        boolean shouldBuy = false;
        boolean shouldSell = false;
        String notification = "";

        if (average(5,1) > average(15,1)
                && average(5) < average(15)) {
            shouldBuy = true;
        } else if (average(5,1) < average(10,1)
                && average(5) > average(10)) {
            shouldSell = true;
        }

        if (shouldBuy && boughtPrice == 0) {
            boughtPrice = arrayOfPrices[0];
            notification = "BUY: " + setDecimals(boughtPrice,2)+ "\n" + dateAndTimeOfMostRecentPricePoint;
        }else if (shouldSell && boughtPrice > 0) {
            double soldPrice = arrayOfPrices[0];
            double thisProfit = soldPrice - boughtPrice;
            profit += thisProfit;
            numberOfTransactionsMade++;
            notification = "SELL: " + setDecimals(soldPrice,2) + " (make " + setDecimals(thisProfit,2) + ", total profit " + setDecimals(profit,2) + ")(" + numberOfTransactionsMade + ")" + "\n" + dateAndTimeOfMostRecentPricePoint;
            boughtPrice = 0;
        }

        return notification;
    }

    private double average(int num) {
        return average(num,0);
    }

    private double average(int num, int startAt) {
        if (count == 0 || num < 1) return 0;
        if (num > count) return 0;
        return sum(num, startAt) / num;
    }

    private void setCount(double[] arrayOfPrices) {
        price = new double[arrayOfPrices.length];
        int i = 0;
        while (i < arrayOfPrices.length && arrayOfPrices[i] != 0) {
            count = ++i;
        }
        System.arraycopy(arrayOfPrices, 0, price, 0, count);
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

    private String setDecimals(double value, int decimals) {
        return new DecimalFormat("#,###" + ((decimals>0)? ("." + new String(new char[decimals]).replace("\0","0")): "")).format(value);
    }
}
