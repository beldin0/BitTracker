package com.beldin0.android.bittracker;


import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by beldi on 03/11/2017.
 */

public class MyListAdapter extends BaseAdapter {

    private List<List<Pair<String, Double>>> source;

    public MyListAdapter(List list) {
        this.source = list;
    }

    @Override
    public int getCount() {
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
            textView2.setText("" + currentItem.getValueAsString());
            textView2.setBackgroundColor(currentItem.getColour());
        }
        return listItemView;
    }
}
