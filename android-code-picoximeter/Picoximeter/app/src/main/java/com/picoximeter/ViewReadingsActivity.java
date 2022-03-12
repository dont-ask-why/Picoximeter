package com.picoximeter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.picoximeter.data.ReadingDataBlock;
import com.picoximeter.data.ReadingsViewModel;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author dont-ask-why
 * @version 2022 February 27
 */
public class ViewReadingsActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private FloatingActionButton fabSort;
    private FloatingActionButton fabDeleteAll;
    private FloatingActionButton fabDeleteSingle;
    private FloatingActionButton fabEdit;
    private boolean isFabOpen = true;

    private boolean filterIsAsc = false;
    private boolean isTagFilterEnabled = false;
    private long filterSmallestDate = 0;
    private long filterLargestDate = Calendar.getInstance().getTimeInMillis();
    private String[] filterTags = new String[0];
    private String[] filterTypes = new String[]{"BLOOD_PRESSURE", "PULOX"};

    private volatile List<String> LIST_TAGS;
    private long SMALLEST_DATE;

    private ReadingsViewModel viewModel;
    private ListViewAdapter listViewAdapter;
    private ListView listView;

    private ReadingDataBlock currentlySelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_readings);

        listView = findViewById(R.id.readings_list);
        viewModel = new ViewModelProvider(this).get(ReadingsViewModel.class);

        listViewAdapter = new ListViewAdapter(
                this,
                R.layout.reading_list_view,
                new ReadingDataBlock[]{new ReadingDataBlock(42, 0, 0, 0, 0, "No Data", "INVALID")});

        LIST_TAGS = new ArrayList<>();
        LIST_TAGS.add("No tags available");
        viewModel.getTags().observe(this, tags -> {
            LIST_TAGS = tags;
            if(!isTagFilterEnabled){
                filterTags = tags.toArray(new String[0]);
                updateObserver();
            }
        });

        SMALLEST_DATE = 0;
        viewModel.getSmallestID().observe(this, id -> {
            if(id == null){
                SMALLEST_DATE = 0;
            } else {
                SMALLEST_DATE = id;
            }
            updateObserver();
        });

        viewModel.getFilteredReadings(filterIsAsc, filterSmallestDate, filterLargestDate, filterTags, filterTypes).observe(this, readings -> {
            listViewAdapter.newData(readings.toArray(new ReadingDataBlock[0]));
        });

        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener((adapterView, view, position, duration) -> {
            if(currentlySelected == null){
                currentlySelected = (ReadingDataBlock) listView.getItemAtPosition(position);
                listView.setItemChecked(position, true);
                fabDeleteSingle.setEnabled(true);
                fabEdit.setEnabled(true);
            } else if (currentlySelected == listView.getItemAtPosition(position)){
                currentlySelected = null;
                fabDeleteSingle.setEnabled(false);
                fabEdit.setEnabled(false);
                listView.clearChoices();
                listView.requestLayout();
            } else {
                currentlySelected = (ReadingDataBlock) listView.getItemAtPosition(position);
                listView.clearChoices();
                listView.setItemChecked(position, true);
            }
        });

        fab = findViewById(R.id.past_fab_more_options);
        fabSort = findViewById(R.id.past_fab_sort);
        fabEdit = findViewById(R.id.past_fab_edit);
        fabDeleteAll = findViewById(R.id.past_fab_delete_all);
        fabDeleteSingle = findViewById(R.id.past_fab_delete_single);
        fab.setOnClickListener(view -> {
            if(!isFabOpen){
                openFabMenu();
            } else {
                closeFabMenu();
            }
        });

        fab.performClick();
        fabEdit.setEnabled(false);
        fabDeleteSingle.setEnabled(false);
    }

    /**
     * Simple call for observer to restart search with class variables as filters.
     */
    private void updateObserver(){
        setNewFilter(filterIsAsc, filterSmallestDate, filterLargestDate, filterTags, filterTypes);
    }

    /**
     * Call for observer to update filter with new filter properties.
     * @param filterIsAsc contains if the data is sorted by date ascending or descending.
     * @param filterSmallestDate using unix time, what is the smallest possible date to be shown.
     * @param filterLargestDate using unix time, what is the largest possible date to be shown.
     * @param filterTags what tags should the results contain, should be a String[0] for all entries.
     */
    private void setNewFilter(boolean filterIsAsc, long filterSmallestDate, long filterLargestDate, String[] filterTags, String[] filterTypes){
        if(viewModel.getFilteredReadings(this.filterIsAsc, this.filterSmallestDate, this.filterLargestDate, this.filterTags, this.filterTypes).hasObservers()){
            viewModel.getReadings().removeObservers(this);
        }

        this.filterIsAsc = filterIsAsc;
        this.filterSmallestDate = filterSmallestDate;
        this.filterLargestDate = filterLargestDate;
        this.filterTypes = filterTypes;

        viewModel.getFilteredReadings(filterIsAsc, filterSmallestDate, filterLargestDate, filterTags, filterTypes).observe(this, readings -> {
            listViewAdapter.newData(readings.toArray(new ReadingDataBlock[0]));
        });
    }

    /**
     * Opens the Buttons for the Floating Action Button Menu.
     */
    private void openFabMenu(){
        isFabOpen=true;
        fabSort.animate().translationY(0).alpha(1.0f);
        findViewById(R.id.past_textView_sort).animate().translationY(0).alpha(1.0f);
        fabDeleteAll.animate().translationY(0).alpha(1.0f);
        findViewById(R.id.past_textView_del_all).animate().translationY(0).alpha(1.0f);
        fabDeleteSingle.animate().translationY(0).alpha(1.0f);
        findViewById(R.id.past_textView_del_single).animate().translationY(0).alpha(1.0f);
        fabEdit.animate().translationY(0).alpha(1.0f);
        findViewById(R.id.past_textView_edit).animate().translationY(0).alpha(1.0f);
    }

    /**
     * Closes the Buttons for the Floating Action Button Menu.
     */
    private void closeFabMenu(){
        isFabOpen=false;
        fabSort.animate().translationY(getResources().getDimension(R.dimen.standard_fab1)).alpha(0.0f);
        findViewById(R.id.past_textView_sort).animate().translationY(getResources().getDimension(R.dimen.standard_fab1)).alpha(0.0f);

        fabDeleteAll.animate().translationY(getResources().getDimension(R.dimen.standard_fab2)).alpha(0.0f);
        findViewById(R.id.past_textView_del_all).animate().translationY(getResources().getDimension(R.dimen.standard_fab2)).alpha(0.0f);

        fabDeleteSingle.animate().translationY(getResources().getDimension(R.dimen.standard_fab3)).alpha(0.0f);
        findViewById(R.id.past_textView_del_single).animate().translationY(getResources().getDimension(R.dimen.standard_fab3)).alpha(0.0f);

        fabEdit.animate().translationY(getResources().getDimension(R.dimen.standard_fab4)).alpha(0.0f);
        findViewById(R.id.past_textView_edit).animate().translationY(getResources().getDimension(R.dimen.standard_fab4)).alpha(0.0f);

        fab.bringToFront();
    }

    /**
     * When an edit is called, a layout will be shown to edit the entry.
     * @param view links to the calling view.
     */
    public void onEditClick(View view){
        if(currentlySelected != null){
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

            Calendar c = currentlySelected.getCalender();
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm", java.util.Locale.getDefault());
            ((TextView) popupView.findViewById(R.id.form_date_text_view)).setText(sdf.format(c));

            EditText text_field = popupView.findViewById(R.id.form_custom_text_field);
            text_field.setText(currentlySelected.getTag());

            popupView.findViewById(R.id.form_save_button).setOnClickListener(l -> {
                if (text_field.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText(this, getText(R.string.form_string_empty), Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    if(currentlySelected.getType().equals("PULOX")) {
                        viewModel.update(new ReadingDataBlock(
                                currentlySelected.getId(),
                                currentlySelected.getHr(),
                                currentlySelected.getSpo2(),
                                0,
                                0,
                                text_field.getText().toString(),
                                currentlySelected.getType()));
                    } else if (currentlySelected.getType().equals("BLOOD_PRESSURE")){
                        viewModel.update(new ReadingDataBlock(
                                currentlySelected.getId(),
                                currentlySelected.getHr(),
                                0,
                                currentlySelected.getSystolic(),
                                currentlySelected.getDiastolic(),
                                text_field.getText().toString(),
                                currentlySelected.getType()));
                    }
                    runOnUiThread(() -> {
                        Toast toast = Toast.makeText(this,getText(R.string.data_edit_saved),Toast.LENGTH_LONG);
                        toast.show();
                    });
                    popupWindow.dismiss();
                }
            });

            listView.clearChoices();
            closeFabMenu();
        }
    }

    /**
     * Button click to sort the entries in the list in a different way by using an opened layout.
     * @param view reference to the calling view
     */
    public void onSortClick(View view){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.reading_sort_layout, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        //new PopupWindow();
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        String[] data = new String[]{getString(R.string.sort_spinner_asc), getString(R.string.sort_spinner_desc)};
        Spinner spinner = popupView.findViewById(R.id.sort_spinner);
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(filterIsAsc?0:1);

        CheckBox cbPulox = (CheckBox) popupView.findViewById(R.id.checkBox_pulseoximeter);
        CheckBox cbBP = (CheckBox) popupView.findViewById(R.id.checkBox_bloodPressure);

        for(String s : filterTypes){
            if(s.equals("PULOX")){
                cbPulox.setChecked(true);
            } else if (s.equals("BLOOD_PRESSURE")){
                cbBP.setChecked(true);
            }
        }

        DatePicker pickerOldest = popupView.findViewById(R.id.datePickerOldest);
        Calendar oldestDate = Calendar.getInstance();
        oldestDate.setTimeInMillis(filterSmallestDate);
        pickerOldest.updateDate(oldestDate.get(Calendar.YEAR), oldestDate.get(Calendar.MONTH), oldestDate.get(Calendar.DAY_OF_MONTH));
        pickerOldest.setMinDate(SMALLEST_DATE);

        DatePicker pickerNewest = popupView.findViewById(R.id.datePickerNewest);
        Calendar newestDate = Calendar.getInstance();
        newestDate.setTimeInMillis(filterLargestDate);
        pickerNewest.updateDate(newestDate.get(Calendar.YEAR), newestDate.get(Calendar.MONTH), newestDate.get(Calendar.DAY_OF_MONTH));
        pickerNewest.setMinDate(SMALLEST_DATE);

        ListView tagList = popupView.findViewById(R.id.sort_tag_listView);
        ArrayList<String> checkedItems = new ArrayList<>();
        tagList.setAdapter(new ArrayAdapter<>(this, R.layout.device_list_view, LIST_TAGS.toArray(new String[0])));

        tagList.setOnItemClickListener((adapterView, view1, position, duration) -> {
            if(checkedItems.contains((String) tagList.getItemAtPosition(position))){
                tagList.setItemChecked(position, false);
                checkedItems.remove((String) tagList.getItemAtPosition(position));
            } else {
                tagList.setItemChecked(position, true);
                checkedItems.add((String) tagList.getItemAtPosition(position));
            }
        });

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.sort_save_button).setOnClickListener(l ->{
            isTagFilterEnabled = !(checkedItems.size() == 0 || checkedItems.size() == LIST_TAGS.size());

            boolean pulseoximeter = cbPulox.isChecked();
            boolean bloodPressure = cbBP.isChecked();
            String[] types;
            if(pulseoximeter && bloodPressure){
                types = new String[]{"BLOOD_PRESSURE", "PULOX"};
            } else if (pulseoximeter) {
                types = new String[]{"PULOX"};
            } else if (bloodPressure) {
                types = new String[]{"BLOOD_PRESSURE"};
            } else {
                types = new String[0];
            }

            setNewFilter(
                    spinner.getSelectedItemPosition() == 0,
                    new GregorianCalendar(pickerOldest.getYear(), pickerOldest.getMonth(), pickerOldest.getDayOfMonth(), 0, 0, 0).getTimeInMillis(),
                    new GregorianCalendar(pickerNewest.getYear(), pickerNewest.getMonth(), pickerNewest.getDayOfMonth(), 23, 59, 59).getTimeInMillis(),
                    isTagFilterEnabled?checkedItems.toArray(new String[0]):LIST_TAGS.toArray(new String[0]),
                    types
            );
            popupWindow.dismiss();
        });
    }

    /**
     * Button click to delete all entries.
     * @param view reference to the calling view
     */
    public void onDeleteAllClick(View view){
        viewModel.deleteAll();
        listView.clearChoices();
        closeFabMenu();
    }

    /**
     * Button click to delete the currently selected entry only.
     * @param view reference to the calling view
     */
    public void onDeleteSingleClick(View view){
        if(currentlySelected != null){
            viewModel.delete(currentlySelected);
            currentlySelected = null;
            listView.clearChoices();
            closeFabMenu();
        }
    }

    /**
     * ListViewAdapter to generate the list of entries properly.
     */
    private class ListViewAdapter extends ArrayAdapter<ReadingDataBlock> {
        private ReadingDataBlock[] myList;
        private final int resource;

        public ListViewAdapter(@NonNull Context context, int resource, @NonNull ReadingDataBlock[] objects) {
            super(context, resource, objects);
            this.resource = resource;
            myList = objects;
        }

        public void newData(ReadingDataBlock[] objects){
            myList = objects;
            notifyDataSetChanged();
        }

        @Nullable
        @Override
        public ReadingDataBlock getItem(int position) {
            return myList[position];
        }

        @Override
        public int getCount() {
            return myList.length;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ReadingDataBlock reading = getItem(position);

            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);
            }

            TextView date = convertView.findViewById(R.id.readings_view_date);
            Calendar c = reading.getCalender();
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm", java.util.Locale.getDefault());
            date.setText(sdf.format(c));
            TextView hr = convertView.findViewById(R.id.readings_view_hr);
            hr.setText(String.format((String) getText(R.string.past_hr), reading.getHr()));
            TextView tag = convertView.findViewById(R.id.readings_view_tag);
            tag.setText(reading.getTag());

            if(reading.getType().equals("PULOX")) {
                TextView spo2 = convertView.findViewById(R.id.readings_view_second);
                spo2.setText(String.format((String) getText(R.string.past_spo2), reading.getSpo2()));
                ((TextView) convertView.findViewById(R.id.readings_view_second_name)).setText(R.string.ble_spo2);

                ((TextView) convertView.findViewById(R.id.readings_view_third_name)).setText("");

                ((TextView) convertView.findViewById(R.id.readings_view_type)).setText(R.string.readings_pulox);
            } else if (reading.getType().equals("BLOOD_PRESSURE")){
                TextView systolic = convertView.findViewById(R.id.readings_view_second);
                systolic.setText(String.format((String) getText(R.string.past_bp), reading.getSystolic()));
                ((TextView) convertView.findViewById(R.id.readings_view_second_name)).setText(R.string.bp_systolic);

                TextView diastolic = convertView.findViewById(R.id.readings_view_third);
                diastolic.setText(String.format((String) getText(R.string.past_bp), reading.getDiastolic()));
                ((TextView) convertView.findViewById(R.id.readings_view_third_name)).setText(R.string.bp_diastolic);

                ((TextView) convertView.findViewById(R.id.readings_view_type)).setText(R.string.readings_bp);
            }

            return convertView;
        }
    }
}