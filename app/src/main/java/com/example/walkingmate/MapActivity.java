package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient;

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference challenge=fb.collection("challenge");

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;

    private NaverMap naverMap;
    private double lat, lon;
    boolean[] IsTracking = new boolean[1]; //경로추적기능 실행중 여부 확인
    float displacement; //이동거리
    int step;//발걸음 수
    long runtime;//걸은 시간(실시간 갱신)

    LatLng[] tmpcoord;

    double[] startcoord = new double[2];

    ArrayList<LatLng> coordList = new ArrayList<>();//경로추적좌표
    ArrayList<LatLng> markList = new ArrayList<>(); //마크된 좌표 모음
    ArrayList<Marker> markerList = new ArrayList<>();//마커 모음
    HashMap<LatLng, String> markMap = new HashMap<>();//마크된 좌표와 메모 모음
    String[] timecheck = new String[2];//시작과 종료 시간,0이 시작-1이 종료


    ImageButton backBtn, endBtn, markBtn;
    TextView disTxt, walkTxt, runtimeTxt;

    LinearLayout MapLayout;
    PathOverlay pathOverlay;

    int selecteditem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        backBtn = findViewById(R.id.back_tracing);
        endBtn = findViewById(R.id.endBtn);

        disTxt = findViewById(R.id.displacement_walk);
        walkTxt = findViewById(R.id.walk_tracking);
        runtimeTxt = findViewById(R.id.time_tracking);

        IsTracking[0] = true;

        tmpcoord = new LatLng[2];



        //비 동기적으로 네이버 지도 정보 가져옴
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d("도보 기록-flpc체크",(fusedLocationProviderClient==null)+"");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("도보 기록","권한 체크 에러");
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //시작위치부터 좌표모음 시작
                    startcoord[0] = location.getLatitude();
                    startcoord[1] = location.getLongitude();
                    coordList.add(new LatLng(startcoord[0], startcoord[1]));
                    tmpcoord[0] = coordList.get(0);
                    Log.d("도보 기록", "시작좌표" + tmpcoord[0]);
                    mapFragment.getMapAsync(MapActivity.this);
                }
                else{
                    mapFragment.getMapAsync(MapActivity.this);
                    Log.d("도보 기록", "시작 null");
                }
            }
        });

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);//현재위치값 받아옴

        //시작 세팅
        coordList.clear();
        markList.clear();
        markMap.clear();
        markerList.clear();
        displacement = 0;
        step = 0;
        walkTxt.setText("0");
        startStepCounterService();
        startTimeCheckingService();
        //시작 세팅

        timecheck[0] = getTime();
        Toast.makeText(MapActivity.this, "경로추적 시작!", Toast.LENGTH_SHORT).show();

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendfilename="";
                //시작한 상태일때만 작동
                if (coordList.size() < 2) {
                    return;
                } else if (IsTracking[0]) {

                    timecheck[1] = getTime();

                    //좌표가 너무 많으면 줄이기
                    while(coordList.size()>5000){
                        ArrayList<LatLng> tmp=new ArrayList();
                        Log.d("여행루트",coordList.size()+"");
                        for(int i=0; i<coordList.size(); ++i){
                            if(i%4!=0){
                                tmp.add(coordList.get(i));
                            }
                        }
                        coordList=tmp;
                    }

                    //피드데이터 내부저장소에 저장
                    FeedData feedData = new FeedData(coordList, markerList, timecheck, step, displacement);
                    sendfilename=feedData.savefeed(feedData, MapActivity.this);

                    IsTracking[0] = false;

                    stopStepCounterService();
                    stopTimeCheckingService();

                    challenge.document(UserData.loadData(MapActivity.this).userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Long updatestep=task.getResult().getLong("step")+(long)step;
                            challenge.document(UserData.loadData(MapActivity.this).userid).update("step",updatestep);
                        }
                    });

                    Intent gofeed=new Intent(MapActivity.this, EndtrackingActivity.class);
                    gofeed.putExtra("filename",sendfilename);
                    startActivity(gofeed);
                    finish();

                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsTracking[0]){
                    Intent backfeed=new Intent(MapActivity.this, FeedCalendarActivity.class);

                    startActivity(backfeed);
                }
                else{
                    finish();
                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }


    //지도 로딩 후 호출되는 매소드
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {


        //실시간 이동거리 계산을 위한 위치 변수, idx0이 현재, idx1이 과거 위치

        this.naverMap = naverMap;
        locationSource.getLastLocation();

        pathOverlay = new PathOverlay();
        pathOverlay.setColor(Color.BLUE);
        pathOverlay.setOutlineColor(Color.BLUE);
        pathOverlay.setWidth(5);




        CameraPosition cameraPosition=new CameraPosition(new LatLng(startcoord[0],startcoord[1]),17);
        naverMap.setCameraPosition(cameraPosition);


        naverMap.setLocationSource(locationSource);//네이버지도상 위치값을 받아온 현재 위치값으로 설정
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

        //아래는 UI세팅을 위한 코드
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);



        //위치 좌표값이 변경될 때 마다 좌표값을 얻어온 뒤 해당 위치로 카메라 이동
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                lat=location.getLatitude();
                lon=location.getLongitude();

                if(IsTracking[0]){
                    tmpcoord[1]=tmpcoord[0];
                    tmpcoord[0] =new LatLng(lat,lon);
                    coordList.add(tmpcoord[0]);
                    Log.d("GPS TRACKING", tmpcoord[0].toString());

                    //좌표모음에 들어간 좌표가 두개 이상인 경우 거리 측정(단위는 km)
                    if(coordList.size()>1){
                        displacement+=tmpcoord[0].distanceTo(tmpcoord[1])/1000;
                    }
                    disTxt.setText(""+Math.round((displacement*1000))/1000.0);

                    //경로 실시간 업데이트
                    if(coordList.size()>1){
                        pathOverlay.setCoords(coordList);
                        pathOverlay.setMap(naverMap);
                    }
                }
            }
        });

        markBtn=findViewById(R.id.markerBtn);

        markBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsTracking[0]!=true){
                    return;
                }
                else if(coordList.size()<1){
                    return;
                }
                else{
                    LatLng tmpll=coordList.get(coordList.size()-1);

                    //이미 등록된 마커인 경우
                    if(markMap.containsKey(tmpll)){
                        Toast.makeText(MapActivity.this, "이미 마커를 등록한 위치입니다.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] tmpmark=new String[1];

                    final LinearLayout markTextdialog=(LinearLayout)View.inflate(MapActivity.this, R.layout.dialog_marktext, null);
                    AlertDialog.Builder adBuilder= new AlertDialog.Builder(MapActivity.this);
                    AlertDialog alertDialog=adBuilder.setView(markTextdialog).setPositiveButton("입력", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText marktext=markTextdialog.findViewById(R.id.markEditText);
                            //입력 버튼 누를시 마커 등록
                            try{
                                tmpmark[0]=marktext.getText().toString();
                            }catch (Exception e){
                                e.printStackTrace();
                                return;
                            }
                            markList.add(tmpll);
                            markMap.put(tmpll,tmpmark[0]);
                            Marker tmp=new Marker();
                            tmp.setPosition(tmpll);
                            tmp.setCaptionAligns(Align.Top);
                            tmp.setCaptionText(tmpmark[0]);
                            markerList.add(tmp);


                            //마커표시 즉시 지도에 반영
                            tmp.setMap(naverMap);
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //취소버튼 누를시 마커 등록도 취소됨
                            return;
                        }
                    }).create();

                    alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    alertDialog.show();
                    alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                }
            }
        });

    }

    public String getTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy년_MM월_dd일_HH시_mm분");//날짜시간
        String getTime=sdf.format(date);
        return getTime;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        Intent intent=new Intent();
        if(id==R.id.challenge){
            intent=new Intent(this, challenge_activity.class);
        }
        else if(id==R.id.managefriends){
            intent=new Intent(this, ManageFriend_Activity.class);
        }
        else if(id==R.id.helpinfo){
            intent=new Intent(this, HelpInfo_Activity.class);
        }
        else if(id==R.id.appinfo){
            intent=new Intent(this, AppInfo_Activity.class);
        }
        startActivity(intent);
        return true;
    }

    //loaction service를 onPause에서 시작하고 onResume에서 끝냄
    //멈춰있는동안만 서비스 실행, 단 위치 추적 실행중에만(Istracking[]
    //이동거리는 resultreceiver에 사용된 handler에서 실시간으로 갱신

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("멈춤","좌표수: "+coordList.size());
        if(IsTracking[0]){
            startLocationService();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("시작","좌표수: "+coordList.size());
        stopLocationService();
    }


    //실행중 뒤로가기로 종료 방지
    @Override
    public void onBackPressed() {
        if(IsTracking[0]){
            Intent backfeed=new Intent(MapActivity.this, FeedCalendarActivity.class);

            startActivity(backfeed);
        }
        else{
            finish();
        }

    }

    //이 아래는 서비스 실행을 위한 코드

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }


    Handler handler=new Handler();
    ResultReceiver resultReceiver= new ResultReceiver(handler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode==1){
                if(IsTracking[0]){
                    LatLng tmp=new LatLng(resultData.getDouble("lat"),resultData.getDouble("lon"));
                    coordList.add(tmp);
                    tmpcoord[1]=tmpcoord[0];
                    tmpcoord[0] =tmp;
                    if(coordList.size()>1){
                        displacement+=tmpcoord[0].distanceTo(tmpcoord[1])/1000;
                    }
                }
            }
            //종료 코드
            else if(resultCode==3){
                if (coordList.size() < 2) {
                    return;
                } else if (IsTracking[0]) {

                    timecheck[1] = getTime();

                    while(coordList.size()>5000){
                        ArrayList<LatLng> tmp=new ArrayList();
                        Log.d("여행루트",coordList.size()+"");
                        for(int i=0; i<coordList.size(); ++i){
                            if(i%4!=0){
                                tmp.add(coordList.get(i));
                            }
                        }
                        coordList=tmp;
                    }


                    //피드데이터 내부저장소에 저장
                    FeedData feedData = new FeedData(coordList, markerList, timecheck, step, displacement);
                    feedData.savefeed(feedData, MapActivity.this);

                    challenge.document(UserData.loadData(MapActivity.this).userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Long updatestep=task.getResult().getLong("step")+(long)step;
                            challenge.document(UserData.loadData(MapActivity.this).userid).update("step",updatestep);
                        }
                    });

                    Log.d("백그라운드","중지");
                    IsTracking[0] = false;

                    //화면 초기화 시켜놓고 종료
                    //startActivity(new Intent(getApplicationContext(),MapActivity.class));
                    finish();
                }
            }
            else if(resultCode==2){
                if(coordList.size()<1){
                    return;
                }
                else{
                    LatLng tmpll=coordList.get(coordList.size()-1);

                    //이미 등록된 마커인 경우
                    if(markMap.containsKey(tmpll)){
                        Toast.makeText(getApplicationContext(), "이미 마커를 등록한 위치입니다.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] tmpmark= {""};

                    markList.add(tmpll);
                    markMap.put(tmpll,tmpmark[0]);
                    Marker tmpm=new Marker();
                    tmpm.setPosition(tmpll);
                    tmpm.setCaptionAligns(Align.Top);
                    tmpm.setCaptionText(tmpmark[0]);
                    markerList.add(tmpm);


                    //마커표시 즉시 지도에 반영
                    tmpm.setMap(naverMap);
                    Toast.makeText(getApplicationContext(),"마커 등록 성공!",Toast.LENGTH_SHORT).show();

                    Log.d("백그라운드","마커등록, 좌표수: "+coordList.size());
                }
            }
        }
    };


    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            intent.putExtra("RECEIVER",resultReceiver);
            startService(intent);
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
        }


    }

    public void startStepCounterService(){
        Intent intent = new Intent(getApplicationContext(), StepCounterService.class);
        intent.setAction(Constants.ACTION_START_STEP_COUNTER_SERVICE);
        intent.putExtra("STEPRECIEVER",resultReceiverStep);
        startService(intent);
    }

    public void stopStepCounterService(){
        Log.d("만보기","정지액션 전송");
        Intent intent = new Intent(getApplicationContext(), StepCounterService.class);
        intent.setAction(Constants.ACTION_STOP_STEP_COUNTER_SERVICE);
        startService(intent);
    }

    private boolean isStepCounterServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (StepCounterService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }




    Handler handler2=new Handler();
    ResultReceiver resultReceiverStep= new ResultReceiver(handler2){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode==10){
                step=resultData.getInt("step");
                walkTxt.setText(""+step);
                //Log.d("만보기","종료 후 작동중 체크용-walk");
            }
        }
    };


    public void startTimeCheckingService(){
        Intent intent = new Intent(getApplicationContext(), TimecheckingService.class);
        intent.setAction(Constants.ACTION_START_TIMECEHCKING_SERVICE);
        intent.putExtra("TIMECHECKINGSERVICE",resultReceiverTime);
        startService(intent);
    }

    public void stopTimeCheckingService(){
        Log.d("걸은시간","정지액션 전송");
        Intent intent = new Intent(getApplicationContext(), TimecheckingService.class);
        intent.setAction(Constants.ACTION_STOP_TIMECHECKING_SERVICE);
        startService(intent);
    }



    Handler handler3=new Handler();
    ResultReceiver resultReceiverTime= new ResultReceiver(handler3){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode==15){
                //Log.d("걸은시간","종료 후 작동중 체크용-time");
                runtime=resultData.getLong("time");
                String h=String.format("%02d",runtime/(3600000));
                String m=String.format("%02d",runtime/60000);
                String s=String.format("%02d",(runtime/1000)%60);
                runtimeTxt.setText(h+":"+m+":"+s);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent1=new Intent(getApplicationContext(),LocationService.class);
        Intent intent2=new Intent(getApplicationContext(),StepCounterService.class);
        Intent intent3=new Intent(getApplicationContext(),TimecheckingService.class);
        stopService(intent1);
        stopService(intent2);
        stopService(intent3);
    }
}