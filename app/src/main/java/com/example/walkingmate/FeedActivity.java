package com.example.walkingmate;

import static com.example.walkingmate.R.id.feedListView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;



import java.util.ArrayList;

public class FeedActivity extends Activity {

    ListView feedListView;

    boolean isFeedexist;

    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_feed);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



        feedListView=findViewById(R.id.feedListView);

        isFeedexist=false;

        Intent getIntent=getIntent();
        int y,m,d; String ys,ms,ds;
        y=getIntent.getIntExtra("year",9999);
        m=getIntent.getIntExtra("month",0);
        d=getIntent.getIntExtra("day",0);
        ys=y+"";
        if(m<10){
            ms="0"+m;
        }
        else{
            ms=m+"";
        }
        if(d<10){
            ds="0"+d;
        }
        else{
            ds=""+d;
        }

        date=ys+"년_"+ms+"월_"+ds+"일_";


        Log.d("checkDate",date);

        FeedData fd=new FeedData();
        ArrayList<String> feedList=new ArrayList<>();

        if(fd.scanFeedList(this)!=null){
            feedList=fd.scanFeedList(this);
            isFeedexist=true;
        }

        ArrayList<String> tmp=new ArrayList<>();
        for(int i=0; i<feedList.size(); ++i){
            if(feedList.get(i).contains(date)){
                tmp.add(feedList.get(i));
            }
        }

        //MapActivity체크용-default로 intent받으면 모든 목록 체크 가능하도록 함
        if(!date.contains("9999년")){
            feedList=tmp;
        }



        if(feedList.size()==0){
            feedList.add("기록된 피드가 존재하지 않습니다.");
        }

        BtnAdapter btnAdapter=new BtnAdapter(getApplicationContext(),feedList, isFeedexist);
        feedListView.setAdapter(btnAdapter);

    }


    public class BtnAdapter extends BaseAdapter{
        Context context;
        LayoutInflater layoutInflater;
        ArrayList<String> data;
        FeedData feedData;
        boolean isFeedexist;

        public BtnAdapter(Context context, ArrayList<String> data, boolean isFeedexist){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.data = data;
            feedData=new FeedData();
            this.isFeedexist=isFeedexist;
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.list_layout, null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(data.get(0).equals("기록된 피드가 존재하지 않습니다.")){
                return emptyview;
            }

            TextView textView = view.findViewById(R.id.datelist);
            textView.setText(convertDateText(data.get(position)));



            View bodyView = view.findViewById(R.id.body);

            bodyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isFeedexist){
                        return;
                    }
                    String filename=data.get(position);
                    Intent goFeedwrite=new Intent(FeedActivity.this, FeedWrite_Activity.class);
                    goFeedwrite.putExtra("filename",filename);
                    startActivity(goFeedwrite);
                }
            });


            Button button=view.findViewById(R.id.listdelbtn);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isFeedexist){
                        return;
                    }
                    String filename=data.get(position);
                    feedData.deletefeed(filename,FeedActivity.this);
                    data.remove(filename);
                    if(data.size()==0){
                        isFeedexist=false;
                        data.add("기록된 피드가 존재하지 않습니다.");
                    }
                    notifyDataSetChanged();

                }
            });

            return view;

        }
    }

    public String convertDateText(String day){
        String result=day;
        result=result.replaceAll("_"," ");
        result=result.replace(".txt","");
        return result;
    }


}