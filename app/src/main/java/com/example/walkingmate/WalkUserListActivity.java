package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WalkUserListActivity extends Activity {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference walkuser=fb.collection("walkuser");
    CollectionReference users=fb.collection("users");
    CollectionReference challenge=fb.collection("challenge");

    String docuid;
    String walkname;

    UserData userData;

    ListView waitlistview,acceptlistview;

    //0은 대기, 1은 수락 ,2는 거절
    ArrayList<String> waituser=new ArrayList<>();
    ArrayList<String> waituserprofile=new ArrayList<>();

    ArrayList<String> acceptuser=new ArrayList<>();
    ArrayList<String> acceptuserprofile=new ArrayList<>();
    WaitAdapter waitAdapter;
    AcceptAdapter acceptAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_walk_user_list);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        userData=UserData.loadData(this);
        walkname=null;

        Intent getintent=getIntent();
        docuid=getintent.getStringExtra("mydocu");
        walkname=getintent.getStringExtra("walkname");

        waitlistview=findViewById(R.id.walkuserlist_waiting);
        acceptlistview=findViewById(R.id.walkuserlist_accept);

        waitAdapter=new WaitAdapter(this);
        acceptAdapter=new AcceptAdapter(this);

        waitlistview.setAdapter(waitAdapter);
        acceptlistview.setAdapter(acceptAdapter);

        getlist();



    }

    public void getlist(){
        waituser.clear();
        acceptuser.clear();
        Log.d("산책 메이트 아이디",docuid);
        walkuser.document(docuid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().get("userlist")!=null){
                    Map<String,Long> userlist= (Map<String, Long>) task.getResult().get("userlist");
                    for(Map.Entry<String,Long> entry: userlist.entrySet()){
                        String strKey=entry.getKey();
                        Long intValue=entry.getValue();
                        Log.d("산책 유저리스트",intValue+", "+strKey);
                        if(intValue==0){
                            waituser.add(strKey);
                        }
                        else if(intValue==1){
                            acceptuser.add(strKey);
                        }
                    }

                }
                getprofiles();



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("산책 메이트 실패",e.toString());
            }
        });
    }

    public void getprofiles(){
        waituserprofile.clear();
        acceptuserprofile.clear();
        if(waituser.size()>0){
            for(int i=0; i<waituser.size(); ++i){
                users.document(waituser.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document=task.getResult();
                        String genderstr;
                        if(document.get("gender").equals("M")){
                            genderstr="남성";
                        }
                        else {
                            genderstr="여성";
                        }
                        String result =String.format("%s (%s/%s)",document.get("appname"),genderstr,document.get("age"));
                        waituserprofile.add(result);
                        waitAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        if(acceptuser.size()>0){
            for(int i=0; i<acceptuser.size(); ++i){
                users.document(acceptuser.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document=task.getResult();
                        String genderstr;
                        if(document.get("gender").equals("M")){
                            genderstr="남성";
                        }
                        else {
                            genderstr="여성";
                        }
                        String result =String.format("%s (%s/%s)",document.get("appname"),genderstr,document.get("age"));
                        acceptuserprofile.add(result);
                        acceptAdapter.notifyDataSetChanged();
                    }
                });
            }
        }


    }

    public void setstate(String userid,int state){
        HashMap<String,Object> data=new HashMap<>();

        HashMap<String, Integer> myreq=new HashMap<>();
        myreq.put(userid,state);
        data.put("userlist",myreq);

        walkuser.document(docuid).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("산책 메이트", "업데이트 성공");
            }
        });
    }




    public class WaitAdapter extends BaseAdapter{

        LayoutInflater layoutInflater;
        Context context;

        public WaitAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return waituser.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=layoutInflater.inflate(R.layout.listlayout_walkwait,null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(waituser.size()==0){
                return emptyview;
            }
            TextView textView=view.findViewById(R.id.user_walkwait);
            Button accept=view.findViewById(R.id.accept_walkwait);
            Button reject=view.findViewById(R.id.reject_walkwait);
            Button chat=view.findViewById(R.id.chat_walkwait);

            LinearLayout body=view.findViewById(R.id.body_walkwait);
            body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(WalkUserListActivity.this, UserProfileActivity.class);
                    intent.putExtra("userid",waituser.get(position));
                    startActivity(intent);
                }
            });

            textView.setText(waituserprofile.get(position));

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //사교적인 워커 본인 반영은 처음 수락시
                    if(acceptuser.size()==0){
                        challenge.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    Long mymeet= (Long) task.getResult().get("meet");
                                    challenge.document(userData.userid).update("meet",mymeet+1);
                                }
                            }
                        });
                    }


                    //수락한 유저 도전과제 반영
                    challenge.document(waituser.get(position)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d("문서 체크",task.getResult().get("meet").toString());
                                Long mymeet= (Long) task.getResult().get("meet");
                                Log.d("신청유저",waituser.toString());
                                challenge.document(waituser.get(position)).update("meet",mymeet+1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        setstate(waituser.get(position),1);
                                        acceptuser.add(waituser.get(position));

                                        Log.d("수락 목록",waituser.get(position));

                                        acceptuserprofile.add(waituserprofile.get(position));
                                        acceptUserChatrooms(docuid,waituser.get(position));

                                        waituser.remove(position);
                                        waituserprofile.remove(position);
                                        notifyDataSetChanged();
                                        acceptAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });

                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setstate(waituser.get(position),2);
                    waituser.remove(position);
                    waituserprofile.remove(position);
                    notifyDataSetChanged();
                }
            });

            //이미 채팅이 생성되었다면 채팅방 열기
            //채팅이 없으면 채팅방 생성
            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addPersonalChatrooms(docuid,waituser.get(position));
                }
            });

            return view;
        }
    }

    //개인 신청자에 대하여 채팅방 생성 및 이동, 이미존재하면 그냥 이동
    public void addPersonalChatrooms(String docuid, String userid){
        DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");

        ChatRoom tmp=new ChatRoom();

        String roomnames="";
        if(walkname!=null){
            roomnames="[산책][개인]"+walkname;
        }
        tmp.roomid=docuid+"@"+userid;
        Log.d("채팅룸 아이디",tmp.roomid);
        tmp.roomname=roomnames;
        Map<String,Boolean> usertmp=new HashMap<>();
        usertmp.put(userData.userid,true);
        usertmp.put(userid,true);
        tmp.userids=usertmp;

        //채팅방 존재여부 체크 후 이동
        dr.child(tmp.roomid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(ChatRoom.class)!=null){
                    //전에 나갔던 채팅방이면 다시 true로 바꾸며 들어옴
                    if(snapshot.getValue(ChatRoom.class).userids.get(userData.userid).equals(false)){
                        Map<String,Object> tmpuser=new HashMap<>();
                        tmpuser.put(userData.userid,true);
                        dr.child(tmp.roomid).child("userids").updateChildren(tmpuser);
                    }


                    Log.d("채팅방 생성","이미 존재하는 채팅방-해당 채팅방으로 이동");
                    Intent intent=new Intent(WalkUserListActivity.this,ChatActivity.class);
                    intent.putExtra("roomid",tmp.roomid);
                    ArrayList<String> tmpid=new ArrayList<>();
                    tmpid.add(userid);
                    intent.putExtra("userids",tmpid);
                    startActivity(intent);
                }
                else{
                    Log.d("채팅방 생성","새 채팅방 생성 후 이동");
                    dr.child(tmp.roomid).setValue(tmp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(WalkUserListActivity.this,ChatActivity.class);
                            intent.putExtra("roomid",tmp.roomid);
                            ArrayList<String> tmpid=new ArrayList<>();
                            tmpid.add(userid);
                            intent.putExtra("userids",tmpid);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //게시물의 수락자들을 모아놓는 채팅방 생성.
    //수락 누를때 마다 실행.
    public void acceptUserChatrooms(String docuid, String acceptuserid){
        DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");

        ChatRoom tmp=new ChatRoom();

        String roomnames="";
        if(walkname!=null){
            roomnames="[산책][수락]"+walkname;
        }
        tmp.roomid=docuid;
        Log.d("채팅룸 아이디",tmp.roomid);
        tmp.roomname=roomnames;
        Map<String,Boolean> usertmp=new HashMap<>();
        usertmp.put(userData.userid,true);
        usertmp.put(acceptuserid,true);
        tmp.userids=usertmp;

        //채팅방 존재여부 체크 후 이동
        dr.child(tmp.roomid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(ChatRoom.class)!=null){
                    Log.d("수락채팅방 생성","이미 존재하는 채팅방-수락자 추가");
                    Map<String,Object> adduser=new HashMap<>();
                    adduser.put(acceptuserid,true);
                    dr.child(tmp.roomid).child("userids").updateChildren(adduser);
                }
                else{
                    Log.d("수락채팅방 생성","새 채팅방 생성");
                    dr.child(tmp.roomid).setValue(tmp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public class AcceptAdapter extends BaseAdapter{

        LayoutInflater layoutInflater;
        Context context;

        public AcceptAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return acceptuser.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=layoutInflater.inflate(R.layout.listlayout_walkwait,null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(acceptuser.size()==0){
                return emptyview;
            }
            TextView textView=view.findViewById(R.id.user_walkwait);
            Button accept=view.findViewById(R.id.accept_walkwait);
            Button reject=view.findViewById(R.id.reject_walkwait);
            Button chat=view.findViewById(R.id.chat_walkwait);

            LinearLayout body=view.findViewById(R.id.body_walkwait);
            body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(WalkUserListActivity.this, UserProfileActivity.class);
                    intent.putExtra("userid",acceptuser.get(position));
                    startActivity(intent);
                }
            });

            textView.setText(acceptuserprofile.get(position));

            accept.setVisibility(View.INVISIBLE);

            reject.setVisibility(View.INVISIBLE);

            chat.setVisibility(View.INVISIBLE);

            return view;
        }
    }
}