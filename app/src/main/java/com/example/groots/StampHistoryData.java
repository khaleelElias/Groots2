package com.example.groots;

import java.util.ArrayList;

public class StampHistoryData {
    String inText;
    String dateText;
    String outText;
    ArrayList<String> changedTime;
    ArrayList<String> changedRoom;
    String radiation;


    StampHistoryData(String _inText, String _dateText, String _outText, ArrayList<String> _changedTime, ArrayList<String> _changedRoom, String _radiation){
        inText = _inText;
        dateText = _dateText;
        outText = _outText;
        changedTime = _changedTime;
        changedRoom = _changedRoom;
        radiation = _radiation;
    }

    public String getInText(){
        return this.inText;
    }
    public String getDateText(){
        return this.dateText;
    }

    public String getOutText(){
        return this.outText;
    }

    public ArrayList<String> getChangedTimeList(){
        return this.changedTime;
    }

    public ArrayList<String> getChangedRoomList(){
        return this.changedRoom;
    }

    public String getRadiation(){
        return radiation;
    }


}
