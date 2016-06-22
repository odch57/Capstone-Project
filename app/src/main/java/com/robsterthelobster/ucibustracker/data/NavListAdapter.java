package com.robsterthelobster.ucibustracker.data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.robsterthelobster.ucibustracker.R;
import com.robsterthelobster.ucibustracker.data.models.Route;

import java.util.List;

/**
 * Created by robin on 6/21/2016.
 */
public class NavListAdapter extends ArrayAdapter<Route> {

    private List<Route> mRoutes;
    private Context mContext;
    private boolean mNotifyOnChange = true;
    private LayoutInflater mInflater;

    public NavListAdapter(Context context, int resource, List<Route> routes) {
        super(context, resource, routes);
        mRoutes = routes;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mRoutes.size();
    }

    @Override
    public Route getItem(int position) {
        return mRoutes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(Route item) {
        return mRoutes.indexOf(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.nav_route_item,parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.nav_route_name);
            holder.icon = (ImageView) convertView.findViewById(R.id.nav_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Route route = mRoutes.get(position);
        holder.name.setText(route.getName());
        holder.icon.setColorFilter(Color.parseColor(route.getColor()));
        holder.position = position;
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    static class ViewHolder {
        TextView name;
        ImageView icon;
        int position;
    }
}
