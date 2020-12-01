package com.example.groots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RadiationExposureHistoryFragment extends Fragment {
    private View view;

    private Firebase firebase = new Firebase();

    private List<StampHistoryData> stampHistory = new ArrayList<>();
    private HistoryAdapter adapter;
    private ListView listView;
    private String userId = "49177F35"; //hardcoded user

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.radiation_exposure_history_layout, container, false);
        listView = (ListView) view.findViewById(R.id.radiationExposureList);
        fetchData();

        return view;
    }

    public void fetchData(){
        adapter = new HistoryAdapter(view.getContext(),stampHistory);
        listView.setAdapter(adapter);

        firebase.readRadiationExposureHistory(userId,stampHistory,adapter);

    }



}
