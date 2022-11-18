package com.example.walkingmate;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Map;
import java.util.Optional;

public class TripFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference tripdata=fb.collection("tripdatalist");
    CollectionReference users=fb.collection("users");
    CollectionReference blocklist=fb.collection("blocklist");
    CollectionReference reqlist=fb.collection("triprequest");

    SwipeRefreshLayout swipeRefreshLayout;

    ListView triplist;
    TripAdapter tripAdapter;
    MyTripAdapter mytripAdapter;
    MyReqTripAdapter myreqtripAdapter;

    ImageButton addtrip,scrollup;
    String curitem="30001112093121";

    ArrayList<String> alldocuids=new ArrayList<>();//전체 게시물 가져올떄 모든 아이디
    ArrayList<String> tripdocuids=new ArrayList<>();//필터링된 전체게시물
    ArrayList<String> mydocuids=new ArrayList<>();//내 게시물
    ArrayList<String> myreqdocuids=new ArrayList<>();//내가 신청한 게시물
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

    ImageButton startcalendar,endcalendar, searchopenbtn;
    EditText syear,smonth,sday,eyear,emonth,eday;

    Button clearSetting;

    String startstr,endstr;

    Spinner viewmode;
    int viewmodenum=2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview= inflater.inflate(R.layout.fragment_trip, container, false);

        swipeRefreshLayout=rootview.findViewById(R.id.refresh_triplist);
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);

        startstr="1000/01/01";
        endstr="9999/12/31";

        sex=rootview.findViewById(R.id.spinner_sex_tripfrag);
        age=rootview.findViewById(R.id.spinner_age_tripfrag);

        startcalendar=rootview.findViewById(R.id.start_tripfrag);
        endcalendar=rootview.findViewById(R.id.end_tripfrag);
        syear=rootview.findViewById(R.id.year_start);
        smonth=rootview.findViewById(R.id.month_start);
        sday=rootview.findViewById(R.id.day_start);
        eyear=rootview.findViewById(R.id.year_end);
        emonth=rootview.findViewById(R.id.month_end);
        eday=rootview.findViewById(R.id.day_end);
        viewmode=rootview.findViewById(R.id.spinner_viewmode_tripfrag);

        viewmode.setSelection(2);



        startcalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getstartResult.launch(new Intent(getActivity(),DateSelector.class));
            }
        });

        endcalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getendResult.launch(new Intent(getActivity(),DateSelector.class));
            }
        });




        userData=UserData.loadData(getActivity());

        searchopenbtn=rootview.findViewById(R.id.search_tripfrag);
        searchopenbtn.setOnClickListener(new View.OnClickListener() {
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
        mytripAdapter=new MyTripAdapter(getContext());
        myreqtripAdapter=new MyReqTripAdapter(getContext());
        triplist.setAdapter(tripAdapter);
        getlist();
        getmylist();
        getmyreqlist();

        viewmode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                viewmodenum=i;
                if(viewmodenum!=2){
                    if(searchopen){
                        searchopenbtn.performClick();
                    }
                    searchopenbtn.setVisibility(View.INVISIBLE);
                }
                else{
                    searchopenbtn.setVisibility(View.VISIBLE);
                }
                switch (i){
                    case 2:
                        triplist.setAdapter(tripAdapter); break;
                    case 1:
                        triplist.setAdapter(myreqtripAdapter); break;
                    case 0:
                        Log.d("내 게시물",mydocuids.toString());
                        triplist.setAdapter(mytripAdapter); break;
                    default:
                        triplist.setAdapter(tripAdapter); break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                if(viewmodenum!=2){
                    return;
                }
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

        rootview.findViewById(R.id.finishSetting_tripfrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmps,tmpe;
                tmps=String.format("%s/%s/%s",syear.getText().toString(),smonth.getText().toString(),sday.getText().toString());
                tmpe=String.format("%s/%s/%s",eyear.getText().toString(),emonth.getText().toString(),eday.getText().toString());
                //비어있으면 제한선 없는것으로 만들음
                Log.d("날짜 체크",tmps+","+tmpe);
                if(tmps.equals("//")){
                    tmps="1000/01/01";
                }
                if(tmpe.equals("//")){
                    tmpe="9999/12/31";
                }
                if(!checkdate(tmps,tmpe)){
                    Toast.makeText(getActivity(),"잘못된 날짜를 입력하셨습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    startstr=tmps;
                    endstr=tmpe;
                }

                if(!addbool){
                    Toast.makeText(getActivity(),"설정 완료되었습니다.",Toast.LENGTH_SHORT).show();
                    addbool=true;
                    refreshs();
                }


            }
        });

        clearSetting=rootview.findViewById(R.id.clearSetting_tripfrag);
        clearSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startstr="1000/01/01";
                endstr="9999/12/31";
                for(CheckBox checkBox:checkBoxes){
                    checkBox.setChecked(false);
                }
                checkBoxes[0].setChecked(true);
                Selectedlocation.clear();
                Selectedlocation.add("전체");
                syear.setText("");
                smonth.setText("");
                sday.setText("");
                eyear.setText("");
                emonth.setText("");
                eday.setText("");
                sex.setSelection(0);
                age.setSelection(0);
            }
        });



        return rootview;
    }

    //포멧 형식과 전후 관계 체크(시작일과 끝일이 같으면 true;)
    public boolean checkdate(String start, String end){
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
            sdf.setLenient(false);
            Date sdate,edate;

            sdate=sdf.parse(start);
            edate=sdf.parse(end);

            if(edate.before(sdate)){
                return false;
            }

        }catch (Exception e){
            return false;
        }
        return true;

    }


    private final ActivityResultLauncher<Intent> getstartResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if(result.getData()!=null){
                        syear.setText(result.getData().getIntExtra("mYear",2022)+"");
                        smonth.setText(result.getData().getIntExtra("mMonth",12)+"");
                        sday.setText(result.getData().getIntExtra("mDay",1)+"");
                    }
                }
            });

    private final ActivityResultLauncher<Intent> getendResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    if(result.getData()!=null){
                        eyear.setText(result.getData().getIntExtra("mYear",2022)+"");
                        emonth.setText(result.getData().getIntExtra("mMonth",12)+"");
                        eday.setText(result.getData().getIntExtra("mDay",1)+"");
                    }
                }
            });



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

        userids.clear();
        titles.clear();
        dates.clear();
        locations.clear();
        writetimes.clear();
        writers.clear();

        tripdocuids.clear();
        alldocuids.clear();
        mydocuids.clear();
        myreqdocuids.clear();

        getlist();
        getmylist();
        getmyreqlist();
    }

    //차단, 연령대, 성별(상대 기준), 날짜 체크
    //파이어베이스 중복실행 에러로 여러번 들어오는 경우 체크
    public boolean checkfilter(DocumentSnapshot document, ArrayList<String> myblockuser){
        //내 차단목록 체크
        if(myblockuser.contains(document.getString("userid"))){
            return false;
        }

        if(tripdocuids.contains(document.getId())){
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

        //시작일 체크:설정시작일<=문서 시작일
        if(!checkdate(startstr,document.getString("starttime"))){
            return false;
        }

        //종료일 체크: 문서 종료일<=설정종료일
        if(!checkdate(document.getString("endtime"),endstr)){
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
                                    if(!tripdocuids.contains("last")){
                                        tripdocuids.add("last");
                                    }

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
                                    if(!tripdocuids.contains("last")){
                                        tripdocuids.add("last");
                                    }
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

    public void getmylist(){
        Log.d("내 리스트 진입","1");
        tripdata.whereEqualTo("userid",userData.userid).orderBy("writetime", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d("파이어베이스에러_여행",1+"");
                    return;
                }
                if(!task.getResult().isEmpty()){
                    Log.d("내 리스트 진입","2");
                    QuerySnapshot documents=task.getResult();
                    for(DocumentSnapshot document: documents){
                        if(mydocuids.contains(document.getId())){
                            continue;
                        }
                        else{
                            mydocuids.add(document.getId());
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
                        mytripAdapter.notifyDataSetChanged();

                    }
                }
                else{
                    Log.d("내 리스트 진입","3");
                    if(mydocuids.size()==0){
                        mydocuids.add("last");
                        mytripAdapter.notifyDataSetChanged();
                        Log.d("내 리스트 진입","4");
                    }
                }
            }
        });
    }

    public void getmyreqlist(){
        Log.d("내 신청 여행 진입체크","0");
        reqlist.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d("파이어베이스에러",1+"");
                    return;
                }
                DocumentSnapshot document=task.getResult();
                ArrayList<String> docuids= (ArrayList<String>) document.get("requestlist");
                if(docuids==null||docuids.size()==0){
                    if(myreqdocuids.size()==0){
                        Log.d("내 신청 여행 진입체크","1");
                        myreqdocuids.add("last");
                        myreqtripAdapter.notifyDataSetChanged();
                    }
                }
                if(docuids!=null){
                    if(docuids.size()>0){
                        Log.d("내 신청 여행 진입체크","2");
                        for(String s:docuids){
                            tripdata.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document=task.getResult();

                                        if(!myreqdocuids.contains(document.getId())){
                                            myreqdocuids.add(document.getId());
                                            titles.put(document.getId(), (String) document.get("title"));
                                            userids.put(document.getId(), (String) document.get("userid"));
                                            try {
                                                dates.put(document.getId(),getdatestr(document.get("year"),document.get("month"),document.get("day"),document.get("hour"),document.get("minute"),document.get("takentime")));
                                            } catch (ParseException e) {
                                                dates.put(document.getId()," ");
                                            }
                                            locations.put(document.getId(), (ArrayList<String>) document.get("locations_name"));
                                            writetimes.put(document.getId(), (String) document.get("writetime"));
                                            myreqtripAdapter.notifyDataSetChanged();
                                            Log.d("내 신청 여행 진입체크","3:"+document.getString("title"));

                                            users.document(document.getString("userid")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    Log.d("여행 유저 추가", (String) task.getResult().get("appname"));
                                                    writers.put(document.getId(), (String) task.getResult().get("appname"));
                                                    myreqtripAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
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
        for(String s:mydocuids){
            if(s.equals("last")){
                continue;
            }
            tripdata.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(!task.getResult().exists()){
                        mydocuids.remove(s);
                        if(mydocuids.size()==0){
                            mydocuids.add("last");
                        }
                        mytripAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        for(String s:myreqdocuids){
            if(s.equals("last")){
                continue;
            }
            tripdata.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(!task.getResult().exists()){
                        myreqdocuids.remove(s);
                        if(myreqdocuids.size()==0){
                            myreqdocuids.add("last");
                        }
                        myreqtripAdapter.notifyDataSetChanged();
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
                else if(alldocuids.size()>0){
                    String lasttime=String.format("%s/%s/%s 이전 게시물\n클릭하여 추가로 게시물 불러오기",
                            curitem.substring(0,4),curitem.substring(4,6),curitem.substring(6,8));
                    lasttxt.setText(lasttime);
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

    public class MyTripAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;


        public MyTripAdapter(Context context){
            this.layoutInflater = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return mydocuids.size();
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
            if(mydocuids.size()==0){
                return emptyview;
            }
            if(mydocuids.get(position).equals("last")){
                TextView lasttxt=lastview.findViewById(R.id.lastviewtxt);
                lasttxt.setText("게시물이 존재하지 않습니다.");
                return lastview;
            }

            View bodyView = view.findViewById(R.id.trip_body);
            try{
                bodyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tripdata.document(mydocuids.get(position)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Toast.makeText(getActivity(),"존재하지 않는 게시물입니다.",Toast.LENGTH_SHORT).show();
                                    mydocuids.remove(mydocuids.get(position));
                                    notifyDataSetChanged();
                                    return;
                                }
                                else{
                                    checkin=true;
                                    Intent intent=new Intent(getActivity(),TripViewActivity.class);
                                    intent.putExtra("docuid",mydocuids.get(position));
                                    intent.putExtra("date",dates.get(mydocuids.get(position)));
                                    intent.putExtra("userid",userids.get(mydocuids.get(position)));
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

            title.setText(titles.get(mydocuids.get(position)));
            datetxt.setText(dates.get(mydocuids.get(position)));
            String timetmp=writetimes.get(mydocuids.get(position));
            writetime.setText(String.format("%s/%s/%s %s:%s",timetmp.substring(0,4),timetmp.substring(4,6),timetmp.substring(6,8),timetmp.substring(8,10),timetmp.substring(10,12)));
            writer.setText(userData.appname);

            return view;

        }
    }

    public class MyReqTripAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;


        public MyReqTripAdapter(Context context){
            this.layoutInflater = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return myreqdocuids.size();
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
            if(myreqdocuids.size()==0){
                return emptyview;
            }
            if(myreqdocuids.get(position).equals("last")){
                TextView lasttxt=lastview.findViewById(R.id.lastviewtxt);
                lasttxt.setText("신청한 게시물이 존재하지 않습니다.");
                return lastview;
            }

            View bodyView = view.findViewById(R.id.trip_body);
            try{
                bodyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tripdata.document(myreqdocuids.get(position)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Toast.makeText(getActivity(),"존재하지 않는 게시물입니다.",Toast.LENGTH_SHORT).show();
                                    myreqdocuids.remove(myreqdocuids.get(position));
                                    notifyDataSetChanged();
                                    return;
                                }
                                else{
                                    checkin=true;
                                    Intent intent=new Intent(getActivity(),TripViewActivity.class);
                                    intent.putExtra("docuid",myreqdocuids.get(position));
                                    intent.putExtra("date",dates.get(myreqdocuids.get(position)));
                                    intent.putExtra("userid",userids.get(myreqdocuids.get(position)));
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

            title.setText(titles.get(myreqdocuids.get(position)));
            datetxt.setText(dates.get(myreqdocuids.get(position)));
            String timetmp=writetimes.get(myreqdocuids.get(position));
            writetime.setText(String.format("%s/%s/%s %s:%s",timetmp.substring(0,4),timetmp.substring(4,6),timetmp.substring(6,8),timetmp.substring(8,10),timetmp.substring(10,12)));
            writer.setText(writers.get(myreqdocuids.get(position)));

            return view;

        }
    }
}