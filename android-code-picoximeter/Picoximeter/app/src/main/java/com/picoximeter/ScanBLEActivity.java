package com.picoximeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author dont-ask-why
 * @version 2022 February 27
 */
public class ScanBLEActivity extends AppCompatActivity {
    private BluetoothLeScanner bluetoothLeScanner;
    private final Activity itself = this;

    private ScanCallback leScanCallback;
    private boolean scanning;
    private final Handler handler = new Handler();

    private static final long SCAN_PERIOD = 10000;

    private HashMap<String, BluetoothDevice> bleDevices;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bleactivity);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getText(R.string.scan_title));

        bleDevices = new HashMap<>();
        listView = findViewById(R.id.scan_ble_listView);

        if(BluetoothAccess.checkForBtPermissions(this)){
            setupWhenPermissionGranted();
        }
    }

    /**
     * Setup which requires bluetooth to be allowed by the user.
     */
    @SuppressLint("MissingPermission")
    private void setupWhenPermissionGranted(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothAccess.checkForBtLeEnabled(bluetoothAdapter,this);
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if(device.getName() != null && result.getScanRecord().getServiceUuids() != null){
                    if(!bleDevices.containsKey(device.getName())){

                        boolean containsUUID = false;
                        for (int i = 0; i < result.getScanRecord().getServiceUuids().size() && !containsUUID; i++) {
                            ParcelUuid uuid = result.getScanRecord().getServiceUuids().get(i);
                            if(uuid.equals(BluetoothAccess.PULOX_SERVICE_PARCEL_UUID)){
                                bleDevices.put(getText(R.string.readings_pulox) + ": " + device.getName(), device);
                                ((ListViewAdapter) listView.getAdapter()).newData(bleDevices.keySet().toArray(new String[0]));
                                containsUUID = true;
                            } else if (uuid.equals(BluetoothAccess.BLOOD_PRESSURE_SERVICE_PARCEL_UUID)){
                                bleDevices.put(getText(R.string.readings_bp) + ": " + device.getName(), device);
                                ((ListViewAdapter) listView.getAdapter()).newData(bleDevices.keySet().toArray(new String[0]));
                                containsUUID = true;
                            }
                        }

                    }
                }
            }
        };

        ListViewAdapter listViewAdapter = new ListViewAdapter(
                this,
                R.layout.device_list_view,
                bleDevices.keySet().toArray(new String[0]));

        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener((adapterView, view, position, duration) -> {
            Intent intent = new Intent(itself, DisplayBLEActivity.class);
            BluetoothDevice device = bleDevices.get(listView.getAdapter().getItem(position));

            Bundle bundle = new Bundle();
            bundle.putParcelable("device", device);
            if(listView.getAdapter().getItem(position).toString().toLowerCase().contains("puls")){
                bundle.putString("type", "PULOX");
            } else {
                bundle.putString("type", "BLOOD_PRESSURE");
            }

            intent.putExtra("extras", bundle);
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            finish();
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == BluetoothAccess.PERMISSION_REQUEST_BLE_SCAN){
            for(int i = 0; i < permissions.length; i++){
                String permission = permissions[i];
                int result = grantResults[i];

                if(permission.equals(Manifest.permission.BLUETOOTH_SCAN) && result == PackageManager.PERMISSION_DENIED){
                    finish();
                }
            }
        } else if (requestCode == BluetoothAccess.PERMISSION_REQUEST_FINE_LOCATION){
            for(int i = 0; i < permissions.length; i++){
                String permission = permissions[i];
                int result = grantResults[i];

                if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) && result == PackageManager.PERMISSION_DENIED){
                    finish();
                }
            }
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onBackPressed() {
        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallback);
        finish();
        super.onBackPressed();
    }

    public void scan(View view){
        scanLeDevice();
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        Button scanButton = (Button) findViewById(R.id.scan_ble_scan_button);
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                if(scanning) {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    scanButton.setEnabled(true);
                    scanButton.setText(getText(R.string.scan_button_enabled));
                    Toast toast = Toast.makeText(this, getText(R.string.scan_toast_stopped), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            scanButton.setEnabled(false);
            scanButton.setText(getText(R.string.scan_button_disabled));
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            scanButton.setEnabled(true);
            scanButton.setText(getText(R.string.scan_button_enabled));
            Toast toast = Toast.makeText(this,getText(R.string.scan_toast_stopped),Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class ListViewAdapter extends ArrayAdapter<String>{
        private String[] myList;

        public ListViewAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
            super(context, resource, objects);
            myList = objects;
        }

        public void newData(String[] objects){
            myList = objects;
            notifyDataSetChanged();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return myList[position];
        }

        @Override
        public int getCount() {
            return myList.length;
        }
    }
}