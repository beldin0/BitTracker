package com.beldin0.android.bittracker;

import java.util.ArrayList;

/**
 * Created by beldi on 06/11/2017.
 */

public class ValueArray extends ArrayList<ValueEntry> {

    public int getIndex (String name) {
        return super.indexOf(new ValueEntry(name));
    }

    public double getValue (String name) {
        return this.get(getIndex(name)).getValue();
    }
}
