package com.picoximeter;

import static com.picoximeter.BluetoothAccess.BLOOD_PRESSURE_CHARACTERISTIC_UUID;
import static com.picoximeter.BluetoothAccess.BLOOD_PRESSURE_READ_CHARACTERISTIC_UUID;
import static com.picoximeter.BluetoothAccess.BLOOD_PRESSURE_SERVICE_UUID;
import static com.picoximeter.BluetoothAccess.PULOX_CHARACTERISTIC_UUID;
import static com.picoximeter.BluetoothAccess.PULOX_SERVICE_UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.picoximeter.data.ReadingDataBlock;
import com.picoximeter.data.ReadingsViewModel;
import java.util.Objects;

/**
 * @author dont-ask-why
 * @version 2022 February 27
 */
public class DisplayBLEActivity extends AppCompatActivity {
    private final static String TAG = DisplayBLEActivity.class.getSimpleName();
    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGatt  bluetoothGatt;
    private String type;
    private final DisplayBLEActivity itself = this;

    @Override
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_bleactivity);

        Bundle bundle = getIntent().getParcelableExtra("extras");
        device = bundle.getParcelable("device");
        type = bundle.getString("type");

        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Toast toast = Toast.makeText(this,getText(R.string.ble_toast_connected),Toast.LENGTH_SHORT);
        toast.show();
        enableButton(false);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        if(type.equals("PULOX")) {
            fragment = new PuloxDisplayFragment();
            Objects.requireNonNull(getSupportActionBar()).setTitle(getText(R.string.ble_title));
        } else {
            fragment = new BloodPressureDisplayFragment();
            Objects.requireNonNull(getSupportActionBar()).setTitle(getText(R.string.readings_bp));
        }
        ft.replace(R.id.pulox_fragmentContainerView, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        if(characteristic != null){
            bluetoothGatt.setCharacteristicNotification(characteristic, false);
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        finish();
        super.onBackPressed();
    }

    @SuppressLint("MissingPermission")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connection State: Connected");
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Connection State: Not Connected");

                runOnUiThread(() -> {
                    Toast toast = Toast.makeText(itself,getText(R.string.ble_toast_disconnected),Toast.LENGTH_SHORT);
                    toast.show();
                    onBackPressed();
                });
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Connection State: Not successful.");
                gatt.disconnect();
                runOnUiThread(() -> {
                    Toast toast = Toast.makeText(itself,getText(R.string.ble_toast_disconnected),Toast.LENGTH_SHORT);
                    toast.show();
                    onBackPressed();
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(type.equals("PULOX")) {
                characteristic = gatt.getService(PULOX_SERVICE_UUID).getCharacteristic(PULOX_CHARACTERISTIC_UUID);
            } else {
                characteristic = gatt.getService(BLOOD_PRESSURE_SERVICE_UUID).getCharacteristic(BLOOD_PRESSURE_CHARACTERISTIC_UUID);
                readCharacteristic = gatt.getService(BLOOD_PRESSURE_SERVICE_UUID).getCharacteristic(BLOOD_PRESSURE_READ_CHARACTERISTIC_UUID);
            }
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

            onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(!characteristic.getUuid().equals(BLOOD_PRESSURE_READ_CHARACTERISTIC_UUID)) {
                String[] s = characteristic.getStringValue(0).split(";");
                if (type.equals("PULOX")) {
                    updateSpo2Values(s[0], s[1]);
                } else {
                    updateBloodPressureValues(s[0], s[1], s[2]);
                    readCharacteristic.setValue(new byte[]{1});
                    Log.e("CharacteristicSend", bluetoothGatt.writeCharacteristic(readCharacteristic) ? "Sucess" : "Fail");
                }
                runOnUiThread(() -> enableButton(true));
            }
        }
    };

    public void updateSpo2Values(String hr, String  spo2){
        runOnUiThread(() -> {
            ((TextView) findViewById(R.id.display_hr_text_view)).setText(hr);
            ((TextView) findViewById(R.id.display_spo2_text_view)).setText(spo2);
        });
    }

    public void updateBloodPressureValues(String hr, String  systolic, String diastolic){
        runOnUiThread(() -> {
            ((TextView) findViewById(R.id.display_hr_text_view)).setText(hr);
            ((TextView) findViewById(R.id.display_systolic_text_view)).setText(systolic);
            ((TextView) findViewById(R.id.display_diastolic_text_view)).setText(diastolic);
        });
    }

    public void enableButton(boolean enable){
        if(enable != findViewById(R.id.display_ble_save_button).isEnabled()){
            findViewById(R.id.display_ble_save_button).setEnabled(enable);
        }
    }

    public void onSaveClick(View view){
        ReadingsViewModel viewModel = new ViewModelProvider(this).get(ReadingsViewModel.class);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Calendar.getInstance().getTime().getTime());

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.reading_form_layout, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        new PopupWindow();
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm", java.util.Locale.getDefault());
        ((TextView) popupView.findViewById(R.id.form_date_text_view)).setText(sdf.format(calendar));

        EditText text_field = popupView.findViewById(R.id.form_custom_text_field);

        popupView.findViewById(R.id.form_save_button).setOnClickListener(l -> {
            if(text_field.getText().toString().isEmpty()){
                Toast toast = Toast.makeText(this,getText(R.string.form_string_empty),Toast.LENGTH_LONG);
                toast.show();
            } else if(type.equals("PULOX")){
                int hr = Integer.parseInt((String) ((TextView) findViewById(R.id.display_hr_text_view)).getText());
                int spo2 = Integer.parseInt((String) ((TextView) findViewById(R.id.display_spo2_text_view)).getText());

                viewModel.insert(new ReadingDataBlock(
                        calendar.getTime().getTime(),
                        hr,
                        spo2,
                        0,
                        0,
                        text_field.getText().toString(),
                        "PULOX"));
                popupWindow.dismiss();
            } else if(type.equals("BLOOD_PRESSURE")){
                int hr = Integer.parseInt((String) ((TextView) findViewById(R.id.display_hr_text_view)).getText());
                int sys = Integer.parseInt((String) ((TextView) findViewById(R.id.display_systolic_text_view)).getText());
                int dia = Integer.parseInt((String) ((TextView) findViewById(R.id.display_diastolic_text_view)).getText());

                viewModel.insert(new ReadingDataBlock(
                        calendar.getTime().getTime(),
                        hr,
                        0,
                        sys,
                        dia,
                        text_field.getText().toString(),
                        "BLOOD_PRESSURE"));
                popupWindow.dismiss();
            }
        });
    }
}