package com.beldin0.android.bittracker;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter {

    private ArrayList<ValueEntry> source;

    public MyListAdapter(ArrayList<ValueEntry> list) {
        this.source = list;
    }

    @Override
    public int getCount() {
        if (source == null) return 0;
        return source.size();
    }

    @Override
    public Object getItem(int position) {
        return source.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView;

        // if no view is passed in to reuse, inflate a new view from the XML layout file
        if (convertView == null) {
            listItemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item, parent, false);
        } else {
            listItemView = convertView;
        }

        // Get the details of the current word from the words ArrayList
        final ValueEntry currentItem = (ValueEntry)getItem(position);
        if (!(currentItem == null)) {
            // Set the text of TextView
            TextView textView = (TextView) listItemView.findViewById(R.id.name);
            textView.setText(currentItem.getName());
            textView.setBackgroundColor(currentItem.getColour());

            TextView textView2 = (TextView) listItemView.findViewById(R.id.value);
            textView2.setText(String.format("%s", currentItem.getValueAsString()));
            textView2.setBackgroundColor(currentItem.getColour());
        }
        return listItemView;
    }
}
