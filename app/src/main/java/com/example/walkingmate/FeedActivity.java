package com.example.walkingmate;

import static com.example.walkingmate.R.id.feedListView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class FeedActivity extends Activity {

    ListView feedListView;

    boolean isFeedexist;

    String date;

    int iswrite;

    UserData userData;

    BtnAdapter btnAdapter;
    FeedAdapter feedAdapter;

    String others=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_feed);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userData=UserData.loadData(this);

        feedListView=findViewById(R.id.feedListView);

        isFeedexist=false;

        Intent getIntent=getIntent();

        //미작성인지 작성인지 체크, 1이면 미작성, 2이면 작성
        iswrite=getIntent.getIntExtra("iswrite",1);
        Log.d("피드_종류",iswrite+"");

        others=getIntent.getStringExtra("others");


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

        Log.d("피드_종류",iswrite+"/"+date);


        Log.d("checkDate",date);


        ArrayList<String> feedList=new ArrayList<>();
        if(iswrite==1){
            FeedData fd=new FeedData();

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

            btnAdapter=new BtnAdapter(getApplicationContext(),feedList, isFeedexist);
            feedListView.setAdapter(btnAdapter);

        }
        else{
            receiveData(y,m,d);
        }



    }

    public void receiveData(int year,int month,int day){
        FirebaseFirestore fb=FirebaseFirestore.getInstance();
        CollectionReference walklist=fb.collection("feedlist");
        final boolean[] isFeedexist = {true};

        ArrayList<String> result=new ArrayList<>();
        ArrayList<String> resulttime=new ArrayList<>();
        ArrayList<String> docuid=new ArrayList<>();

        String userid=userData.userid;
        if(others!=null){
            userid=others;
        }

        if(year==9999){
            Log.d("피드리스트","모든날 진입");
            walklist.whereEqualTo("userid",userid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(int i=0; i<task.getResult().size(); ++i){
                                if(task.getResult().getDocuments().get(i).get("isOpen")==null||(Boolean) task.getResult().getDocuments().get(i).get("isOpen")){
                                    result.add((String) task.getResult().getDocuments().get(i).get("title"));
                                    resulttime.add((String) task.getResult().getDocuments().get(i).get("writetime"));
                                    docuid.add((String) task.getResult().getDocuments().get(i).getId());
                                }

                            }
                            if(result.size()==0){
                                isFeedexist[0] =false;
                                result.add("기록된 피드가 존재하지 않습니다.");
                            }
                            feedAdapter=new FeedAdapter(getApplicationContext(),result, isFeedexist[0],resulttime,docuid);
                            feedListView.setAdapter(feedAdapter);
                        }
                    });
        }
        else{
            Log.d("피드리스트","특정날 진입");
            walklist.whereEqualTo("userid",userData.userid).whereEqualTo("day",day).whereEqualTo("month",month).
                    whereEqualTo("year",year).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(int i=0; i<task.getResult().size(); ++i){
                                result.add((String) task.getResult().getDocuments().get(i).get("title"));
                                resulttime.add((String) task.getResult().getDocuments().get(i).get("writetime"));
                                docuid.add((String) task.getResult().getDocuments().get(i).getId());
                            }
                            if(result.size()==0){
                                isFeedexist[0] =false;
                                result.add("기록된 피드가 존재하지 않습니다.");
                            }
                            feedAdapter=new FeedAdapter(getApplicationContext(),result, isFeedexist[0],resulttime,docuid);
                            feedListView.setAdapter(feedAdapter);
                        }
                    });
        }
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

    public class FeedAdapter extends BaseAdapter{
        Context context;
        LayoutInflater layoutInflater;
        ArrayList<String> data;
        boolean isFeedexist;
        ArrayList<String> times;
        ArrayList<String> docuid;

        public FeedAdapter(Context context, ArrayList<String> data, boolean isFeedexist, ArrayList<String> times,ArrayList<String> docuid){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.data = data;
            this.isFeedexist=isFeedexist;
            this.times=times;
            this.docuid=docuid;
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
            View view = layoutInflater.inflate(R.layout.feedlist_layout,null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(data.get(0).equals("기록된 피드가 존재하지 않습니다.")){
                return emptyview;
            }

            TextView textView = view.findViewById(R.id.datelistfeed);
            textView.setText(convertDateText(data.get(position)));

            TextView writetime=view.findViewById(R.id.writedate);
            String wy=times.get(position).substring(0,4);
            String wm=times.get(position).substring(4,6);
            String wd=times.get(position).substring(6,8);
            writetime.setText("작성 날짜: "+wy+"-"+wm+"-"+wd);



            View bodyView = view.findViewById(R.id.bodyfeed);

            bodyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isFeedexist){
                        return;
                    }
                    String filename=docuid.get(position);
                    Intent goFeedwrite=new Intent(FeedActivity.this, ViewFeedActivity.class);
                    goFeedwrite.putExtra("filename",filename);
                    startActivity(goFeedwrite);
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