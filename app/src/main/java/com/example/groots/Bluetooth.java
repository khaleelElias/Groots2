package com.example.groots;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Bluetooth  {
    private ConnectBluetooth connectBluetooth = null;
    private CommunicationHandler communicationHandler = null;
    private BluetoothAdapter bluetoothAdapter;
    private MainActivity activity;
    private BluetoothDevice bluetoothDevice;

    private static final int ACCESS_COARSE_LOCATION_CODE = 1;

    /******************** BLUETOOTH CLASS ***********************/
    public Bluetooth(MainActivity _activity) {
        activity = _activity;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    public synchronized void start()    {
        if(connectBluetooth != null)    {
            connectBluetooth.cancel();
            connectBluetooth = null;
        }
        if(communicationHandler != null)    {
            communicationHandler.cancel();
            communicationHandler = null;
        }

        if(bluetoothAdapter == null)    {
            Log.i("Bluetooth", "Bluetooth is not Supported!");
            return;
        }   else if (!bluetoothAdapter.isEnabled())   {
            Log.i("Bluetooth", "Bluetooth is supported but is inactive!");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBTIntent, 1);
        }   else    {
            Log.i("Bluetooth","Bluetooth is active!");
        }
    }

    public void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        final ArrayList<String> list = new ArrayList<String>();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName != null)
                    list.add(deviceName + " (" + deviceHardwareAddress +")");
                else
                    list.add("(" + deviceHardwareAddress + ")");
            }
            activity.fillBTList(list);
        }
    }

    public void searchForNewBTDevices()   {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION))  {
                final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Permission missing");
                alertDialog.setMessage("The app needs permissions to search for new Bluetooth devices!");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_CODE);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_CODE);
        }   else    {
            bluetoothAdapter.startDiscovery();
        }
    }


    public void choseBluetoothDevice(final ArrayList<String> list, final int position)  {
        bluetoothAdapter.cancelDiscovery();

        String deviceAddress = (list.get(position)).split("\\(")[1].split("\\)")[0];
        Log.i("Bluetooth", deviceAddress);

        connectBluetooth = new ConnectBluetooth(deviceAddress);
        connectBluetooth.run();
    }

    private void bluetoothConnected(BluetoothSocket socket, Boolean isConnected) {
        Log.i("Bluetooth", "BluetoothConnected-function. isConnected: " + isConnected);
        if(isConnected) {
            communicationHandler = new CommunicationHandler(socket);
            communicationHandler.start();

            activity.bluetoothConnected();
        }   else    {
            Log.e("Bluetooth", "Not connected, failed to connect!");
        }
    }

    public void write(byte [] bytes) {
        communicationHandler.write(bytes);
    }

    public void cancelDiscovery() {
        if(bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
    }

    public void interrupt() {
        communicationHandler.cancel();
        connectBluetooth.cancel();
    }

    /****************************************************************/
    /******************** CONNECTBLUETOOTH CLASS ********************/
    /****************************************************************/
    private class ConnectBluetooth extends Thread {
        private BluetoothSocket bluetoothSocket = null;

        final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectBluetooth(String deviceAddress)   {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        }

        public void run()   {
            BluetoothSocket tmp = null;
            try {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
                Log.i("Bluetooth", "ConnectBluetooth constructor! Socket: " + tmp);
            } catch (IOException e) {
                Log.i("Bluetooth", "Create bluetoothSocket error: " + e.toString());
                e.printStackTrace();
            }
            bluetoothSocket = tmp;
            //bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                Log.i("Bluetooth", "Bluetooth connected!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Bluetooth", "Connect first Error: " + e.toString());
                try {
                    bluetoothSocket.close();
                } catch (IOException ee) {
                    Log.i("Bluetooth", "Connect second error: " + ee.toString());
                }
            }
            Log.i("Bluetooth", "Bluetooth connected: " + bluetoothSocket.isConnected());
            bluetoothConnected(bluetoothSocket, bluetoothSocket.isConnected());
        }

        public void cancel() {
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /********************************************************************/
    /**************** COMMUNICATIONHANDLER CLASS ************************/
    /********************************************************************/
    private class CommunicationHandler extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] byteBuffer; // mmBuffer store for the stream


        private CommunicationHandler(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = bluetoothSocket.getInputStream();
            }   catch (IOException e)   {
                e.printStackTrace();
            }
            try {
                tmpOut = bluetoothSocket.getOutputStream();
            }   catch (IOException e)   {
                e.printStackTrace();
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run()   {
            byteBuffer = new byte[1024];
            int numBytes;

            while (true)    {
                //Log.i("Bluetooth", "Listening to incoming data!");
                try {
                    if(inputStream.available() > 0) {
                        numBytes = inputStream.read(byteBuffer);
                        Log.i("Bluetooth", "Got numByte: " + numBytes);
                        final String message = new String(byteBuffer, 0, numBytes);
                        Log.i("Bluetooth", "got message: " + message);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    activity.messageReceived(MessageReader.messageParser(message));
                                }   catch (MessageException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        });
                    }   else
                        SystemClock.sleep(50);
                }   catch (IOException e)   {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte [] bytes)    {
            try {
                outputStream.write(bytes);
            }   catch (IOException e)   {
                e.printStackTrace();
            }
        }

        public void cancel()    {
            try {
                bluetoothSocket.close();
            }   catch (IOException e)   {
                e.printStackTrace();
            }
        }
    }
}
