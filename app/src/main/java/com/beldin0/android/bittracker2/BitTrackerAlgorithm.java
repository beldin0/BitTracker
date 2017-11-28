package com.beldin0.android.bittracker2;

import java.util.ArrayList;

interface BitTrackerAlgorithm {


    /**
     *  compareOrders performs calculations on the price data and determines whether to buy or sell
     * @param dateAndTimeOfMostRecentPricePoint contains the date and time information to be included in the message if desired.
     * @param arrayOfPrices contains the generated price points
     * @return a string with either a buy or sell message, or an empty string ("").
     */
    String compareOrders(String dateAndTimeOfMostRecentPricePoint, double[] arrayOfPrices);

    ArrayList<ValueEntry> getValuesToBeShownInUI();
}
