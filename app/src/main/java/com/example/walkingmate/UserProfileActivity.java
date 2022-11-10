package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class UserProfileActivity extends AppCompatActivity {

    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent getIntent=getIntent();
        userid=getIntent.getStringExtra("userid");
    }
}