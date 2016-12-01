package notificationapp.de.notificationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;

/**
 * Created by B1 on 30.11.2016.
 */

public class HashMapAdapter extends BaseAdapter {
    private LinkedHashMap<String, String> mData = new LinkedHashMap<String, String>();
    private String[] mKeys;
    private int mViewId;
    LayoutInflater inflater;

    public HashMapAdapter(Context context, int viewId, LinkedHashMap<String, String> data){
        mData  = data;
        updateKeys();
        mViewId = viewId;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        String key = mKeys[pos];
        String Value = getItem(pos).toString();

        if (convertView == null) convertView = inflater.inflate(mViewId, parent, false);
        TextView label = (TextView) convertView.findViewById(R.id.label);
        label.setText(mData.get(mKeys[pos]));

        return convertView;
    }

    private void updateKeys() {
        mKeys = mData.keySet().toArray(new String[mData.size()]);
        notifyDataSetChanged();
    }

    public void put(String key, String value) {
        mData.put(key, value);
        updateKeys();
    }

    public void clear() {
        mData.clear();
        updateKeys();
    }

    public void remove(String key) {
        mData.remove(key);
        updateKeys();
    }

}
