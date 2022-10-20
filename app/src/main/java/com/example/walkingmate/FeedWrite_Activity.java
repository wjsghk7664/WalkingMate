package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.widget.LinearLayout;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;



import java.util.ArrayList;

public class FeedWrite_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    LinearLayout FeedMapLayout;
    PathOverlay pathOverlay;

    FeedData feedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_write);

        Intent getFeed=getIntent();
        String fileName=getFeed.getStringExtra("filename");

        FeedData fd=new FeedData();
        feedData=fd.loadfeed(fileName,this);

        MapFragment mapFragmentFeed=(MapFragment)getSupportFragmentManager().findFragmentById(R.id.feedmap);
        mapFragmentFeed.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        FeedMapLayout=findViewById(R.id.FeedMapLayout);
        this.naverMap=naverMap;

        pathOverlay=new PathOverlay();
        pathOverlay.setColor(Color.BLUE);
        pathOverlay.setOutlineColor(Color.BLUE);
        pathOverlay.setWidth(5);

        pathOverlay.setCoords(feedData.coordList);
        pathOverlay.setMap(naverMap);

        if(feedData.markerList.size()>0){
            for(int i=0; i<feedData.markerList.size(); ++i){
                feedData.markerList.get(i).setMap(naverMap);
            }
        }

        LatLng[] range=getMiddle(feedData.coordList);
        CameraUpdate cameraUpdate =CameraUpdate.fitBounds(new LatLngBounds(range[0],range[1]),100,200,100,100);
        naverMap.moveCamera(cameraUpdate);




    }

    public LatLng[] getMiddle(ArrayList<LatLng> coords){
        float maxlat=0,minlat=1000,maxlon=0,minlon=1000;
        float midlat, midlon;
        for(int i=0; i<coords.size(); ++i){
            if(maxlat<(float)coords.get(i).latitude){
                maxlat= (float) coords.get(i).latitude;
            }
            if(minlat>(float)coords.get(i).latitude){
                minlat=(float) coords.get(i).latitude;
            }
            if(maxlon<(float)coords.get(i).longitude){
                maxlon= (float) coords.get(i).longitude;
            }
            if(minlon>(float)coords.get(i).longitude){
                minlon=(float) coords.get(i).longitude;
            }
        }
        midlat=(maxlat+minlat)/2;
        midlon=(maxlon+minlon)/2;
        LatLng result[]=new LatLng[3];
        result[0]=new LatLng(minlat,minlon);
        result[1]=new LatLng(maxlat,maxlon);
        result[2]=new LatLng((maxlat+minlat)/2, (maxlon+minlon)/2);

        Log.d("카메라 좌표","min: "+result[0]+", max: "+result[1]);

        return result;
    }
}