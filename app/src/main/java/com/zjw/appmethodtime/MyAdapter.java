package com.zjw.appmethodtime;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hasee on 2016/12/23.
 */

public class MyAdapter extends BaseAdapter {

    private static final String debugTag = MyAdapter.class.getName();
    private ArrayList<String> mArrayList;
    private Context mContext;

    public MyAdapter(ArrayList<String> arrayList, Context context) {
        mArrayList = arrayList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = null;
        if (view == null || !(view.getTag() instanceof ViewHolder)) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        return view;
    }

    public void printToDebugLog() {
        if (BuildConfig.DEBUG) {
            Log.d(debugTag, "mArrayList = " + mArrayList);
            Log.d(debugTag, "mContext = " + mContext);
            Log.d(debugTag, "IGNORE_ITEM_VIEW_TYPE = " + IGNORE_ITEM_VIEW_TYPE);
            Log.d(debugTag, "NO_SELECTION = " + NO_SELECTION);
        }
    }

    static class ViewHolder {
        protected ImageView mImage;
        protected TextView mTx;

        ViewHolder(View rootView) {
            initView(rootView);
        }

        private void initView(View rootView) {
            mImage = (ImageView) rootView.findViewById(R.id.image);
            mTx = (TextView) rootView.findViewById(R.id.tx);
        }
    }
}
