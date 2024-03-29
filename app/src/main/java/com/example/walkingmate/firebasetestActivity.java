package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class firebasetestActivity extends AppCompatActivity {

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference cr=fb.collection("tripdata");
    CollectionReference crl=fb.collection("tripdatalist");

    TextView state,seldocument;

    long[] time=new long[2];

    long[] listtime=new long[2];

    ArrayList<String> triplist=new ArrayList<>();
    ListView doculist;

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

        doculist=findViewById(R.id.doculist);
        seldocument=findViewById(R.id.seldocument);

        crl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document: task.getResult()){
                        triplist.add(document.getId());
                    }
                    ArrayAdapter arrayAdapter=new ArrayAdapter(firebasetestActivity.this, android.R.layout.simple_list_item_multiple_choice,triplist);
                    doculist.setAdapter(arrayAdapter);

                    doculist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            editText.setText((String)adapterView.getItemAtPosition(i));
                        }
                    });

                    findViewById(R.id.delfirelist).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            SparseBooleanArray checkselected = doculist.getCheckedItemPositions();
                            int count= arrayAdapter.getCount();
                            String[] result={""};
                            for(int i=count-1; i>=0; --i){
                                if(checkselected.get(i)){
                                    String selectedId=(String)doculist.getItemAtPosition(i);

                                    cr.document(selectedId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            result[0]+="tripdata에서 삭제완료";
                                        }
                                    });
                                    crl.document(selectedId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            result[0]+="\ntripdatalist에서 삭제완료";
                                        }
                                    });
                                    triplist.remove(selectedId);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                            state.setText(result[0]);
                            doculist.clearChoices();
                        }
                    });

                    findViewById(R.id.startTestbtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listtime[0]=SystemClock.currentThreadTimeMillis();
                            seldocument.setText("로딩중");
                            String selectedId=editText.getText().toString();
                            cr.document(selectedId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot documentSnapshot=task.getResult();
                                    if(documentSnapshot.exists()){
                                        listtime[1]=SystemClock.currentThreadTimeMillis();
                                        ArrayList<String> locations= (ArrayList<String>) documentSnapshot.get("locations_name");
                                        seldocument.setText(locations.get(0)+"\n성공"+(listtime[1]-listtime[0])/1000.0);
                                    }
                                    else{
                                        seldocument.setText("실패");
                                    }
                                }
                            });
                        }
                    });
                }

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