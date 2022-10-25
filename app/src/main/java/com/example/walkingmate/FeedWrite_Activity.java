package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.MarkerIcons;


import java.io.InputStream;
import java.util.ArrayList;


public class FeedWrite_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naverMap;
    LinearLayout FeedMapLayout;
    PathOverlay pathOverlay;

    FeedData feedData;

    TextView distxt,steptxt,timetxt, datetxt, curpage;
    ImageView imageback;
    ListView listView;
    MbEditText record;

    FrameLayout imagebacklayout;

    ArrayList<Marker> markers;

    int selected=-1;

    MapFragment mapFragmentFeed;
    ArrayList<imageFragment> fragments=new ArrayList<>();


    ViewPager2 viewPager2;
    FragmentStateAdapter fragmentStateAdapter;

    int curposition=0;

    Bitmap bmp;

    String weather="sunny";
    String emotion="smile";
    ImageButton weatherbtn,emotionbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_write);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent getFeed=getIntent();
        String fileName=getFeed.getStringExtra("filename");

        FeedData fd=new FeedData();
        feedData=fd.loadfeed(fileName,this);

        markers=feedData.markerList;

        mapFragmentFeed=(MapFragment)getSupportFragmentManager().findFragmentById(R.id.feedmap);
        mapFragmentFeed.getMapAsync(this);

        record=findViewById(R.id.record_feedwrite);

        record.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        record.clearFocus();
                        InputMethodManager imm =
                                (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(record.getWindowToken(), 0);
                        return true;
                    }
                }
                return false;
            }
        });


        imageback=findViewById(R.id.imagebackground);
        imagebacklayout=findViewById(R.id.image_feedwrite);

        distxt=findViewById(R.id.distext_feed);
        steptxt=findViewById(R.id.feedstep);
        timetxt=findViewById(R.id.feed_time);
        datetxt=findViewById(R.id.feed_date);

        String diststr=String.format("%.3f km",feedData.displacement);
        String stepstr=String.format("%d 걸음",feedData.step);

        String[] start=feedData.timecheck[0].split("_");
        String[] end=feedData.timecheck[1].split("_");
        String timestr=String.format("%s:%s ~ %s:%s",start[3].replace("시",""),
                start[4].replace("분",""),end[3].replace("시",""),end[4].replace("분",""));

        String datestr=feedData.timecheck[0].replace("_", " ");

        distxt.setText(diststr);
        steptxt.setText(stepstr);
        timetxt.setText(timestr);
        datetxt.setText(datestr);





        findViewById(R.id.back_feedwrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        curpage=findViewById(R.id.curpage);


        listView=findViewById(R.id.loclist_feed);
        locAdapter locAdapter=new locAdapter(this);
        listView.setAdapter(locAdapter);
        setListViewHeightBasedOnItems(listView);


        viewPager2=findViewById(R.id.viewpager);
        setviewpager();

        findViewById(R.id.finish_feedwrtie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        findViewById(R.id.addimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent galleryintent=new Intent();
                                galleryintent.setType("image/*");
                                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(galleryintent,1);

                            }
                        });
                    }
                }).start();
            }
        });
        findViewById(R.id.delimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fragments.size()<1){
                    return;
                }
                else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fragments.remove(curposition);
                                    setviewpager();
                                    if(fragments.size()==0){
                                        imageback.setVisibility(View.VISIBLE);
                                        imagebacklayout.setBackgroundResource(R.drawable.plusbtn);
                                        curpage.setText("0 / 0");
                                    }
                                    else{
                                        curpage.setText(String.format("%d / %d",curposition+1,fragments.size()));
                                    }
                                    Log.d("피드기록 삭제", ""+fragments.size());
                                }
                            });
                        }
                    }).start();
                }

            }
        });

        emotionbtn=findViewById(R.id.emotion);
        weatherbtn=findViewById(R.id.weather);

        emotionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goemo=new Intent(FeedWrite_Activity.this, SelectFeedActivity.class);
                goemo.putExtra("action",2);
                startActivityForResult(goemo,2);
            }
        });
        weatherbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goweath=new Intent(FeedWrite_Activity.this, SelectFeedActivity.class);
                goweath.putExtra("action",3);
                startActivityForResult(goweath,3);
            }
        });

    }



    public void setviewpager(){

        viewPager2.setAdapter(null);
        fragmentStateAdapter=new MyAdapter(this);
        viewPager2.setAdapter(fragmentStateAdapter);

        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setCurrentItem(curposition,false);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                curposition=position;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                curpage.setText(String.format("%d / %d",curposition+1,fragments.size()));
                            }
                        });
                    }
                }).start();
                Log.d("피드기록 위치",""+position);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==2){
                int tmpemonum=data.getIntExtra("emotion",4);
                switch(tmpemonum){
                    case 0:
                        emotion="angry"; emotionbtn.setImageResource(R.drawable.feeling_angry_xml); break;
                    case 1:
                        emotion="crying"; emotionbtn.setImageResource(R.drawable.feeling_crying_xml);break;
                    case 2:
                        emotion="heart"; emotionbtn.setImageResource(R.drawable.feeling_heart_xml);break;
                    case 3:
                        emotion="neutral"; emotionbtn.setImageResource(R.drawable.feeling_neutral_xml);break;
                    case 4:
                        emotion="smile"; emotionbtn.setImageResource(R.drawable.feeling_smiling_xml);break;
                    case 5:
                        emotion="tired"; emotionbtn.setImageResource(R.drawable.feeling_tired_xml);break;
                    default:
                        emotion="smile"; emotionbtn.setImageResource(R.drawable.feeling_smiling_xml);break;
                }

            }
        else if(requestCode==3){
                int tmpweathnum=data.getIntExtra("weather",6);
                switch (tmpweathnum){
                    case 0:
                        weather="cloudy"; weatherbtn.setImageResource(R.drawable.weather_cloudy_xml);break;
                    case 1:
                        weather="fog";weatherbtn.setImageResource(R.drawable.weather_fog_xml);break;
                    case 2:
                        weather="moon";weatherbtn.setImageResource(R.drawable.weather_moon_xml);break;
                    case 3:
                        weather="rainbow";weatherbtn.setImageResource(R.drawable.weather_rainbow_xml);break;
                    case 4:
                        weather="rainy";weatherbtn.setImageResource(R.drawable.weather_rainy_xml);break;
                    case 5:
                        weather="snow";weatherbtn.setImageResource(R.drawable.weather_snow_xml);break;
                    case 6:
                        weather="sunny";weatherbtn.setImageResource(R.drawable.weather_sunny_xml);break;
                    case 7:
                        weather="windy";weatherbtn.setImageResource(R.drawable.weather_windy_xml);break;
                    default:
                        weather="sunny"; weatherbtn.setImageResource(R.drawable.weather_sunny_xml);break;
                }
            }
            else{
                try{
                    InputStream in=getContentResolver().openInputStream(data.getData());
                    bmp= BitmapFactory.decodeStream(in);
                    fragments.add(new imageFragment(fragments.size()+1,bmp));
                    curposition=fragments.size()-1;
                    setviewpager();
                    imageback.setVisibility(View.INVISIBLE);
                    imagebacklayout.setBackgroundResource(R.color.transparent);
                    curpage.setText(String.format("%d / %d",curposition+1,fragments.size()));
                    in.close();
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

    public class MyAdapter extends FragmentStateAdapter {


        public MyAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public imageFragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }


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

        if(markers.size()>0){
            for(int i=0; i<markers.size(); ++i){
                markers.get(i).setMap(null);
            }
            for(int i=0; i<markers.size(); ++i){
                markers.get(i).setIcon(MarkerIcons.BLACK);
                if(selected==i){
                    markers.get(i).setIconTintColor(Color.RED);
                }
                else{
                    markers.get(i).setIconTintColor(Color.BLUE);
                }
                markers.get(i).setMap(naverMap);
            }
        }

        LatLng[] range=getMiddle(feedData.coordList);
        CameraUpdate cameraUpdate =CameraUpdate.fitBounds(new LatLngBounds(range[0],range[1]),100,200,100,200);
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

    public class locAdapter extends BaseAdapter {

        Context context;
        LayoutInflater layoutInflater;


        public locAdapter(Context context){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return markers.size();
        }

        @Override
        public Object getItem(int i) {
            return markers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            View view=layoutInflater.inflate(R.layout.trip_list_layout, null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(markers.size()==0){
                return null;
            }
            else{
                ImageView imageView=view.findViewById(R.id.trip_image);
                TextView textView=view.findViewById(R.id.triplist);

                if(i==selected){
                    imageView.setImageResource(R.drawable.pin_red);
                }
                else{
                    imageView.setImageResource(R.drawable.pin_blue);
                }

                textView.setText(markers.get(i).getCaptionText());

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(selected==i){
                            selected=-1;
                        }
                        else{
                            selected=i;
                        }
                        notifyDataSetChanged();
                        mapFragmentFeed.getMapAsync(FeedWrite_Activity.this);
                    }
                });

                return view;
            }


        }
    }


    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        FeedWrite_Activity.locAdapter listAdapter = (FeedWrite_Activity.locAdapter) listView.getAdapter();
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
            params.height = (int) ((totalItemsHeight + totalDividersHeight + totalPadding));
            listView.setLayoutParams(params);
            listView.requestLayout();
            //setDynamicHeight(listView);
            return true;

        } else {
            return false;
        }
    }
}