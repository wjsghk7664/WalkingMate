package com.example.walkingmate;

import static androidx.annotation.Dimension.DP;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import org.checkerframework.checker.units.qual.A;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalkFragment extends Fragment implements OnMapReadyCallback{
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference walkdata=fb.collection("walkdata");
    CollectionReference udata=fb.collection("users");
    CollectionReference walkuser=fb.collection("walkuser");
    CollectionReference walkreq=fb.collection("walkrequest");
    CollectionReference block=fb.collection("blocklist");

    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NaverMap naverMap;
    private MapView mapView;

    UserData userData;
    LatLng setLocation;

    ImageButton refresh,addwalk, close;
    Spinner gender, age;

    Button mywalk, mate;

    CircleImageView userImage;
    TextView title,usertxt,time,locationtxt;
    LinearLayout userprofile;

    String genderstr, agestr;

    ArrayList<String> idlist=new ArrayList<>();
    ArrayList<String> docuidlist=new ArrayList<>();
    ArrayList<LatLng> coordlist=new ArrayList<>();
    ArrayList<String> timelist=new ArrayList<>(); //yyyyMMddhhmm형식으로
    ArrayList<String> locationlist=new ArrayList<>();

    ArrayList<Marker> markers=new ArrayList<>();

    ArrayList<Marker> mymarker=new ArrayList<>();
    ArrayList<LatLng> mycoordlist=new ArrayList<>();
    ArrayList<String> mydaylist=new ArrayList<>();
    ArrayList<String> mytimelist=new ArrayList<>();
    ArrayList<String> mydocuidlist=new ArrayList<>();

    Map<String,Long> myreqlist=new HashMap<>();

    boolean firstsync=true;
    int mywalkcheck=0; //0내글 제외, 1 내글만, 2 신청한 글, 3 전체

    Bitmap retBitmap = null;//프로필에 띄울 이미지
    String curdocu=""; //현재 클릭한 게시물 문서아이디



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_walk, container, false);

        setLocation=new LatLng(37.5666103, 126.9783882);

        userImage=root.findViewById(R.id.profileImage);
        title=root.findViewById(R.id.walkview_title);
        usertxt=root.findViewById(R.id.walkview_user);
        time=root.findViewById(R.id.walkview_time);
        userprofile=root.findViewById(R.id.userprofile_walkview);
        locationtxt=root.findViewById(R.id.walkview_location);

        genderstr="무관";
        agestr="무관";

        userData=UserData.loadData(getActivity());
        mapView=root.findViewById(R.id.fragmap);
        mapView.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //시작위치부터 좌표모음 시작
                    setLocation=new LatLng(location.getLatitude(),location.getLongitude());
                }
            }
        });

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);//현재위치값 받아옴

        getdata();
        getmydata();
        myreq();

        age=root.findViewById(R.id.spinner_age_walkmap);
        gender=root.findViewById(R.id.spinner_sex_walkmap);

        refresh=root.findViewById(R.id.refresh_walkingmap);
        mywalk=root.findViewById(R.id.seemy_walkview);
        addwalk=root.findViewById(R.id.add_walkview);
        mate=root.findViewById(R.id.mate_walkview);
        close=root.findViewById(R.id.close_profile);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agestr=age.getSelectedItem().toString();
                genderstr=gender.getSelectedItem().toString();
                Log.d("산책_버튼","테스트:"+agestr+"_"+genderstr);
                refreshdata();
            }
        });

        mywalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //처음(0):내글 제외
                //1:내글만
                refreshdata();
                if(mywalkcheck==0){
                    mywalk.setTextColor(Color.BLUE);
                    mywalkcheck=1;
                    mapsync();
                }
                else if(mywalkcheck==1){
                    mywalk.setTextColor(Color.RED);
                    mywalkcheck=2;
                    mapsync();
                }
                else if(mywalkcheck==2){
                    mywalk.setTextColor(Color.MAGENTA);
                    mywalkcheck=3;
                    mapsync();
                }
                else if(mywalkcheck==3){
                    mywalk.setTextColor(Color.WHITE);
                    mywalkcheck=0;
                    mapsync();
                }
            }
        });

        addwalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),WalkWriteActivity.class));
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curdocu="";
                userprofile.setVisibility(View.INVISIBLE);
            }
        });

        mate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkandsendreq에서 refreshdata까지 수행
                checkandsendreq();
            }
        });


        return root;
    }

    public void refreshdata(){
        getdata();
        getmydata();
        myreq();
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        locationSource.getLastLocation();
        if(firstsync){
            CameraPosition cameraPosition=new CameraPosition(setLocation,17);
            naverMap.setCameraPosition(cameraPosition);
            firstsync=false;
        }



        naverMap.setLocationSource(locationSource);//네이버지도상 위치값을 받아온 현재 위치값으로 설정
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

        //아래는 UI세팅을 위한 코드
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        uiSettings.setZoomControlEnabled(false);

        if(markers.size()!=0){
            for(int i=0; i<markers.size(); ++i){
                markers.get(i).setMap(null);
            }
        }
        markers.clear();

        if(coordlist.size()!=0&&(mywalkcheck==0||mywalkcheck==2||mywalkcheck==3)){
            Log.d("산책", "마커세팅 진입");
            for(int i=0; i<coordlist.size(); ++i){
                Marker tmpmarker=new Marker();
                tmpmarker.setPosition(coordlist.get(i));
                final LinearLayout linearLayout=(LinearLayout) View.inflate(getActivity(),R.layout.marker_view,null);
                TextView markertxt_day=linearLayout.findViewById(R.id.markerText_day);
                TextView markertxt_time=linearLayout.findViewById(R.id.markerText_time);
                String[] timedata=timelist.get(i).split("_");
                markertxt_day.setText(String.format("%s년 %s월 %s일",timedata[0],timedata[1],timedata[2]));
                markertxt_time.setText(String.format("%s : %s",timedata[3],timedata[4]));
                tmpmarker.setIcon(OverlayImage.fromView(linearLayout));

                if(myreqlist.containsKey(docuidlist.get(i))){
                    String state="";
                    if(myreqlist.get(docuidlist.get(i))==0){
                        state="수락대기중";
                    }
                    else if(myreqlist.get(docuidlist.get(i))==1){
                        state="수락 됨";
                    }
                    else{
                        state="거절됨";
                    }
                    Log.d("산책 캡션",state);
                    tmpmarker.setCaptionText(state);
                    tmpmarker.setCaptionAligns(Align.Top);
                }

                tmpmarker.setMap(naverMap);

                if(mywalkcheck==2){
                    if(!myreqlist.containsKey(docuidlist.get(i))){
                        tmpmarker.setMap(null);
                    }
                }

                tmpmarker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        int idx=markers.indexOf(tmpmarker);
                        curdocu=docuidlist.get(idx);
                        getuserdata(idlist.get(idx),String.format("%s년 %s월 %s일 %s시 %s분",timedata[0],timedata[1],timedata[2],timedata[3],timedata[4]),locationlist.get(idx));
                        return false;
                    }
                });
                markers.add(tmpmarker);
            }
        }

        if(mymarker.size()!=0){
            for(int i=0; i<mymarker.size(); ++i){
                mymarker.get(i).setMap(null);
            }
        }
        mymarker.clear();

        if(mycoordlist.size()!=0&&(mywalkcheck==1||mywalkcheck==3)){
            for(int i=0; i<mycoordlist.size(); ++i){
                Marker marker=new Marker();
                marker.setPosition(mycoordlist.get(i));
                marker.setCaptionAligns(Align.Top);
                marker.setCaptionText("MY");
                marker.setCaptionColor(Color.BLUE);

                final LinearLayout linearLayout=(LinearLayout) View.inflate(getActivity(),R.layout.marker_view,null);
                TextView markertxt_day=linearLayout.findViewById(R.id.markerText_day);
                TextView markertxt_time=linearLayout.findViewById(R.id.markerText_time);
                markertxt_day.setText(mydaylist.get(i));
                markertxt_time.setText(mytimelist.get(i));
                marker.setIcon(OverlayImage.fromView(linearLayout));
                marker.setMap(naverMap);
                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        int idx=mymarker.indexOf(marker);
                        Intent intent=new Intent(getActivity(),WalkUserListActivity.class);
                        intent.putExtra("mydocu",mydocuidlist.get(idx));
                        intent.putExtra("walkname",mydaylist.get(idx)+" "+mytimelist.get(idx));
                        startActivity(intent);
                        return false;
                    }
                });

                mymarker.add(marker);
            }
        }


    }

    public void myreq(){
        myreqlist.clear();
        walkreq.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document=task.getResult();
                ArrayList<String> docuids= (ArrayList<String>) document.get("requestlist");
                if(docuids!=null){
                    if(docuids.size()>0){
                        for(String s:docuids){
                            walkuser.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot documentSnapshot=task.getResult();
                                    Map<String,Long> datas= (Map<String, Long>) documentSnapshot.get("userlist");
                                    myreqlist.put(s,datas.get(userData.userid));
                                    mapsync();
                                }
                            });
                        }
                    }
                }

            }
        });
    }

    public void checkandsendreq(){
        walkreq.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                ArrayList<String> tmps= (ArrayList<String>) task.getResult().get("requestlist");
                //요청한적이 없어 문서가 없거나 요청을 안한경우
                if(tmps==null||!tmps.contains(curdocu)){
                    sendreq();
                }
                else{
                    Toast.makeText(getActivity(),"이미 요청을 보낸 게시물입니다.",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void sendreq(){
        HashMap<String,Object> data=new HashMap<>();

        HashMap<String, Integer> myreq=new HashMap<>();
        myreq.put(userData.userid,0);

        //userlist는 map-setoption.merge로 업데이트
        data.put("userlist",myreq);
        walkuser.document(curdocu).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("산책 메이트 신청","성공");
            }
        });

        data.clear();
        data.put("requestlist", Arrays.asList(curdocu));

        //requestlist는 list-arrayunion으로 업데이트
        walkreq.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.getResult().exists()){
                    walkreq.document(userData.userid).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(),"메이트 신청 완료",Toast.LENGTH_SHORT).show();
                            refreshdata();
                        }
                    });
                }
                else{
                    walkreq.document(userData.userid).update("requestlist", FieldValue.arrayUnion(curdocu)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(),"메이트 신청 완료",Toast.LENGTH_SHORT).show();
                            refreshdata();
                        }
                    });
                }
            }
        });


    }

    public void getuserdata(String userid,String times,String location){
        udata.document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document=task.getResult();

                String titlestr,userstr;
                if(document.get("title").equals("없음")){
                    titlestr="";
                }else{
                    titlestr= (String) document.get("title");
                }
                String gender;
                if(document.get("gender").equals("M")){
                    gender="남성";
                }
                else{
                    gender="여성";
                }
                userstr=String.format("%s (%s/%s)",document.get("appname"),gender,document.get("age"));
                title.setText(titlestr);
                usertxt.setText(userstr);
                time.setText(times);
                locationtxt.setText(location);
                String urlstr= (String) document.get("profileImagebig");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        InputStream is = null;
                        try {
                            URL imgUrl = new URL(urlstr);
                            connection = (HttpURLConnection) imgUrl.openConnection();
                            connection.setDoInput(true); //url로 input받는 flag 허용
                            connection.connect(); //연결
                            is = connection.getInputStream(); // get inputstream
                            retBitmap = BitmapFactory.decodeStream(is);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userImage.setImageBitmap(retBitmap);
                                    userprofile.setVisibility(View.VISIBLE);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (connection != null) {
                                connection.disconnect();
                            }
                        }
                    }
                }).start();
           }
        });
    }

    public void getdata(){
        idlist.clear();
        docuidlist.clear();
        coordlist.clear();
        timelist.clear();
        locationlist.clear();

        //상대가 설정한 필터, 내가 설정한 필터 모두 이용
        String mygender;
        String myage=userData.age;
        switch (userData.gender){
            case "M":
                mygender="남성"; break;
            default:
                mygender="여성"; break;
        }
        Query query=walkdata;

        //내 게시물은 제외
        query=query.whereNotEqualTo("userid",userData.userid);

        SimpleDateFormat format=new SimpleDateFormat("yyyy MM dd HH mm");
        long now=System.currentTimeMillis();
        Date date=new Date(now);
        String[] getTime=format.format(date).split(" ");
        int[] times=new int[5];
        for(int i=0; i<getTime.length; ++i){
            times[i]=Integer.parseInt(getTime[i]);
        }

        //내 요구사항
        if(!genderstr.equals("무관")){
            query=query.whereEqualTo("usergender",genderstr);
            Log.d("산책 성별체크",genderstr);
        }
        if(!agestr.equals("무관")){
            query=query.whereEqualTo("userage",agestr);
            Log.d("산책 나이체크",agestr);
        }

        Query finalQuery = query;

        //받아오는 사람이 체크해서 못받아오게 하기위함
        //내가 블록한 사람체크 우선, 그 후 받아오기
        ArrayList<String> blockusers=new ArrayList<>();
        block.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    DocumentSnapshot document=task.getResult();
                    for(String s:(ArrayList<String>)document.get("userid")){
                        blockusers.add(s);
                    }
                }
                block.whereArrayContains("userid",userData.userid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(!task.getResult().getDocuments().isEmpty()){
                            ArrayList<DocumentSnapshot> documents= (ArrayList<DocumentSnapshot>) task.getResult().getDocuments();
                            for(DocumentSnapshot document: documents){
                                blockusers.add(document.getId());
                            }
                        }
                        Log.d("차단유저 체크",blockusers.toString());
                        finalQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    Log.d("산책 쿼리 결과물",""+task.getResult().size());
                                    for(QueryDocumentSnapshot document: task.getResult()){
                                        boolean req=true;
                                        //상대 요구와 다르면 제외
                                        if(!document.get("age").equals("무관")){
                                            if(!document.get("age").equals(myage)){
                                                req=false;
                                                Log.d("산책 맵1","결과"+req);
                                            }
                                        }
                                        if(!document.get("gender").equals("무관")){
                                            if(!document.get("gender").equals(mygender)){
                                                req=false;
                                                Log.d("산책 맵2","결과"+req);
                                            }
                                        }
                                        //날짜 지났으면 제외
                                        if((Long)document.get("year")<times[0]){
                                            req=false;
                                            Log.d("산책 맵3-0","결과"+req);
                                        }
                                        else if((Long)document.get("year")==times[0]){
                                            if((Long)document.get("month")<times[1]){
                                                req=false;
                                                Log.d("산책 맵3","결과"+req);
                                            }
                                            else if((Long)document.get("month")==times[1]){
                                                if((Long)document.get("day")<times[2]){
                                                    req=false;
                                                    Log.d("산책 맵4","결과"+req);
                                                }
                                                else if((Long)document.get("day")==times[2]){
                                                    if((Long)document.get("hour")<times[3]){
                                                        req=false;
                                                        Log.d("산책 맵5","결과"+req);
                                                    }
                                                    else if((Long)document.get("hour")==times[3]){
                                                        if((Long)document.get("minute")<times[4]){
                                                            req=false;
                                                            Log.d("산책 맵6","결과"+req);
                                                        }
                                                    }
                                                }
                                            }
                                        }



                                        Log.d("산책 맵7","결과"+req);
                                        if(req&&(!blockusers.contains(document.get("userid")))){
                                            idlist.add((String) document.get("userid"));
                                            docuidlist.add(document.getId());
                                            locationlist.add((String) document.get("location_name"));

                                            Map<String, Object> tmpmap= (Map<String, Object>) document.get("location_coord");
                                            LatLng tmplatlng=new LatLng((Double) tmpmap.get("latitude"), (Double) tmpmap.get("longitude"));
                                            Log.d("산책_게시물좌표",tmplatlng.toString());

                                            //카메라 범위에 벗어나면 제외는 Onmapready에서 처리
                                            coordlist.add(tmplatlng);



                                            String timetmp=String.format("%04d_%02d_%02d_%02d_%02d",(Long)document.get("year"),(Long)document.get("month"),(Long)document.get("day"),(Long)document.get("hour"),(Long)document.get("minute"));
                                            timelist.add(timetmp);
                                        }

                                    }
                                }
                                else{
                                    Log.d("산책 쿼리","실패");
                                }
                                mapsync();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("산책 쿼리 에러",e.toString());
                            }
                        });
                    }
                });
            }
        });

    }

    public void getmydata(){
        mycoordlist.clear();
        mydaylist.clear();
        mytimelist.clear();
        mydocuidlist.clear();

        walkdata.whereEqualTo("userid",userData.userid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot document: task.getResult()){
                    Marker marker=new Marker();

                    Map<String, Object> tmpmap= (Map<String, Object>) document.get("location_coord");
                    LatLng tmplatlng=new LatLng((Double) tmpmap.get("latitude"), (Double) tmpmap.get("longitude"));
                    mycoordlist.add(tmplatlng);
                    mydocuidlist.add(document.getId());
                    mydaylist.add(String.format("%04d년 %02d월 %02d일",document.get("year"),document.get("month"),document.get("day")));
                    mytimelist.add(String.format("%02d : %02d",document.get("hour"),document.get("minute")));
                }
                mapsync();
            }
        });
    }

    //좌표 기반으로 마커 생성
    public void mapsync(){
        mapView.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        refreshdata();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}