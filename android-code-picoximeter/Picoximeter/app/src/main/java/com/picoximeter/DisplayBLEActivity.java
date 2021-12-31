package com.picoximeter;

import static com.picoximeter.BluetoothAccess.CHARACTERISTIC_UUID;
import static com.picoximeter.BluetoothAccess.SERVICE_UUID;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.concurrent.Executors;

/**
 * @author dont-ask-why
 * @version 2021 December 31
 */
public class DisplayBLEActivity extends AppCompatActivity {
    private final static String TAG = DisplayBLEActivity.class.getSimpleName();
    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic;
    BluetoothGatt  bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_bleactivity);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getText(R.string.ble_title));

        device = getIntent().getParcelableExtra("device");
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        Toast toast = Toast.makeText(this,getText(R.string.ble_toast_connected),Toast.LENGTH_SHORT);
        toast.show();
        enableButton(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

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

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connection State: Connected");
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Connection State: Not Connected");
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Connection State: Not successful.");
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
            runOnUiThread(() -> enableButton(true));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String[] s = characteristic.getStringValue(0).split(";");
            System.out.println(s);
            updateValues(s[0], s[1]);
        }
    };

    public void updateValues(String hr, String  spo2){
        ((TextView) findViewById(R.id.display_hr_text_view)).setText(hr);
        ((TextView) findViewById(R.id.display_spo2_text_view)).setText(spo2);
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
        int hr = Integer.parseInt((String) ((TextView) findViewById(R.id.display_hr_text_view)).getText());
        int spo2 = Integer.parseInt((String) ((TextView) findViewById(R.id.display_spo2_text_view)).getText());

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
        ((TextView) popupView.findViewById(R.id.form_hr_text_view)).setText(String.format(
                String.valueOf(getText(R.string.past_hr)), hr));
        ((TextView) popupView.findViewById(R.id.form_spo2_text_view)).setText(String.format(
                String.valueOf(getText(R.string.past_spo2)), spo2));

        EditText text_field = popupView.findViewById(R.id.form_custom_text_field);

        popupView.findViewById(R.id.form_save_button).setOnClickListener(l -> {
            if(text_field.getText().toString().isEmpty()){
                Toast toast = Toast.makeText(this,getText(R.string.form_string_empty),Toast.LENGTH_LONG);
                toast.show();
            } else {
                viewModel.insert(new ReadingDataBlock(
                        calendar.getTime().getTime(),
                        hr,
                        spo2,
                        text_field.getText().toString()));
                popupWindow.dismiss();
            }
        });
    }
}