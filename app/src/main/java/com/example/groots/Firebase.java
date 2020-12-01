package com.example.groots;

import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Firebase {

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    /*********************************************************
     *
     * Clock in
     *
     ********************************************************/
    public void clockIn(final String id, final String room){
        Log.d("Firebase","clock in init");
        //get date n time
        final String currentDate = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(new Date());
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        db.collection(id).document(currentDate)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.getString("In") == null)
                            performClockIn(currentDate,currentTime,id,room);
                    } else {
                        performClockIn(currentDate,currentTime,id,room);
                    }
                } else {
                    Log.d("Firebase", "get failed with ", task.getException());
                }
            }
        });
    }

    private void performClockIn(String currentDate, String currentTime, String id, String room){
        Log.d("Firebase","clockinVerified init");

        //map to stamp
        Map<String, Object> stamp = new HashMap<>();
        stamp.put("In", currentTime);
        stamp.put("Change", Arrays.asList(currentTime));
        stamp.put("ChangedRoom", Arrays.asList(room));
        stamp.put("Out", "");
        stamp.put("radiation", "0");

        //send user to firebase
        db.collection(id).document(currentDate)
                .set(stamp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Firebase", "Clocked in");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "document couldn't be created");
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d("Firebase", "document was cancelled");
            }
        });
    }

    /*********************************************************
     *
     * clock out
     *
     ********************************************************/
    public void clockOut(final String id, final String radiation){
        Log.d("Firebase","clocked out initiated");

        //get date n time
        final String currentDate = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(new Date());
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        db.collection(id).document(currentDate).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    Log.d("Firebase", " document: " + doc.getData());
                    String currentRadiation = doc.getString("radiation");
                    String out = doc.getString("Out");
                    Log.d("Firebase", "out" + out);
                    currentRadiation = String.valueOf(Integer.parseInt(radiation) + Integer.parseInt(currentRadiation));

                    if(!out.isEmpty()){
                        Log.d("Firebase", "user "+ id +" have already stamped out!");
                    }else{
                        performClockout(id,currentDate,currentTime,currentRadiation);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "Failed to clock out: " + e.getMessage());
            }
        });

    }
    private void performClockout(final String id, String date , String time, String radiation){

        Map<String, Object> stamp = new HashMap<>();
        stamp.put("Out", time);
        stamp.put("radiation",radiation);

        //send user to firebase
        db.collection(id).document(date)
                .update(stamp).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Firebase", "clocked out!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", e.getMessage());
            }
        });
    }

    /*********************************************************
     *
     * Read history
     *
     ********************************************************/
    public void readHistory(final String id, final List<StampHistoryData> stampHistory, final ArrayAdapter<StampHistoryData> adapter) {

        db.collection(id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty())
                    return;

                for(QueryDocumentSnapshot document: task.getResult()){
                    String date = document.getId();
                    String in = String.valueOf(document.get("In"));
                    String out = String.valueOf(document.get("Out"));
                    ArrayList<String> changedTime = new ArrayList<>();
                    ArrayList<String> changedRoom = new ArrayList<>();
                    String radiation = String.valueOf(document.get("radiation"));

                    for(Object obj: (List<Object>) document.get("Change")) {
                        Log.d("ViewUserHistory", obj.toString());
                        changedTime.add(String.valueOf(obj));
                    }

                    for(Object obj: (List<Object>) document.get("ChangedRoom")){
                        Log.d("ViewUserHistory", obj.toString());
                        changedRoom.add(String.valueOf(obj));
                    }

                    stampHistory.add(0,new StampHistoryData(in,date,out,changedTime,changedRoom,radiation));
                }
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Firebase",e.getMessage());
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.d("Firebase","Cancelled call");
                }
        });
    }
    /*********************************************************
     *
     * read radiation exposure history
     *
     *******************************************************/
    public void readRadiationExposureHistory(String id, final List<StampHistoryData> exposureList, final ArrayAdapter<StampHistoryData> adapter){
        db.collection(id).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        exposureList.add(new StampHistoryData(
                                String.valueOf(doc.get("radiation")),
                                doc.getId(),
                                "",
                                null,
                                null,
                                null
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "Couldn't read radiation exposure history: " + e.getMessage());
            }
        });
    }
    /*********************************************************
     *
     * change room
     *
     ********************************************************/
    public void changeRoom(final String id, final String room, final String radiation){
        Log.d("Firebase", "change room initiated");
        final String currentDate = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(new Date());
        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        db.collection(id).document(currentDate)
            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().get("Out") != "")
                    return;

                String currentRadiation = task.getResult().getString("radiation");
                currentRadiation = String.valueOf(Integer.parseInt(radiation) + Integer.parseInt(currentRadiation));
                List<Object> timeArray = (List<Object>) task.getResult().get("Change");
                List<Object> roomArray = (List<Object>) task.getResult().get("ChangedRoom");

                timeArray.add(currentTime);
                roomArray.add(room);



                performChangeRoom(id, timeArray, roomArray, currentRadiation, currentDate);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase","error getting document "+ id);
            }
        });
    }

    private void performChangeRoom(String id, List<Object> times, List<Object> rooms, String radiation, String currentDate){
        Log.d("Firebase", "PerformChangeRoom init");

        Map<String, Object> stamp = new HashMap<>();
        stamp.put("Change", times);
        stamp.put("ChangedRoom",rooms);
        stamp.put("radiation", radiation);

        db.collection(id).document(currentDate).update(stamp)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Firebase","changed room successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Firebase","problem changing room: " + e.getMessage());
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.d("Firebase", "Changing room cancelled");
                }
        });
    }

    /*********************************************************
     *
     * read all users
     *
     *******************************************************/
public void readAllUsers(final List<UsersData> users, final ArrayAdapter<UsersData> adapter){
        db.collection("users").get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for(QueryDocumentSnapshot doc: task.getResult()){
                        users.add(new UsersData( doc.get("name").toString(),doc.getId()));
                    }

                    adapter.notifyDataSetChanged();

                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "error retreiving all collections " + e.getMessage());
            }
        });
    }



    /*********************************************************
     *
     * create user
     * Adming create user if we have time.
     ********************************************************/
    public void createUser(String id, String name){
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);


        db.collection("users").document(id).set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Firebase", "success creating user");
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "error creating user: " + e.getMessage());
            }
        });
    }

}
