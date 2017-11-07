package com.beldin0.android.bittracker;

import android.graphics.Color;

import java.text.DecimalFormat;

public class ValueEntry implements Comparable {

    private String pName;
    private double pValue;
    private int colour;

    public double getValue() {
        return pValue;
    }

    public int getColour() {
        return colour;
    }

    public String getName() {
        return pName;
    }

    public ValueEntry(String name) {
        this(name, 0.00, Color.WHITE);
    }

    public ValueEntry(String name, double value) {
        this(name, value, Color.WHITE);
    }

    public ValueEntry(String name, double value, int colour) {
        pName = name;
        pValue = value;
        this.colour = colour;
    }

    public String getValueAsString() {
        return new DecimalFormat("#,###.0000").format(pValue);
    }

    @Override
    public int compareTo(Object o) {
        ValueEntry v = (ValueEntry) o;
        if (v.pValue == pValue) return 0;
        return (v.pValue > pValue)? 1 : -1;
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof ValueEntry)) return false;
        ValueEntry ve = (ValueEntry)o;
        return ve.getName().equals(this.getName());
    }
}
