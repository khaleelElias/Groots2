package com.example.groots;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class WorkedHoursHistoryFragment extends Fragment {
    private ListView listView;
    private Firebase firebase = new Firebase();
    private List<StampHistoryData> stampsHistory = new ArrayList<StampHistoryData>();
    private HistoryAdapter adapter;
    private String userId = "49177F35"; //hardcoded user id
    View view;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.worked_hours_history_layout, container, false);
        listView = (ListView) view.findViewById(R.id.userHistoryListView);

        fetchData();
        return view;
    }

    private void fetchData(){
        adapter = new HistoryAdapter(view.getContext(), stampsHistory);
        listView.setAdapter(adapter);

        firebase.readHistory(userId,stampsHistory,adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StampHistoryData cellData = stampsHistory.get(position);
                Intent intent = new Intent(view.getContext(), ViewDayOfUser.class);

                intent.putExtra("out", cellData.getOutText());
                intent.putStringArrayListExtra("changeTime",  cellData.getChangedTimeList());
                intent.putStringArrayListExtra("changeRoom",  cellData.getChangedRoomList());
                intent.putExtra("radiation",cellData.getRadiation());

                startActivity(intent);
            }
        });
    }

}