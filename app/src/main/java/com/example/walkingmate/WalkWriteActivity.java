package com.example.walkingmate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class WalkWriteActivity extends AppCompatActivity {

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference walkdata=fb.collection("tripdata");

    Button searchbtn, finishbtn;
    TextView startloctxt;
    ImageView pin_walk;
    Spinner agespin, sexspin;

    String location;
    LatLng loccoord;
    int month, day, hour, min, taken_time;
    String gender,age;

    EditText montxt,daytxt,hourtxt,mintxt, taken_timetxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_write);

        searchbtn=findViewById(R.id.search_btn);
        finishbtn=findViewById(R.id.finish_walk);
        startloctxt=findViewById(R.id.StartLocation);
        pin_walk=findViewById(R.id.pin_walk);

        agespin=findViewById(R.id.spinner_age);
        sexspin=findViewById(R.id.spinner_sex);


        montxt=findViewById(R.id.month_walk);
        daytxt=findViewById(R.id.day_walk);
        hourtxt=findViewById(R.id.hour_walk);
        mintxt=findViewById(R.id.min_walk);
        taken_timetxt=findViewById(R.id.minute_walk);

        setHintTime();

        findViewById(R.id.back_walkwrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getsearchResult.launch(new Intent(WalkWriteActivity.this,SetLocationWalkActivity.class));
            }
        });

        finishbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    month=Integer.parseInt(montxt.getText().toString());
                    day=Integer.parseInt(daytxt.getText().toString());
                    hour=Integer.parseInt(hourtxt.getText().toString());
                    min=Integer.parseInt(mintxt.getText().toString());

                    taken_time=Integer.parseInt(taken_timetxt.getText().toString());

                }catch (NumberFormatException e){
                    e.printStackTrace();
                    Toast.makeText(WalkWriteActivity.this,"입력값이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                }catch (Exception ee){
                    ee.printStackTrace();
                }
                if(CheckValues(month,day,hour,min,taken_time)){
                    age=agespin.getSelectedItem().toString();
                    gender=sexspin.getSelectedItem().toString();
                    sendData();
                    finish();

                    }
                else{
                    Toast.makeText(WalkWriteActivity.this,"입력값이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void sendData(){
        HashMap<String, Object> data=new HashMap<>();

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String writetime=sdf.format(date);

        String documentID= writetime;//나중에 뒤에 유저아이디 추가
        Log.d("테스트",documentID);
        //아래 정보는 보는 사람 입장에서 필터링을 위함.
        //data.put("userid, userid);
        //data.put("userage", userage);
        //data.put("usergender, usergender);

        data.put("writetime",writetime);
        data.put("age",age);
        data.put("gender",gender);
        data.put("month", month);
        data.put("day", day);
        data.put("hour", hour);
        data.put("minute", min);
        data.put("takentime", taken_time);
        data.put("location_name",location);
        data.put("location_coord",location);
        walkdata.document(documentID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"작성 완료되었습니다.",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"작성 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<Intent> getsearchResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if(result.getData()!=null){
                        loccoord=new LatLng(result.getData().getFloatExtra("lat",0),
                                result.getData().getFloatExtra("lat",0));
                        location=result.getData().getStringExtra("location");
                        startloctxt.setVisibility(View.VISIBLE);
                        pin_walk.setVisibility(View.VISIBLE);
                        startloctxt.setText(location);
                    }
                }
            });

    //현재 시간을 힌트로 표시
    //월 일은 세팅
    public void setHintTime(){
        SimpleDateFormat format=new SimpleDateFormat("MM dd HH mm");
        long now=System.currentTimeMillis();
        Date date=new Date(now);
        String[] getTime=format.format(date).split(" ");
        montxt.setHint(getTime[0]);
        daytxt.setHint(getTime[1]);
        hourtxt.setHint(getTime[2]);
        mintxt.setHint(getTime[3]);

        montxt.setText(getTime[0]);
        daytxt.setText(getTime[1]);

    }

    //입력값 오류체크
    public boolean CheckValues(int mon, int day, int hour, int min, int taken_time){
        boolean result=true;

        //현재 시간 이전값인지 체크
        if(mon<Integer.parseInt(montxt.getHint().toString())){
            result=false;
        }
        else if(mon==Integer.parseInt(montxt.getHint().toString())){
            if(day<Integer.parseInt(daytxt.getHint().toString())){
                result=false;
            }
            else if(day==Integer.parseInt(daytxt.getHint().toString())){
                if(hour<Integer.parseInt(hourtxt.getHint().toString())){
                    result=false;
                }
                else if(hour==Integer.parseInt(hourtxt.getHint().toString())){
                    if(min<Integer.parseInt(mintxt.getHint().toString())){
                        result=false;
                    }
                }
            }
        }

        //잘못된 날짜값인지 체크
        try{
            String date=String.format("%02d%02d%02d%02d",mon,day,hour,min);
            SimpleDateFormat sdf=new SimpleDateFormat("MMddHHmm");
            sdf.setLenient(false);
            sdf.parse(date);
        }catch (Exception e){
            result=false;
        }

        //걸리는 시간 체크
        if(taken_time<=0){
            result=false;
        }

        //장소 선택여부 체크
        if(loccoord==null){
            result=false;
        }

        return result;
    }
}