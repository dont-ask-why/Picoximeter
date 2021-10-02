package com.picoximeter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.picoximeter.data.ReadingDataBlock;

public class MainActivity extends AppCompatActivity {

    private final static String IS_FIRST_START = "com.picoximeter.isFirstStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity itself = this;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(pref.getBoolean(IS_FIRST_START, true)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.start_alert_title));
            builder.setMessage(getText(R.string.start_alert_text));
            builder.setPositiveButton(R.string.start_alert_ok, (dialogInterface, i) -> {
                pref.edit().putBoolean(IS_FIRST_START, false).apply();
                Intent intent = new Intent(itself, HowToActivity.class);
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.start_alert_no, (dialogInterface, i) -> {
                pref.edit().putBoolean(IS_FIRST_START, false).apply();
                // we remain on the page
            });
            builder.show();
        }
    }

    public void connect(View view) {
        Intent intent = new Intent(this, ScanBLEActivity.class);
        startActivity(intent);
    }

    public void imageClick(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dont-ask-why/Picoximeter"));
        startActivity(browserIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu_item:
                Intent intentAbout = new Intent(this, AboutPageActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.how_to_menu_item:
                Intent intentHowTo = new Intent(this, HowToActivity.class);
                startActivity(intentHowTo);
                break;
            case R.id.past_readings_menu_item:
                Intent intentReadings = new Intent(this, ViewReadingsActivity.class);
                startActivity(intentReadings);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}