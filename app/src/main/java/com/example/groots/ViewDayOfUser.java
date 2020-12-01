package com.example.groots;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewDayOfUser extends AppCompatActivity {

    String out;
    ArrayList<String> changeTime;
    ArrayList<String> changeRoom;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_day_of_user);
        linearLayout = (LinearLayout) findViewById(R.id.viewDayOfUserLinearLayout);
        Log.d("ViewdayOfUser", "view day of user init");

        out = getIntent().getStringExtra("out");
        changeTime = (ArrayList<String>) getIntent().getSerializableExtra("changeTime");
        changeRoom = (ArrayList<String>) getIntent().getSerializableExtra("changeRoom");

        for(int i = 0; i < changeTime.size(); i++){

            Log.d("ViewdayOfUser",out);
            if(i == changeTime.size() - 1){
                if(out != ""){
                    createCard(changeTime.get(i),out,changeRoom.get(i));
                }else{
                    createCard(changeTime.get(i)," ", changeRoom.get(i));
                }
            }else{
                createCard(changeTime.get(i), changeTime.get(i+1),changeRoom.get(i));
            }
        }

        createCardForRadiation(getIntent().getStringExtra("radiation"));
    }

    //create card for showing time n room
    private void createCard(String from, String to, String room){
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);

        TextView textViewFrom = new TextView(this);
        textViewFrom.setText(from + " - ");

        TextView textViewTo = new TextView(this);
        textViewTo.setText(to);

        TextView textViewRoom = new TextView(this);
        textViewRoom.setText("room: " +room);
        textViewRoom.setGravity(RelativeLayout.ALIGN_PARENT_END);

        box.addView(textViewFrom);
        box.addView(textViewTo);
        box.addView(textViewRoom);

        linearLayout.addView(box);

    }

    private void createCardForRadiation(String radiation){
        TextView rTextView = (TextView) findViewById(R.id.viewDayOfUserRadiation);
        rTextView.setText("Radiation of the day: " + radiation);
    }
}
