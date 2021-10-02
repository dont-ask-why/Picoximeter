package com.picoximeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 */
public class HowToAppFragment extends Fragment {

    public HowToAppFragment() {
        // Required empty public constructor
    }

    public static HowToAppFragment newInstance(){
        HowToAppFragment fragment = new HowToAppFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_how_to_app, container, false);

        Button button = (Button) view.findViewById(R.id.connectButtonFragment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect(button);
            }
        });

        return view;
    }

    public void connect(View view) {
        Intent intent = new Intent(getActivity(), ScanBLEActivity.class);
        startActivity(intent);
    }
}