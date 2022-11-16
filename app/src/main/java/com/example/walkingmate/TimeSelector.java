package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

public class TimeSelector extends AppCompatActivity {

    int mHour=0,mMinute=0;

    TimePicker time_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selector);

        time_picker=findViewById(R.id.time_picker);
        time_picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                mHour=i;
                mMinute=i1;
            }
        });


    }

    public void mtimeOnClick(View v){

        Intent intent = new Intent();

        intent.putExtra("mHour",mHour);

        intent.putExtra("mMinute", mMinute);

        setResult(RESULT_OK, intent);

        finish();

    }

}