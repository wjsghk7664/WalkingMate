package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;

public class background_test_Activity extends AppCompatActivity {

    Button start,stop;

    int step=0;

    TextView steps,state;

    boolean checkservice;
    boolean sendbool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_test);

        start=findViewById(R.id.startService);
        stop=findViewById(R.id.stopService);

        steps=findViewById(R.id.steps);
        state=findViewById(R.id.state);

        checkservice=false;
        sendbool=false;

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkservice){
                    checkservice=true;
                    state.setText("start");
                }

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkservice){
                    checkservice=false;
                    steps.setText("걸음수: 0");
                    state.setText("stop");
                }

            }
        });



    }

    @Override
    protected void onPause() {
        super.onPause();
        sendbool=true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        sendbool=false;

    }
}