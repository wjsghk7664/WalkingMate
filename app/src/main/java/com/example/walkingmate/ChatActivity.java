package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference user=db.collection("users");

    ChildEventListener childEventListener;
    ValueEventListener valueEventListener;

    String roomid;
    ArrayList<String> users=new ArrayList<>();
    HashMap<String, Bitmap> userimgs=new HashMap<>();
    HashMap<String, String> usernames=new HashMap<>();

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
        Log.d("채팅방","activity시작");

        userData=UserData.loadData(this);

        Intent intent=getIntent();
        roomid=intent.getStringExtra("roomid");

        //자기 프로필은 필요없으니 제외
        users=intent.getStringArrayListExtra("userids");
        Log.d("채팅 유저",users.toString());
        users.remove(userData.userid);

        checkuser();
        loadlocalprofile();

        msg=findViewById(R.id.msg);
        sendmsg=findViewById(R.id.sendmsg);
        msglist=findViewById(R.id.msglist);

        msgAdapter=new MsgAdapter(this,roomid,userData.userid);
        msglist.setAdapter(msgAdapter);

        loaduserprofile();

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
            String dir=folder+roomid+"message.txt";
            File file=new File(dir);
            FileInputStream fis=new FileInputStream(file);
            byte[] buffer=new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            result=new String(buffer);
            results=result.split("\n");
            for(int i=0; i<results.length; i+=3){
                ChatRoom.Comment tmpc=new ChatRoom.Comment();
                tmpc.userid=results[i];
                tmpc.msg=results[i+1];
                tmpc.time=results[i+2];
                resultc.add(tmpc);
                Log.d("채팅 불러오기",tmpc.msg);
            }
            if(resultc.size()!=0){
                starttime=resultc.get(resultc.size()-1).time;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return resultc;
    }


    public void sendmsgs(){
        String tmpmsg=msg.getText().toString();

        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSS");
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
        String filename=roomid+"message.txt";
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

    public void checkuser(){
        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("채팅방","새로운 참가자 변동");
                Map<String,Object> tmpusers=new HashMap<>();
                tmpusers= (Map<String, Object>) snapshot.getValue();
                for(String s:tmpusers.keySet()){
                    if(((Boolean)tmpusers.get(s))&&(!users.contains(s))){
                        users.add(s);
                    }
                }
                loaduserprofile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        dr.child(roomid).child("userids").addValueEventListener(valueEventListener);
    }

    //제일 먼저 로컬에서 사진파일 불러오기
    public void loadlocalprofile(){
        for(String userid:users){
            String path=getFilesDir().getAbsolutePath()+"/messages/"+roomid+"@"+userid+".jpg";
            Bitmap bitmap=null;
            bitmap=BitmapFactory.decodeFile(path);
            if(bitmap!=null){
                userimgs.put(userid,bitmap);
            }
        }

    }

    public void savelocalprofile(Bitmap bitmap,String userid){
        if(bitmap==null){
            return;
        }

        String path=getFilesDir().getAbsolutePath()+ "/messages/";
        File storage = new File(path);
        String filename=roomid+"@"+userid+".jpg";
        File tempFile = new File(storage, filename);
        try {

            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    //hashmap에 있는지 체크. 없으면 불러오기 시작
    //첫 시작, 새유저 접속시 작동
    //목록에 없으면 저장
    public void loaduserprofile(){
        for(String s:users){
            //기존에 없는 경우에만 불러옴.
            user.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        String urlstr= (String) task.getResult().get("profileImagesmall");
                        if(usernames.get(s)==null){
                            usernames.put(s, (String) task.getResult().get("appname"));
                        }
                        if(userimgs.get(s)==null){
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
                                        Bitmap retBitmap = BitmapFactory.decodeStream(is);
                                        userimgs.put(s,retBitmap);
                                        savelocalprofile(retBitmap,s);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                msgAdapter.notifyDataSetChanged();
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
                    }
                }
            });


        }
    }

    public String gettime(String time){
        String y=time.substring(0,4);
        String m=time.substring(4,6);
        String d=time.substring(6,8);
        String h=time.substring(8,10);
        String min=time.substring(10,12);
        return String.format("%s/%s/%s %s:%s",y,m,d,h,min);
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
            TextView msgitem,timeitem,username;
            CircleImageView userimg;
            if(comments.get(position).userid.equals(userid)){
                view=layoutInflater.inflate(R.layout.layout_mymsg,null);
                msgitem=view.findViewById(R.id.msg_mine);
                timeitem=view.findViewById(R.id.time_mine);
            }
            else{
                //if는 맨 처음이거나 바로 위와 다른사람이 말을 한 경우-프로필 있는걸로
                if(position==0||(!comments.get(position).userid.equals(comments.get(position-1).userid))){
                    view=layoutInflater.inflate(R.layout.layout_othersmsg, null);
                    msgitem=view.findViewById(R.id.msg_others);
                    timeitem=view.findViewById(R.id.time_others);
                    username=view.findViewById(R.id.name_others);
                    userimg=view.findViewById(R.id.userimg_others);

                    username.setText(usernames.get(comments.get(position).userid));
                    if(userimgs.get(comments.get(position).userid)!=null){
                        userimg.setImageBitmap(userimgs.get(comments.get(position).userid));
                    }


                }
                else{
                    view=layoutInflater.inflate(R.layout.layout_othersmsg_noprofile, null);
                    msgitem=view.findViewById(R.id.msg_othersno);
                    timeitem=view.findViewById(R.id.time_othersno);
                }
            }
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(comments.size()==0){
                return emptyview;
            }

            if(position+1<comments.size()){
                if(comments.get(position+1).userid.equals(comments.get(position).userid)&&comments.get(position+1).time.substring(0,12).equals(comments.get(position).time.substring(0,12))){
                    timeitem.setVisibility(View.INVISIBLE);
                }
            }

            msgitem.setText(comments.get(position).msg);
            timeitem.setText(gettime(comments.get(position).time));

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
            childEventListener=new ChildEventListener() {
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
            };
            dr.child(roomid).child("comments").orderByChild("time").startAfter(starttime).addChildEventListener(childEventListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dr.child(roomid).child("comments").orderByChild("time").startAfter(starttime).removeEventListener(childEventListener);
        dr.child(roomid).child("userids").removeEventListener(valueEventListener);
    }
}