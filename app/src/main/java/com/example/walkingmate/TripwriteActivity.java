package com.example.walkingmate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.MarkerIcons;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TripwriteActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference tripwrite=fb.collection("tripdata");
    CollectionReference triplist=fb.collection("tripdatalist");

    EditText title,montxt,daytxt,hourtxt,mintxt,takentxt;
    Spinner gender,age;
    Button search,finish;
    ImageButton back;
    ListView loclist;
    ScrollView scrollView;

    int mon,day,hour,min,taken;
    String titlestr,genderstr,agestr;

    ArrayList<LatLng> routecoords;
    ArrayList<LatLng> loccoords=new ArrayList<>();
    ArrayList<String> locnames=new ArrayList<>();
    ArrayList<Marker> markers=new ArrayList<>();

    NaverMap naverMap;
    PathOverlay pathOverlay=new PathOverlay();
    MapFragment mapFragmentFeed;

    locAdapter locAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tripwrite);

        scrollView=findViewById(R.id.scrollView_trip);


        title=findViewById(R.id.title_trip);
        montxt=findViewById(R.id.month_trip);
        daytxt=findViewById(R.id.day_trip);
        hourtxt=findViewById(R.id.hour_trip);
        mintxt=findViewById(R.id.min_trip);
        takentxt=findViewById(R.id.taken_time);

        gender=findViewById(R.id.spinner_sex_trip);
        age=findViewById(R.id.spinner_age_trip);

        search=findViewById(R.id.search_trip);
        finish=findViewById(R.id.finish_trip);

        back=findViewById(R.id.back_trip);

        loclist=findViewById(R.id.loclist_trip);

        markers=new ArrayList<>();

        setHintTime();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getsearchResult.launch(new Intent(TripwriteActivity.this,TestActivity.class));
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mon=Integer.parseInt(montxt.getText().toString());
                    day=Integer.parseInt(daytxt.getText().toString());
                    hour=Integer.parseInt(hourtxt.getText().toString());
                    min=Integer.parseInt(mintxt.getText().toString());

                    taken=Integer.parseInt(takentxt.getText().toString());

                }catch (NumberFormatException e){
                    e.printStackTrace();
                    Toast.makeText(TripwriteActivity.this,"입력값이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                }catch (Exception ee){
                    ee.printStackTrace();
                }
                if(CheckValues(mon,day,hour,min,taken)){
                    agestr=age.getSelectedItem().toString();
                    genderstr=gender.getSelectedItem().toString();
                    titlestr=title.getText().toString();
                    sendData();
                    //finish();
                }
                else{
                    Toast.makeText(TripwriteActivity.this,"입력값이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mapFragmentFeed=(MapFragment)getSupportFragmentManager().findFragmentById(R.id.tripwriteMap);

        locAdapter=new locAdapter(this);
        loclist.setAdapter(locAdapter);




    }


    public void sendData(){
        HashMap<String, Object> data=new HashMap<>();

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        String documentID= sdf.format(date);//나중에 뒤에 유저아이디 추가
        Log.d("테스트",documentID);
        //data.put("userid, userid);
        data.put("title",titlestr);
        data.put("age",agestr);
        data.put("gender",genderstr);
        data.put("month", mon);
        data.put("day", day);
        data.put("hour", hour);
        data.put("minute", min);
        data.put("takentime", taken);
        data.put("locations_name",locnames);

        triplist.document(documentID).set(data);

        data.put("locations_coordinate",loccoords);
        data.put("route",routecoords);
        tripwrite.document(documentID).set(data);
    }

    //현재 시간을 힌트로 표시
    public void setHintTime(){
        SimpleDateFormat format=new SimpleDateFormat("MM dd HH mm");
        long now=System.currentTimeMillis();
        Date date=new Date(now);
        String[] getTime=format.format(date).split(" ");
        montxt.setHint(getTime[0]);
        daytxt.setHint(getTime[1]);
        hourtxt.setHint(getTime[2]);
        mintxt.setHint(getTime[3]);

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
        if(loccoords.size()==0){
            result=false;
        }

        return result;
    }

    private final ActivityResultLauncher<Intent> getsearchResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if(result.getData()!=null){
                        routecoords=(ArrayList<LatLng>) result.getData().getSerializableExtra("routecoords");
                        loccoords=(ArrayList<LatLng>) result.getData().getSerializableExtra("loccoords");
                        locnames=(ArrayList<String>) result.getData().getSerializableExtra("locnames");
                        Log.d("여행","주소검색결과반환:"+loccoords.size());
                        mapFragmentFeed.getMapAsync(this);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        locAdapter.notifyDataSetChanged();
                                        setListViewHeightBasedOnItems(loclist);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d("여행", "onmapready");

        this.naverMap=naverMap;

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        if(routecoords!=null){
            Log.d("여행게시물",routecoords.size()+"");
            LatLng[] range;
            pathOverlay.setMap(null);
            if(routecoords.size()>0){
                pathOverlay=new PathOverlay();
                pathOverlay.setColor(Color.BLUE);
                pathOverlay.setOutlineColor(Color.BLUE);
                pathOverlay.setWidth(5);

                pathOverlay.setCoords(routecoords);
                pathOverlay.setMap(naverMap);
                range=getMiddle(routecoords);
            }
            else{
                range=getMiddle(loccoords);
            }
            CameraUpdate cameraUpdate =CameraUpdate.fitBounds(new LatLngBounds(range[0],range[1]),50,100,50,50);
            naverMap.moveCamera(cameraUpdate);
        }



        if(markers.size()>0){
            //마커 초기화
            for(int i=0; i<markers.size(); ++i){
                markers.get(i).setMap(null);
            }
            markers.clear();

        }
        //markers세팅
        for(int i=0; i<locnames.size(); ++i){
            Marker tmp=new Marker();
            tmp.setIcon(MarkerIcons.BLACK);
            tmp.setCaptionAligns(Align.Top);
            tmp.setPosition(loccoords.get(i));
            tmp.setWidth(63);
            tmp.setHeight(84);

            String destorderString="";
            switch (i){
                case 0:
                    destorderString+="출발지";
                    break;
                case 1:
                    destorderString+="1st"; break;
                case 2:
                    destorderString+="2nd"; break;
                case 3:
                    destorderString+="3rd"; break;
                default:
                    destorderString+=i+"th"; break;
            }
            if(i==0){
                tmp.setCaptionColor(Color.RED);
                tmp.setIconTintColor(Color.RED);
            }
            else{
                tmp.setCaptionColor(Color.BLUE);
                tmp.setIconTintColor(Color.BLUE);
            }
            tmp.setCaptionText(destorderString);
            markers.add(tmp);
            tmp.setMap(naverMap);
        }



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

    public class locAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater;


        public locAdapter(Context context){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return locnames.size();
        }

        @Override
        public Object getItem(int i) {
            return locnames.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            View view=layoutInflater.inflate(R.layout.trip_list_layout, null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(locnames.size()==0){
                return null;
            }
            else{
                ImageView imageView=view.findViewById(R.id.trip_image);
                TextView textView=view.findViewById(R.id.triplist);

                if(i==0){
                    imageView.setImageResource(R.drawable.pin_red);
                }
                else{
                    imageView.setImageResource(R.drawable.pin_blue);
                }
                textView.setText(locnames.get(i));

                return view;
            }
        }
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        locAdapter listAdapter = (TripwriteActivity.locAdapter) listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                float px = 500 * (listView.getResources().getDisplayMetrics().density);
                item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Get padding
            int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

            Log.d("리스트뷰",totalItemsHeight+","+totalDividersHeight+","+totalPadding);
            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (int) ((totalItemsHeight + totalDividersHeight + totalPadding)*0.7);
            listView.setLayoutParams(params);
            listView.requestLayout();
            //setDynamicHeight(listView);
            return true;

        } else {
            return false;
        }
    }
}