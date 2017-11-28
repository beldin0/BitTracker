package com.beldin0.android.bittracker2;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;

public class ValueEntry implements Comparable<ValueEntry>, Parcelable {

    private final String pName;
    private final double pValue;
    private final int colour;

    int getColour() {
        return colour;
    }

    public String getName() {
        return pName;
    }

    ValueEntry(String name, double value) {
        this(name, value, Color.WHITE);
    }

    ValueEntry(String name, double value, int colour) {
        pName = name;
        pValue = value;
        this.colour = colour;
    }

    String getValueAsString() {
        return new DecimalFormat("#,###.0000").format(pValue);
    }

    @Override
    public int compareTo(ValueEntry v) {
        if (v.pValue == pValue) return 0;
        return (v.pValue > pValue)? 1 : -1;
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof ValueEntry)) return false;
        ValueEntry ve = (ValueEntry)o;
        return (ve.getName().equals(this.getName()) && ve.getValueAsString().equals(this.getValueAsString()));
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pName);
        dest.writeDouble(pValue);
        dest.writeInt(colour);
    }

    private ValueEntry(Parcel parcel) {
        pName = parcel.readString();
        pValue = parcel.readDouble();
        this.colour = parcel.readInt();
    }

    public static final Parcelable.Creator<ValueEntry> CREATOR
            = new Parcelable.Creator<ValueEntry>() {
        public ValueEntry createFromParcel(Parcel in) {
            return new ValueEntry(in);
        }

        public ValueEntry[] newArray(int size) {
            return new ValueEntry[size];
        }
    };
}
