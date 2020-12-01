package com.example.groots;

public class UsersData {
    String name;
    String id;

    //constructor of user
    UsersData(String _name, String _id){
        name = _name;
        id = _id;
    }

    //returns name for listview
    public String toString(){
        return this.getName();
    }

    //returns id
    public String getId(){
        return this.id;
    }

    //returns name
    public String getName(){
        return this.name;
    }
}