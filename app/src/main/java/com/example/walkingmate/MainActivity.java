package com.example.walkingmate;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity {


    TextView coordText, permlist;

    Button tmploginbtn,permissionbtn;

    String reqperm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tmploginbtn = findViewById(R.id.tmplogin);
        permissionbtn=findViewById(R.id.permission);
        permlist=findViewById(R.id.permlist);

        findViewById(R.id.gotest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });

        findViewById(R.id.goLoginbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NaverLoginActivity.class));
            }
        });

        findViewById(R.id.chatbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ChattingActivity.class));
            }
        });

        findViewById(R.id.uitest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,UitestingActivity.class));
            }
        });





        //백그라운드 위치권한은 요청이 불가능 하므로 직접 설정하도록 유도해 줘야함.
        //메인 화면에서 백그라운드 권한 요청이 안되어 있는경우 화면에 보여서 권한 승인 화면으로 가도록 유도
        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};


        ActivityCompat.requestPermissions(this, Permissions, 1);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            tmploginbtn.setVisibility(View.INVISIBLE);
            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
        }
        else{
            tmploginbtn.setVisibility(View.VISIBLE);
            permissionbtn.setVisibility(View.INVISIBLE);
            permlist.setVisibility(View.INVISIBLE);
        }

        reqperm="";
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="위치권한: 항상 허용으로 설정\n";
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="신체활동 권한: 허용으로 설정";
        }
        permlist.setText(reqperm);

        permissionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recogIntent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:com.example.walkingmate"));
                startActivity(recogIntent);
            }
        });




        //로그인 후 지도 화면으로 넘어감
        tmploginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GoMap =new Intent(MainActivity.this, MapActivity.class);
                startActivity(GoMap);
            }
        });

        Button gocal=findViewById(R.id.gocal);
        gocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Gocal=new Intent(MainActivity.this, FeedCalendarActivity.class);
                startActivity(Gocal);
            }
        });

        Button testbtn=findViewById(R.id.testbtn);
        EditText testText=findViewById(R.id.testText);
        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String tmp=testText.getText().toString();
                    String y,m,d;
                    y=tmp.substring(0,4);
                    m=tmp.substring(4,6);
                    d=tmp.substring(6,8);
                    FeedData feedData=new FeedData();
                    String[] tmpdd={y+"년_"+m+"월_"+d+"일_"+"10시_23분"," "};
                    feedData.timecheck=tmpdd;
                    feedData.savefeed(feedData,MainActivity.this);
                    Toast.makeText(MainActivity.this,"저장완료:"+feedData.timecheck[0],Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


    }

    //뒤로가기로 나갔을시 홈버튼으로 나간것처럼 만들음. 종료로 인한 오류 방지
    @Override
    public void onBackPressed() {
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            tmploginbtn.setVisibility(View.INVISIBLE);
            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
        }
        else{
            tmploginbtn.setVisibility(View.VISIBLE);
            permissionbtn.setVisibility(View.INVISIBLE);
            permlist.setVisibility(View.INVISIBLE);
        }
        reqperm="";
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="위치권한: 항상 허용으로 설정\n";
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="신체활동 권한: 허용으로 설정";
        }
        permlist.setText(reqperm);
    }
}