package com.example.walkingmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class TripFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference tripdata=fb.collection("tripdatalist");
    CollectionReference users=fb.collection("users");

    SwipeRefreshLayout swipeRefreshLayout;

    ListView triplist;
    TripAdapter tripAdapter;

    ImageButton addtrip,scrollup;
    String curitem="30001112093121";

    ArrayList<String> tripdocuids=new ArrayList<>();
    HashMap<String, String> userids=new HashMap<>();
    HashMap<String,String> titles=new HashMap<>();
    HashMap<String, String> dates=new HashMap<>();
    HashMap<String, ArrayList<String>> locations=new HashMap<>();
    HashMap<String, String> writetimes=new HashMap<>();
    HashMap<String, String> writers=new HashMap<>();

    boolean addbool=false;//최하단 스크롤시 getlist여러번 실행되는 오류막기위한 불리안
    boolean backbool=false;//게시물 추가후 돌아왔을때 새로고침시 중복실행 막기위한 불리안
    boolean checkin=false;

    View rootview;

    boolean searchopen=false;

    ArrayList<String> Selectedlocation=new ArrayList<>();

    CheckBox r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,r13,r14,r15,r16;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview= inflater.inflate(R.layout.fragment_trip, container, false);

        swipeRefreshLayout=rootview.findViewById(R.id.refresh_triplist);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        rootview.findViewById(R.id.search_tripfrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visible;
                if(searchopen){
                    visible=View.INVISIBLE;
                    searchopen=false;
                }
                else{
                    visible=View.VISIBLE;
                    searchopen=true;
                }
                rootview.findViewById(R.id.searchlayout_tripfrag).setVisibility(visible);
            }
        });

        triplist=rootview.findViewById(R.id.triplist);
        addtrip=rootview.findViewById(R.id.add_triplist);
        addtrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),TripwriteActivity.class));
                backbool=true;
            }
        });

        scrollup=rootview.findViewById(R.id.up_triplist);

        tripAdapter=new TripAdapter(getContext());
        triplist.setAdapter(tripAdapter);
        getlist();

        scrollup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tripdocuids.size()>40){
                    triplist.setSelection(0);
                }
                else{
                    triplist.smoothScrollToPosition(0);
                }

            }
        });

        triplist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(!triplist.canScrollVertically(1)){
                    if(!addbool){
                        addbool=true;
                        getlist();
                    }

                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });


        r1=rootview.findViewById(R.id.radio1);
        r1.setChecked(true);
        r1.setOnClickListener(checkboxlistener);



        return rootview;
    }

    CheckBox.OnClickListener checkboxlistener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean ischecked=((CheckBox)view).isChecked();
            String location=((CheckBox)view).getText().toString();
            if(ischecked){
                Toast.makeText(getContext(),"추가"+location,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getContext(),"제거"+location,Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void refreshs(){
        curitem="30001112093121";
        tripdocuids.clear();
        userids.clear();
        titles.clear();
        dates.clear();
        locations.clear();
        writetimes.clear();
        writers.clear();
        getlist();
    }

    //필터 추가시 쿼리적용
    public void getlist(){





        Log.d("여행 최하단 게시물 시작전",curitem);
        tripdata.whereLessThan("writetime",curitem).orderBy("writetime", Query.Direction.DESCENDING).limit(15).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    QuerySnapshot documents=task.getResult();
                    for(DocumentSnapshot document: documents){
                        tripdocuids.add(document.getId());
                        titles.put(document.getId(), (String) document.get("title"));
                        userids.put(document.getId(), (String) document.get("userid"));
                        try {
                            dates.put(document.getId(),getdatestr(document.get("year"),document.get("month"),document.get("day"),document.get("hour"),document.get("minute"),document.get("takentime")));
                        } catch (ParseException e) {
                            dates.put(document.getId()," ");
                        }
                        locations.put(document.getId(), (ArrayList<String>) document.get("locations_name"));
                        writetimes.put(document.getId(), (String) document.get("writetime"));
                    }
                    curitem=writetimes.get(tripdocuids.get(tripdocuids.size()-1));
                    Log.d("여행 최하단 게시물",curitem);
                    for(String s:tripdocuids){
                        users.document(userids.get(s)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Log.d("여행 유저 추가", (String) task.getResult().get("appname"));
                                writers.put(s, (String) task.getResult().get("appname"));
                                tripAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    if(addbool){
                        addbool=false;
                    }
                    if(backbool){
                        backbool=false;
                    }

                    Log.d("여행리스트수",tripdocuids.size()+"");
                }
            }
        });
    }

    public String getdatestr(Object y, Object m, Object d, Object h, Object min, Object taken) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String start=String.format("%04d/%02d/%02d %02d:%02d",y,m,d,h,min);
        Date date=sdf.parse(start);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE,Long.valueOf((Long) Optional.ofNullable(taken).orElse(0L)).intValue());
        return start +"~"+ sdf.format(calendar.getTime());
    }

    @Override
    public void onRefresh() {
        refreshs();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(backbool){
            refreshs();
        }
        if(checkin){
            checkdelete();
            checkin=false;
        }

    }

    public void checkdelete(){
        for(String s:tripdocuids){
            tripdata.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(!task.getResult().exists()){
                        tripdocuids.remove(s);
                        tripAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public class TripAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;


        public TripAdapter(Context context){
            this.layoutInflater = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return tripdocuids.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.layout_triplist, null);
            View emptyview=layoutInflater.inflate(R.layout.empty_layout,null);
            if(tripdocuids.size()==0){
                return emptyview;
            }

            View bodyView = view.findViewById(R.id.trip_body);
            try{
                bodyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tripdata.document(tripdocuids.get(position)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Toast.makeText(getActivity(),"존재하지 않는 게시물입니다.",Toast.LENGTH_SHORT).show();
                                    tripdocuids.remove(tripdocuids.get(position));
                                    notifyDataSetChanged();
                                    return;
                                }
                                else{
                                    checkin=true;
                                    Intent intent=new Intent(getActivity(),TripViewActivity.class);
                                    intent.putExtra("docuid",tripdocuids.get(position));
                                    intent.putExtra("date",dates.get(tripdocuids.get(position)));
                                    intent.putExtra("userid",userids.get(tripdocuids.get(position)));
                                    startActivity(intent);
                                }
                            }
                        });

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }


            TextView title=view.findViewById(R.id.trip_title);
            TextView datetxt=view.findViewById(R.id.trip_date);
            TextView writetime=view.findViewById(R.id.trip_writetime);
            TextView writer=view.findViewById(R.id.trip_writer);

            title.setText(titles.get(tripdocuids.get(position)));
            datetxt.setText(dates.get(tripdocuids.get(position)));
            String timetmp=writetimes.get(tripdocuids.get(position));
            writetime.setText(String.format("%s/%s/%s %s:%s",timetmp.substring(0,4),timetmp.substring(4,6),timetmp.substring(6,8),timetmp.substring(8,10),timetmp.substring(10,12)));
            writer.setText(writers.get(tripdocuids.get(position)));

            return view;

        }
    }
}