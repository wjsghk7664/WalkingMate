package com.example.walkingmate;



import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TestActivity extends AppCompatActivity implements OnMapReadyCallback{

    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NaverMap naverMap;
    private EditText mEtAddress;
    Button sync;
    TextView coord;
    String location, x, y;//x-lon, y-lat(반대임)
    double xval,yval;

    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        coord=findViewById(R.id.coord);

        sync=findViewById(R.id.sync);

        MapFragment mapFragmentFeed=(MapFragment)getSupportFragmentManager().findFragmentById(R.id.testmap);

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    yval = location.getLatitude();
                    xval = location.getLongitude();
                    mapFragmentFeed.getMapAsync(TestActivity.this);
                    coord.setText(yval+","+xval);
                }
            }
        });


        mEtAddress=findViewById(R.id.edit_address);
        mEtAddress.setFocusable(false); //입력막음

        //주소입력창 이동, getsearchResult로 주소명 받아옴
        mEtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TestActivity.this, SearchActivity.class);
                getsearchResult.launch(intent);
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragmentFeed.getMapAsync(TestActivity.this);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap=naverMap;

        Marker marker=new Marker();


        CameraPosition cameraPosition=new CameraPosition(new LatLng(yval,xval),17);
        naverMap.setCameraPosition(cameraPosition);

        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        naverMap.setLocationTrackingMode(LocationTrackingMode.None);

        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                CameraPosition tmp=naverMap.getCameraPosition();
                yval=tmp.target.latitude;
                xval=tmp.target.longitude;
                coord.setText(yval+","+xval);
            }
        });

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                marker.setPosition(latLng);
                marker.setMap(naverMap);
            }
        });


    }

    private final ActivityResultLauncher<Intent> getsearchResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if(result.getData()!=null){
                        location=result.getData().getStringExtra("data");
                        mEtAddress.setText(location);
                        //받아온 뒤 바로 geocoding으로 좌표 추출
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                requestCoord(location);
                            }
                        }).start();
                    }
                }
            });


    public void requestCoord(String loc){
        try{
            BufferedReader bufferedReader;
            StringBuilder stringBuilder=new StringBuilder();
            String query= "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(loc, "UTF-8");
            URL url=new URL(query);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            if(conn!=null){
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","ixriz6b4c6");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY","fi7zIz2MbRs544dcY8AxvifpraozMHAAqXoE1ewe");
                conn.setDoInput(true);

                int responseCode=conn.getResponseCode();
                Log.d("주소 검색","Geocoding결과:"+responseCode);

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line=null;
                while((line=bufferedReader.readLine())!=null){
                    stringBuilder.append(line+"\n");
                }

                int indexFirst, indexLast;
                indexFirst = stringBuilder.indexOf("\"x\":\"");
                indexLast = stringBuilder.indexOf("\",\"y\":");
                Log.d("주소 검색","x:"+indexFirst+", y:"+indexLast);

                x=stringBuilder.substring(indexFirst+5,indexLast);

                indexFirst = stringBuilder.indexOf("\"y\":\"");
                indexLast = stringBuilder.indexOf("\",\"distance\":");
                y = stringBuilder.substring(indexFirst + 5, indexLast);

                bufferedReader.close();
                conn.disconnect();

                xval=Double.parseDouble(x); //lon
                yval=Double.parseDouble(y);//lat

                Log.d("주소 검색","좌표:"+xval+","+yval);

            }
            else{
                Log.d("주소 검색","연결실패");
            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //
        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }
}