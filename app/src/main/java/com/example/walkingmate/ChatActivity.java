package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");
    String roomid;

    EditText msg;
    Button sendmsg;
    ListView msglist;

    UserData userData;

    MsgAdapter msgAdapter;

    String starttime="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userData=UserData.loadData(this);

        Intent intent=getIntent();
        roomid=intent.getStringExtra("roomid");

        msg=findViewById(R.id.msg);
        sendmsg=findViewById(R.id.sendmsg);
        msglist=findViewById(R.id.msglist);

        msgAdapter=new MsgAdapter(this,roomid,userData.userid);
        msglist.setAdapter(msgAdapter);

        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsgs();
                msg.getText().clear();
            }
        });
    }

    //로컬에서 메시지 불러오기
    //저장된 메시지의 마지막꺼를 starttime으로
    public ArrayList<ChatRoom.Comment> setChattingroom(String roomid){
        String folder= getFilesDir().getAbsolutePath() + "/messages/";
        String result="";
        String[] results;
        ArrayList<ChatRoom.Comment> resultc=new ArrayList<>();

        File check;
        try{
            check=new File(folder);

            //경로가 없는경우 그냥 null반환하고 끝
            if(!check.isDirectory()){
                return resultc;
            }
            String dir=folder+roomid+".txt";
            File file=new File(dir);
            FileInputStream fis=new FileInputStream(file);
            byte[] buffer=new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            result=new String(buffer);
            results=result.split("\n");
            for(int i=1; i<results.length; i+=3){
                ChatRoom.Comment tmpc=new ChatRoom.Comment();
                tmpc.userid=results[i];
                tmpc.msg=results[i+1];
                tmpc.time=results[i+2];
                resultc.add(tmpc);
                Log.d("채팅 불러오기",tmpc.msg);
            }
            starttime=resultc.get(resultc.size()-1).time;
        }catch(Exception e){
            e.printStackTrace();
        }
        return resultc;
    }


    public void sendmsgs(){
        String tmpmsg=msg.getText().toString();

        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        Date date=new Date(System.currentTimeMillis());
        String now=sdf.format(date);

        ChatRoom.Comment tmpcomment=new ChatRoom.Comment();
        tmpcomment.userid=userData.userid;
        tmpcomment.msg=tmpmsg;
        tmpcomment.time=now;
        Log.d("채팅 전송",tmpcomment.msg);

        Log.d("채팅 룸아이디",roomid+"");
        dr.child("/"+roomid+"/comments/").push().setValue(tmpcomment);
    }

    public void savemsg(String msg){
        String folder= getFilesDir().getAbsolutePath() + "/messages/";
        String filename=roomid+".txt";
        File file_path;
        try{
            file_path=new File(folder);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
                Log.d("채팅 데이터 저장","경로 생성");
            }
            FileWriter fileWriter=new FileWriter(folder+filename,true);
            fileWriter.write(msg+"\n");
            Log.d("채팅 저장",msg);
            fileWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class MsgAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        Context context;
        ArrayList<ChatRoom.Comment> comments=new ArrayList<>();
        String roomid;
        String userid;

        public MsgAdapter(Context context, String roomid, String userid){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
            this.roomid=roomid;
            this.userid=userid;
            comments=setChattingroom(roomid);
            getrecentmsg();
        }

        @Override
        public int getCount() {
            return comments.size();
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
            View view;
            TextView msgitem;
            if(comments.get(position).userid.equals(userid)){
                view=layoutInflater.inflate(R.layout.layout_mymsg,null);
                msgitem=view.findViewById(R.id.msg_mine);
            }
            else{
                view=layoutInflater.inflate(R.layout.layout_othersmsg, null);
                msgitem=view.findViewById(R.id.msg_others);
            }
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(comments.size()==0){
                return emptyview;
            }

            msgitem.setText(comments.get(position).msg);

            return view;
        }

        //아래는 매번 모든 값을 가져와서 취소
        /*public void getmsg(){
            DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");
            dr.child(roomid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                        comments.add(dataSnapshot.getValue(ChatRoom.Comment.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }*/

        //이건 추가된것만 가져오므로 선택
        //startAt으로 기존 것은 안가져오게 설정
        //메시지는 로컬에 저장해두기.
        public void getrecentmsg(){
            DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");
            dr.child(roomid).child("comments").orderByChild("time").startAfter(starttime).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    ChatRoom.Comment tmpcomment=snapshot.getValue(ChatRoom.Comment.class);
                    comments.add(tmpcomment);
                    savemsg(tmpcomment.userid);
                    savemsg(tmpcomment.msg);
                    savemsg(tmpcomment.time);
                    notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}