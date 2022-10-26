package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class firebasetestActivity extends AppCompatActivity {

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference cr=fb.collection("tripdata");

    TextView state;

    long[] time=new long[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebasetest);

        state=findViewById(R.id.teststate);
        EditText editText=findViewById(R.id.datatitle);
        findViewById(R.id.startTestbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state.setText("로딩중");
                time[0]= SystemClock.currentThreadTimeMillis();
                getdata(editText.getText().toString());
            }
        });

    }

    public void getdata(String name){
        DocumentReference dr=cr.document(name);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("파이어베이스", "DocumentSnapshot data: " + document.getData());
                    ArrayList<LatLng> tmp= (ArrayList<LatLng>) document.getData().get("route");
                    time[1]=SystemClock.currentThreadTimeMillis();
                    state.setText("로딩완료!: "+tmp.size()+", 걸린시간:"+(time[1]-time[0])/1000.0);
                } else {
                    Log.d("파이어베이스","실패");
                }
            }
        });
    }
}