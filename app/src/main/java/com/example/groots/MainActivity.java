package com.example.groots;

import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static Firebase firebase;

    private ArrayAdapter<String> arrayAdapter;
    public static Bluetooth bluetooth;
    CountDownTimer countDownTimer = null;

    private static final String adminUsername = "admin";
    private static final String adminPassword = "admin";
    protected static boolean adminIsLoggedIn = false;

    private static final int RADIATION_SAFETY_LIMIT = 500000;
    private static final int MAX_REACTOR_OUTPUT = 100;
    private static final int MIN_REACTOR_OUTPUT = 0;
    private static final double BREAK_ROOM_COEFFICIENT = 0.1;
    private static final double CONTROL_ROOM_COEFFICIENT = 0.5;
    private static final double REACTOR_ROOM_COEFFICIENT = 1.6;

    protected static Boolean isLoggedIn = false;
    private static String userId = "49177F35";
    protected static String Id = "49177F35";
    private static int radSafetyLimit = RADIATION_SAFETY_LIMIT;
    private static double exposurePerSecond;
    private static int currentRoomId = 0;
    private NotificationManagerCompat notificationManager;

    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this.getBaseContext());

        setContentView(R.layout.activity_main);
        bluetooth = new Bluetooth(this);
        firebase = new Firebase();

        notificationManager = NotificationManagerCompat.from(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        //initiate HomeFragment in navigation bar
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
               new HomeFragment()).commit();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    bluetooth.cancelDiscovery();
                    if(arrayAdapter != null)
                        arrayAdapter.clear();
                    switch (menuItem.getItemId()){
                        case R.id.navigation_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.navigation_history:
                            selectedFragment = new HistoryFragment();
                            break;
                        case R.id.navigation_admin:
                            selectedFragment = new AdminFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetooth.interrupt();
        unregisterReceiver(bluetoothReceiver);
    }

    final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<String> list = new ArrayList<String>();
            String action = intent.getAction();
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_ON) {
                //Bluetooth is active!
                //mainText.setText("Bluetooth is active!");
                Log.d("Bluetooth", "Bluetooth is active!");
                bluetooth.getPairedDevices();
            } else if (state == BluetoothAdapter.STATE_OFF) {
                //Bluetooth is not active!
                //mainText.setText("Bluetooth is not active!");
                Log.d("Bluetooth", "Bluetooth is inactive!");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //A new device is found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceName != null)
                    list.add(deviceName + " (" + deviceHardwareAddress + ")");
                else
                    list.add(" - (" + deviceHardwareAddress + ")");
                fillBTList(list);
            }   else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connectionLost();
            }
        }
    };
  

    public void fillBTList(final ArrayList<String> list)    {
        ListView listView = findViewById(R.id.BluetoothListView);
        if(arrayAdapter == null)    {
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

            //onClickListener for Bluetooth list menu.
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    bluetooth.choseBluetoothDevice(list, position);
                }
            });
        }   else    {
            for(String listItem : list) {
                arrayAdapter.add(listItem);
            }
        }
        listView.setAdapter(arrayAdapter);
    }

    public void bluetoothConnected()    {
        LinearLayout pairBluetoothView = findViewById(R.id.pairBluetoothView);
        LinearLayout informationView = findViewById(R.id.informationView);
        TextView mainText = findViewById(R.id.mainText);

        pairBluetoothView.setVisibility(View.GONE);
        informationView.setVisibility(View.VISIBLE);

        mainText.setText("Connected, check in!");
        mainText.setVisibility(View.VISIBLE);
    }

    public void connectionLost() {
        final TextView mainText = findViewById(R.id.mainText);
        mainText.setText("You are not connected anymore!");

        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void searchForNewBTDevices(View view) {
        bluetooth.searchForNewBTDevices();
    }

    public void searchForPairedDevices(View view)   {
        bluetooth.getPairedDevices();
    }

    public void messageReceived(String[] parsedMessage){
        TextView roomText = findViewById(R.id.roomText);
        TextView radiationText = findViewById(R.id.radiationText);
        TextView hazmatSuitText = findViewById(R.id.hazmatSuitText);

        //0 == id
        //1 == room
        //2 == hazmat suite
        //3 == radiation
        //4 == logged in
        Log.d("Bluetooth", "id: " + parsedMessage[0]);
        Log.d("Bluetooth", "room: " + parsedMessage[1] );
        Log.d("Bluetooth",  "hazmat suit: " + parsedMessage[2]);
        Log.d("Bluetooth", "radiation: " + parsedMessage[3] );
        Log.d("Bluetooth", "logged in: " + parsedMessage[4]);

        Log.d("Bluetooth", "IsLoggedIn: " +isLoggedIn + " " + currentRoomId + " == " + parsedMessage[1]);
        if(!isLoggedIn || Integer.parseInt(parsedMessage[1]) == currentRoomId) {
            try {
                userId = parsedMessage[0];
                toggleLogin(Integer.parseInt(parsedMessage[4]), Integer.parseInt(parsedMessage[1]));
            } catch (RoomException e) {
                Log.i("Bluetooth", e.getMessage());
            }

            if(countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }

            return;
        }   else if(Integer.parseInt(parsedMessage[1]) != currentRoomId)   {
            currentRoomId = Integer.parseInt(parsedMessage[1]);
            firebase.changeRoom(parsedMessage[0], parsedMessage[1], String.valueOf(RADIATION_SAFETY_LIMIT-radSafetyLimit));
        }

        String room = "";
        switch (parsedMessage[1])   {
            case "1": room = "Break room"; break;
            case "2": room = "Control room"; break;
            case "3": room = "Reactor room"; break;
            default: room = "No room information is available"; break;
        }

        /*try {
            userId = parsedMessage[0];
            toggleLogin(Integer.parseInt(parsedMessage[4]), Integer.parseInt(parsedMessage[1]));
        } catch (RoomException e) {
            Log.i("Bluetooth", e.getMessage());
        }*/

        roomText.setText(room);
        radiationText.setText(parsedMessage[3]);
        hazmatSuitText.setText(parsedMessage[2]);

        int timeLeft = 0;
        try {
            timeLeft = timeLeftInSec(
               Integer.parseInt(parsedMessage[1]),
               Integer.parseInt(parsedMessage[2]),
               Integer.parseInt(parsedMessage[3]));
        } catch (RoomException e) {
            System.out.println(e.getMessage());
        }

        timer(timeLeft);

        byte [] timeBytes = new byte[9];
        timeBytes[0] = 't';
        for (int i = 1; i < String.valueOf(timeLeft).length() + 1; i++) {
            timeBytes[i] = (byte) String.valueOf(timeLeft).charAt(i-1);
        }
        timeBytes[String.valueOf(timeLeft).length() + 1] = '#';
        Log.i("Bluetooth", String.valueOf(timeBytes));
        bluetooth.write(timeBytes);
    }

/**
* Login stuff
* */
    protected void toggleLogin(int login, int roomId) throws RoomException {
        if((login == 1 && !isLoggedIn) || (login == 0 && isLoggedIn)) {
            if (isLoggedIn) {
                firebase.clockOut(userId, String.valueOf(RADIATION_SAFETY_LIMIT-radSafetyLimit));
                isLoggedIn = false;
                final TextView mainText = findViewById(R.id.mainText);
                mainText.setText("You are not logged in anymore!");
                mainText.setTextColor(Color.parseColor("#000000"));

            }   else    {
                String room = "";
                switch (roomId) {
                    case 1: room = "Break room"; break;
                    case 2: room = "Control room"; break;
                    case 3: room = "Reactor room"; break;
                    default: throw new RoomException("Trying to log-in to a room that does not exist");
                }
                firebase.clockIn(userId, room);
                isLoggedIn = true;
            }
        }

    }

    protected static String adminLogin(String userName, String password) {
        if (userName.equals(adminUsername) && password.equals(adminPassword)) {
            adminIsLoggedIn = true;

            return "Login succeeded.";
        }
        else return "Login failed, wrong username or password.";
    }

/**
* Notify stuff
* */
    public void notify(int timeLeftInMinutes)    {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Groots")
                .setContentText(timeLeftInMinutes + " minuter kvar!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1, notification);
    }


/**
* Timer stuff
* */

    public void timer(int timeLeft) {
        final TextView mainText = findViewById(R.id.mainText);

        Log.i("Bluetooth", "timeLeft: " + timeLeft);
        if(countDownTimer == null) {
            countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long totSeconds = millisUntilFinished / 1000;
                    long hours = totSeconds / 3600;
                    long minutes = (totSeconds % 3600) / 60;
                    long seconds = ((totSeconds % 3600) % 60) % 60;
                    mainText.setText(hours + ":" + minutes + ":" + seconds);
                    checkTime((int) totSeconds);
                    radSafetyLimit -= exposurePerSecond;
                }

                public void onFinish() {
                    mainText.setTextColor(Color.parseColor("#FF0000"));
                    Toast.makeText(MainActivity.this, "För hög dos av radioaktivitet!!", Toast.LENGTH_LONG).show();

                    byte[] warning = new byte[]{'W'};
                    bluetooth.write(warning);
                }
            }.start();
        }   else    {
            countDownTimer.cancel();
            countDownTimer = null;
            timer(timeLeft);
        }
    }

    public static int timeLeftInSec(int room, int hazmatSuit, int reactorOutput) throws RoomException {
        int seconds;
        int protection = hazmatSuit == 1 ? 5 : 1;
        double roomCoefficients;

        if(reactorOutput > MAX_REACTOR_OUTPUT)                      //Stabilize reactor output
            reactorOutput = MAX_REACTOR_OUTPUT;
        else if (reactorOutput < MIN_REACTOR_OUTPUT)
            reactorOutput = MIN_REACTOR_OUTPUT;

        switch (room) {
            case 1: roomCoefficients = BREAK_ROOM_COEFFICIENT; break;
            case 2: roomCoefficients = CONTROL_ROOM_COEFFICIENT; break;
            case 3: roomCoefficients = REACTOR_ROOM_COEFFICIENT; break;
            default: throw new RoomException("Unable to calculate room coefficient to a room that does not exist.");
        }

        exposurePerSecond = (reactorOutput * roomCoefficients) / protection;
        seconds = (int) Math.round(radSafetyLimit / exposurePerSecond);

        return seconds;
    }

    public void checkTime(int seconds){
        switch(seconds) {
            case 600: notify(10); break;
            case 300: notify(5); break;
            case 60: notify(1); break;
            case 0: notify(0); break;
        }
    }
}