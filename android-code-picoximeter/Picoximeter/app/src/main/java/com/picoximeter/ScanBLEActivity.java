package com.picoximeter;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ScanBLEActivity extends AppCompatActivity {
    static final UUID SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    static final ParcelUuid SERVICE_PARCEL_UUID = new ParcelUuid(SERVICE_UUID);

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Activity itself = this;

    private ScanCallback leScanCallback;
    private boolean scanning;
    private Handler handler = new Handler();

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
                    new StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() > -1) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle(getText(R.string.scan_bt_permission_title));
                            builder.setMessage(getText(R.string.scan_bt_permission_text));
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(dialog -> finish());
                            builder.show();
                        } else {
                            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                        }
                    });

            mLauncher.launch(enableIntent);
        }

        if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.scan_pos_permission_title));
            builder.setMessage(getText(R.string.scan_pos_permission_text));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION));
            builder.show();
        }

        leScanCallback = new ScanCallback() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if(device.getName() != null && result.getScanRecord().getServiceUuids() != null){
                    if(!bleDevices.containsKey(device.getName())){
                        bleDevices.put(device.getName(), device);
                        ((ListViewAdapter) listView.getAdapter()).newData(bleDevices.keySet().toArray(new String[0]));
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
            intent.putExtra("device", bleDevices.get(listView.getAdapter().getItem(position)));
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            finish();
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        scanning = false;
        bluetoothLeScanner.stopScan(leScanCallback);
        finish();
        super.onBackPressed();
    }

    public void scan(View view){
        scanLeDevice();
    }

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

            ArrayList<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setServiceUuid(SERVICE_PARCEL_UUID).build());
            ScanSettings settings = new ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

            bluetoothLeScanner.startScan(filters, settings, leScanCallback);
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