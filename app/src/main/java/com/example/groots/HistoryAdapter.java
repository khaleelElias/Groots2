package com.example.groots;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class HistoryAdapter extends ArrayAdapter<StampHistoryData> {

    private Activity context;
    private List<StampHistoryData> data;

    public HistoryAdapter(@NonNull Context context, List<StampHistoryData> data) {
        super(context, R.layout.row_item ,data);

        this.context = (Activity) context;
        this.data = data;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.row_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();

            //ListView cell variables
            viewHolder.inText = (TextView) view.findViewById(R.id.inText);
            viewHolder.dateText = (TextView) view.findViewById(R.id.dateText);
            viewHolder.outText = (TextView) view.findViewById(R.id.outText);

            view.setTag(viewHolder);
        }

        StampHistoryData cellData = (StampHistoryData) getItem(position);

        ((ViewHolder)view.getTag()).inText.setText(cellData.getInText());
        ((ViewHolder)view.getTag()).dateText.setText(cellData.getDateText());
        ((ViewHolder)view.getTag()).outText.setText(cellData.getOutText());

        return view;

    }

    //ListView cell holder
    public class ViewHolder{
        private TextView inText;
        private TextView dateText;
        private TextView outText;
    }

}
