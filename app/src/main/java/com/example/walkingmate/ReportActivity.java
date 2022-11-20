package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends Activity {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference reportcr=fb.collection("report");

    Button b1,b2,b3,b4,send;
    EditText reason;

    int reportcase=0;

    String userid;
    UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_report);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Intent getintent=getIntent();
        userid=getintent.getStringExtra("userid");
        userData=UserData.loadData(this);

        b1=findViewById(R.id.report_reason1);
        b2=findViewById(R.id.report_reason2);
        b3=findViewById(R.id.report_reason3);
        b4=findViewById(R.id.reason_extra);
        reason=findViewById(R.id.reportreason);
        send=findViewById(R.id.send_report);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportcase=1;
                b1.setBackgroundResource(R.drawable.writebox_blur);
                b2.setBackgroundResource(R.drawable.greenbtn);
                b3.setBackgroundResource(R.drawable.greenbtn);
                b4.setBackgroundResource(R.drawable.greenbtn);
                reason.setVisibility(View.INVISIBLE);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportcase=2;
                b1.setBackgroundResource(R.drawable.greenbtn);
                b2.setBackgroundResource(R.drawable.writebox_blur);
                b3.setBackgroundResource(R.drawable.greenbtn);
                b4.setBackgroundResource(R.drawable.greenbtn);
                reason.setVisibility(View.INVISIBLE);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportcase=3;
                b1.setBackgroundResource(R.drawable.greenbtn);
                b2.setBackgroundResource(R.drawable.greenbtn);
                b3.setBackgroundResource(R.drawable.writebox_blur);
                b4.setBackgroundResource(R.drawable.greenbtn);
                reason.setVisibility(View.INVISIBLE);
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportcase=4;
                b1.setBackgroundResource(R.drawable.greenbtn);
                b2.setBackgroundResource(R.drawable.greenbtn);
                b3.setBackgroundResource(R.drawable.greenbtn);
                b4.setBackgroundResource(R.drawable.writebox_blur);
                reason.setVisibility(View.VISIBLE);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reports="";
                if(reportcase==0){
                    Toast.makeText(ReportActivity.this,"신고사유를 선택해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(reportcase==4){
                    reports=reason.getText().toString();
                }
                else{
                    reports=reportcase+"";
                }

                Map<String,String> data=new HashMap<>();
                data.put(userData.userid,reports);


                reportcr.document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.getResult().exists()){
                            DocumentSnapshot document=task.getResult();
                            if(document.get(userData.userid)!=null){
                                Toast.makeText(ReportActivity.this,"이미 신고한 유저입니다.",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                reportcr.document(userid).set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ReportActivity.this,"신고 접수 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                        else{
                            reportcr.document(userid).set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(ReportActivity.this,"신고 접수 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("신고",e.toString());
                    }
                });

            }
        });

    }
}