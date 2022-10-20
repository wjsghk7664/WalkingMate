package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.naver.maps.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RouteActivity extends AppCompatActivity {

    double startX, startY, endX, endY;
    ArrayList<LatLng> passList=new ArrayList<>();//경유지
    String startName, endName;
    ArrayList<LatLng> coordlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        startX=127.1537926;
        startY=37.266414;
        endX=127.156369063794;
        endY=37.26793109745994;
        startName="출발";
        endName="도착";
        passList.add(new LatLng(126.92774822,37.55395475));
        passList.add(new LatLng(126.92577620,37.55337145));

        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestTmap();
            }
        }).start();


    }

    public void RequestTmap(){

        try{
            Log.d("루트 검색","시작");

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"startX\":" +
                    startX +
                    ",\"startY\":" +
                    startY +
                    "\"endX\":" +
                    endX +
                    ",\"endY\":" +
                    endY +
                    ",\"reqCoordType\":\"WGS84GEO\",\"startName\":\"" +
                    URLEncoder.encode(startName,"UTF-8")+
                    "\",\"endName\":\"" +
                    URLEncoder.encode(endName,"UTF-8") +
                    "\",\"searchOption\":\"0\",\"resCoordType\":\"WGS84GEO\",\"sort\":\"index\"}");
            Request request = new Request.Builder()
                    .url("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("appKey", "l7xxab136fb0e58b4e84a4a923596d9b17e6")
                    .build();

            Response response = client.newCall(request).execute();

            String mresponse=response.body().string();
            Log.d("JSON원본",mresponse);

            coordlist=getPoints(mresponse);
            Log.d("JSON-최종 좌표모음",coordlist.toString());
            Log.d("JSON-최종 좌표 수",coordlist.size()+"");


        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public ArrayList<LatLng> getPoints(String json){
        ArrayList<LatLng> points=new ArrayList<>();

        try{
            JSONObject jsonObject=new JSONObject(json);

            JSONArray jsonArray=jsonObject.getJSONArray("features");

            for(int i=0; i< jsonArray.length(); ++i){
                JSONObject object=jsonArray.getJSONObject(i);
                JSONObject innerobject=object.getJSONObject("geometry");
                String type=innerobject.getString("type");
                JSONArray coords=innerobject.getJSONArray("coordinates");
                Log.d("JSON테스트-객체",type);
                Log.d("JSON테스트-포인트",coords.toString());


                //1이 latitude, 0이 longitude
                if(type.equals("Point")){
                    double lat=coords.getDouble(1);
                    double lon=coords.getDouble(0);
                    points.add(new LatLng(lat,lon));
                }
                else {
                    for(int j=0; j<coords.length(); ++j){
                        JSONArray tmpob=coords.getJSONArray(j);

                        double lat=tmpob.getDouble(1);
                        double lon=tmpob.getDouble(0);
                        points.add(new LatLng(lat,lon));
                    }
                }
            }

        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        return points;
    }

    public String passlistString(ArrayList<LatLng> list){
        String result="";
        for(int i=0; i<list.size(); ++i){
            result+=list.get(i).latitude+","+list.get(i).longitude+"_";
        }
        result=result.substring(0,result.length()-1);
        return result;
    }


}