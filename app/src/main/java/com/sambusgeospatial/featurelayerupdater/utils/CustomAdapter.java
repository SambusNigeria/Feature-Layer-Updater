package com.sambusgeospatial.featurelayerupdater.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sambusgeospatial.featurelayerupdater.R;
import com.sambusgeospatial.featurelayerupdater.models.States;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<States> assetArrayList;

    public CustomAdapter(Context context, ArrayList<States> assetArrayList) {

        this.context = context;
        this.assetArrayList = assetArrayList;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return assetArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return assetArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.state_list_item, null, true);

            holder.country = convertView.findViewById(R.id.country);
            holder.name = convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.country.setText(assetArrayList.get(position).getCountry());
        holder.name.setText(assetArrayList.get(position).getName());

        return convertView;
    }

    private class ViewHolder {
        protected TextView country, name;
    }

}