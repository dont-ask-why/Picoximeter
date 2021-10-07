package com.picoximeter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.UUID;

public class BluetoothAccess {
    public static final UUID SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("d761c8ea-1ac4-11ec-9621-0242ac130002");
    public static final ParcelUuid SERVICE_PARCEL_UUID = new ParcelUuid(SERVICE_UUID);

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSION_REQUEST_BLE_SCAN = 2;

    private BluetoothAccess() {}

    public static void checkForBtPermissions(AppCompatActivity parent){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(ContextCompat.checkSelfPermission(parent, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if(parent.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                    builder.setTitle(parent.getText(R.string.scan_12_permission_title));
                    builder.setMessage(parent.getText(R.string.scan_12_permission_denied_text));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog ->
                            parent.onRequestPermissionsResult(
                                    PERMISSION_REQUEST_BLE_SCAN,
                                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                    new int[]{PackageManager.PERMISSION_DENIED}));
                    builder.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                    builder.setTitle(parent.getText(R.string.scan_12_permission_title));
                    builder.setMessage(parent.getText(R.string.scan_12_permission_text));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog ->
                            parent.requestPermissions(
                                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                                    PERMISSION_REQUEST_BLE_SCAN));
                    builder.show();
                }
            }
        } else {
            if(ContextCompat.checkSelfPermission(parent, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(parent.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                    builder.setTitle(parent.getText(R.string.scan_pos_permission_title));
                    builder.setMessage(parent.getText(R.string.scan_pos_permission_denied_text));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog ->
                            parent.onRequestPermissionsResult(
                                    PERMISSION_REQUEST_FINE_LOCATION,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    new int[]{PackageManager.PERMISSION_DENIED}));
                    builder.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                    builder.setTitle(parent.getText(R.string.scan_pos_permission_title));
                    builder.setMessage(parent.getText(R.string.scan_pos_permission_text));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                        parent.requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_FINE_LOCATION);
                        });
                    builder.show();
                }
            }
        }
    }

    public static void checkForBtLeEnabled(BluetoothAdapter bluetoothAdapter, AppCompatActivity parent){
        boolean enabled = (bluetoothAdapter != null && bluetoothAdapter.isEnabled());

        if(!enabled) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            ActivityResultLauncher<Intent> mLauncher = parent.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() > -1) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                            builder.setTitle(parent.getText(R.string.scan_bt_permission_title));
                            builder.setMessage(parent.getText(R.string.scan_bt_permission_text));
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(dialog -> parent.finish());
                            builder.show();
                        } else {

                        }
                    });
            mLauncher.launch(enableIntent);
        }
    }
}
