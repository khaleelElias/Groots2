package com.example.groots;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ViewUserHistory extends AppCompatActivity {

    private ListView listView;
    private Firebase firebase = new Firebase();
    private String userId = "";
    private List<StampHistoryData> stampsHistory = new ArrayList<StampHistoryData>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_histories);

        listView = (ListView) findViewById(R.id.userHistoryListView);
        userId = getIntent().getStringExtra("userId");

        if(!userId.equals(""))
            fetchData();
        else
            Toast.makeText(this,"the user doesn't exist", Toast.LENGTH_SHORT).show();
    }


    private void fetchData(){
        adapter = new HistoryAdapter(this, stampsHistory);
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