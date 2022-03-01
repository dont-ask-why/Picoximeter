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

/**
 * Class used to ask for Bluetooth permissions and to enable the bluetooth connection on the device.
 *
 * @author dont-ask-why
 * @version 2022 February 27
 */
public class BluetoothAccess {
    public static final UUID PULOX_SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    public static final UUID BLOOD_PRESSURE_SERVICE_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");
    public static final UUID PULOX_CHARACTERISTIC_UUID = UUID.fromString("d761c8ea-1ac4-11ec-9621-0242ac130002");
    public static final UUID BLOOD_PRESSURE_CHARACTERISTIC_UUID = UUID.fromString("98117140-14e4-49c2-870c-f702edb5fc3d");
    public static final UUID BLOOD_PRESSURE_READ_CHARACTERISTIC_UUID = UUID.fromString("e5cecf22-6947-42be-8d21-1748293a718b");
    public static final ParcelUuid PULOX_SERVICE_PARCEL_UUID = new ParcelUuid(PULOX_SERVICE_UUID);
    public static final ParcelUuid BLOOD_PRESSURE_SERVICE_PARCEL_UUID = new ParcelUuid(BLOOD_PRESSURE_SERVICE_UUID);

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    public static final int PERMISSION_REQUEST_BLE_SCAN = 2;

    /**
     * No constructor needed as everything is static.
     */
    private BluetoothAccess() {}

    /**
     * This method checks for correct permissions to use BLE on Android 12 as well as older versions.
     * The method will return true if all permissions have been set correctly.
     * Please use the parents onRequestPermissionsResult method for final results of asking the user for permissions.
     * @param parent
     * @return true if the permission has been granted, false if it is denied or not set.
     */
    public static boolean checkForBtPermissions(AppCompatActivity parent){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(ContextCompat.checkSelfPermission(parent, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                askForPermission(parent, PERMISSION_REQUEST_BLE_SCAN, parent.shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN));
                return false;
            }
        } else {
            if(ContextCompat.checkSelfPermission(parent, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                askForPermission(parent, PERMISSION_REQUEST_FINE_LOCATION, parent.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION));
                return false;
            }
        }
        return true;
    }

    /**
     * Mathod to create an AlertDialog asking for permissions from the user.
     * @param parent Activity calling this class to ask for permissions.
     * @param request Request code, specified in the class variables.
     * @param isDenied Should be true if the request has previously been denied by the user.
     */
    private static void askForPermission(AppCompatActivity parent, int request, boolean isDenied){
        String[] permissions;
        int title;
        int message;

        switch(request){
            case PERMISSION_REQUEST_BLE_SCAN:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
                    title = R.string.scan_12_permission_title;
                    message = isDenied ? R.string.scan_12_permission_denied_text : R.string.scan_12_permission_text;
                } else {
                    throw new IllegalStateException("Bluetooth Access like this is only needed in Android 12. Ask for Location access if using an earlier version.");
                }
                break;
            case PERMISSION_REQUEST_FINE_LOCATION:
                permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                title = R.string.scan_pos_permission_title;
                message = isDenied ? R.string.scan_pos_permission_denied_text: R.string.scan_pos_permission_text;
                break;
            default:
                throw new IllegalArgumentException("Request not found. Access using class variables of Bluetooth Access.");
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(parent.getText(title));
        builder.setMessage(parent.getText(message));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(dialog -> {
            if(isDenied) {
                parent.onRequestPermissionsResult(
                        request,
                        permissions,
                        new int[]{PackageManager.PERMISSION_DENIED});
            } else {
                parent.requestPermissions(
                        permissions,
                        request);
            }
        });
        builder.show();
    }

    /**
     * Checks if bluetooth is enabled on the phone. If it is not, the parent activity will be finished.
     * @param bluetoothAdapter previously created BluetoothAdapter.
     * @param parent the Activity calling this one.
     */
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
