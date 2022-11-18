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
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class TripFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference tripdata=fb.collection("tripdatalist");
    CollectionReference users=fb.collection("users");
    CollectionReference blocklist=fb.collection("blocklist");

    SwipeRefreshLayout swipeRefreshLayout;

    ListView triplist;
    TripAdapter tripAdapter;

    ImageButton addtrip,scrollup;
    String curitem="30001112093121";

    ArrayList<String> alldocuids=new ArrayList<>();
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

    UserData userData;

    View rootview;

    boolean searchopen=false;

    ArrayList<String> Selectedlocation=new ArrayList<>();

    CheckBox[] checkBoxes=new CheckBox[16];

    Spinner sex,age;

    boolean nomore=true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview= inflater.inflate(R.layout.fragment_trip, container, false);

        swipeRefreshLayout=rootview.findViewById(R.id.refresh_triplist);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        sex=rootview.findViewById(R.id.spinner_sex_tripfrag);
        age=rootview.findViewById(R.id.spinner_age_tripfrag);

        userData=UserData.loadData(getActivity());

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


        checkBoxes[0]=rootview.findViewById(R.id.radio1);
        checkBoxes[0].setChecked(true);
        Selectedlocation.add(checkBoxes[0].getText().toString());

        checkBoxes[1]=rootview.findViewById(R.id.radio2);checkBoxes[8]=rootview.findViewById(R.id.radio9);
        checkBoxes[2]=rootview.findViewById(R.id.radio3);checkBoxes[9]=rootview.findViewById(R.id.radio10);
        checkBoxes[3]=rootview.findViewById(R.id.radio4);checkBoxes[10]=rootview.findViewById(R.id.radio11);
        checkBoxes[4]=rootview.findViewById(R.id.radio5);checkBoxes[11]=rootview.findViewById(R.id.radio12);
        checkBoxes[5]=rootview.findViewById(R.id.radio6);checkBoxes[12]=rootview.findViewById(R.id.radio13);
        checkBoxes[6]=rootview.findViewById(R.id.radio7);checkBoxes[13]=rootview.findViewById(R.id.radio14);
        checkBoxes[7]=rootview.findViewById(R.id.radio8);checkBoxes[14]=rootview.findViewById(R.id.radio15);
        checkBoxes[15]=rootview.findViewById(R.id.radio16);
        for(CheckBox checkBox:checkBoxes){
            checkBox.setOnClickListener(checkboxlistener);
        }

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



        return rootview;
    }


    CheckBox.OnClickListener checkboxlistener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean ischecked=((CheckBox)view).isChecked();
            String location=((CheckBox)view).getText().toString();
            if(ischecked){
                if(!Selectedlocation.contains(location)){
                    Selectedlocation.add(location);
                }

            }
            else{
                if(Selectedlocation.contains(location)){
                    Selectedlocation.remove(location);
                }
            }
            Log.d("검색 필터",Selectedlocation.toString());
        }
    };


    public void refreshs(){
        nomore=true;
        curitem="30001112093121";
        tripdocuids.clear();
        userids.clear();
        titles.clear();
        dates.clear();
        locations.clear();
        writetimes.clear();
        writers.clear();
        alldocuids.clear();
        getlist();
    }

    //차단, 연령대, 성별 체크 (상대 기준)
    public boolean checkfilter(DocumentSnapshot document, ArrayList<String> myblockuser){
        //내 차단목록 체크
        if(myblockuser.contains(document.getString("userid"))){
            return false;
        }

        //연령대 체크
        String agefilter=document.getString("age");
        if(!agefilter.equals("무관")){
            if(!agefilter.equals(userData.age)){
                return false;
            }
        }

        //성별 체크
        String mygender;
        if(userData.gender.equals("M")){
            mygender="남성";
        }
        else{
            mygender="여성";
        }
        String genderfilter=document.getString("blockgender");
        if(genderfilter.equals(mygender)){
            return false;
        }

        return true;

    }

    //필터 추가시 쿼리적용
    public void getlist(){

        if(tripdocuids.contains("last")){
            tripdocuids.remove("last");
        }
        Query query=tripdata;

        if(Selectedlocation.size()==0){
            Toast.makeText(getActivity(),"지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Selectedlocation.contains("전체")){
            query=query.whereIn("startlocation",Selectedlocation);
            Log.d("지역 필터",Selectedlocation.toString());
        }
        else{
            Log.d("지역 필터","없음");
        }

        String gender=sex.getSelectedItem().toString(),ages=age.getSelectedItem().toString();

        //내 필터
        if(!gender.equals("무관")){
            query=query.whereEqualTo("usergender",gender);
            Log.d("성별 필터",gender);
        }
        else{
            Log.d("성별 필터","없음");
        }
        if(!ages.equals("무관")){
            query=query.whereEqualTo("userage",ages);
            Log.d("연령 필터",ages);
        }
        else{
            Log.d("연령 필터","없음");
        }


        Log.d("여행 쿼리","gen:"+gender+", age:"+ages+", locations:"+Selectedlocation.toString());
        Log.d("여행 최하단 게시물 시작전",curitem);

        ArrayList<String> myblockusers=new ArrayList<>();
        Query finalQuery = query;

        //내 차단 유저 목록
        blocklist.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d("파이어베이스에러_차단",1+"");
                    return;
                }
                if(task.getResult().exists()){
                    DocumentSnapshot document=task.getResult();
                    for(String s:(ArrayList<String>)document.get("userid")){
                        myblockusers.add(s);
                    }
                }

                //날 차단한 유저목록
                blocklist.whereArrayContains("userid",userData.userid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(!task.isSuccessful()){
                            Log.d("파이어베이스에러",42+"");
                            return;
                        }
                        if(!task.getResult().getDocuments().isEmpty()){
                            ArrayList<DocumentSnapshot> documents= (ArrayList<DocumentSnapshot>) task.getResult().getDocuments();
                            for(DocumentSnapshot document: documents){
                                myblockusers.add(document.getId());
                            }
                        }

                        //쿼리로 데이터 받아옴
                        finalQuery.whereLessThan("writetime",curitem).orderBy("writetime", Query.Direction.DESCENDING).limit(50).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(!task.isSuccessful()){
                                    Log.d("파이어베이스에러_여행",1+"");
                                    return;
                                }
                                if(!task.getResult().isEmpty()){
                                    QuerySnapshot documents=task.getResult();
                                    for(DocumentSnapshot document: documents){

                                        boolean req=checkfilter(document,myblockusers);

                                        //필터링에 걸린 문서만 넣음
                                        if(req){
                                            tripdocuids.add(document.getId());
                                        }
                                        //모든 문서 아이디 넣음
                                        alldocuids.add(document.getId());

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
                                    curitem=writetimes.get(alldocuids.get(alldocuids.size()-1));
                                    Log.d("여행 최하단 게시물",curitem);
                                    for(String s:tripdocuids){
                                        if(s.equals("last")){
                                            continue;
                                        }
                                        users.document(userids.get(s)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                Log.d("여행 유저 추가", (String) task.getResult().get("appname"));
                                                writers.put(s, (String) task.getResult().get("appname"));
                                                tripAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                    tripdocuids.add("last");
                                    tripAdapter.notifyDataSetChanged();
                                    if(addbool){
                                        addbool=false;
                                    }
                                    if(backbool){
                                        backbool=false;
                                    }
                                    Log.d("여행리스트수",tripdocuids.size()+"");
                                }
                                else{
                                    tripdocuids.add("last");
                                    nomore=false;
                                    tripAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
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
            if(s.equals("last")){
                continue;
            }
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
            View lastview=layoutInflater.inflate(R.layout.layout_lasttriplist,null);
            if(tripdocuids.size()==0){
                return emptyview;
            }
            if(tripdocuids.get(position).equals("last")){
                TextView lasttxt=lastview.findViewById(R.id.lastviewtxt);
                lasttxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!addbool){
                            addbool=true;
                            getlist();
                        }
                    }
                });
                if(!nomore){
                    lasttxt.setText("더 이상 게시물이 존재하지 않습니다.");
                }
                return lastview;
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