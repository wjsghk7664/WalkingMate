package com.example.walkingmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatFragment extends Fragment {
    DatabaseReference dr= FirebaseDatabase.getInstance().getReference("Chatrooms");
    UserData userData;

    ListView chatrooms;
    ChatroomAdapter chatroomAdapter;
    ArrayList<ChatRoom> chatRooms=new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("테스트","oc");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("테스트","ocv");
        View view=inflater.inflate(R.layout.fragment_chat, container, false);

        view.findViewById(R.id.addroom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addChatrooms();
            }
        });

        view.findViewById(R.id.addevery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEveryChatrooms();
            }
        });

        userData=UserData.loadData(getActivity());


        chatrooms=view.findViewById(R.id.ChatroomList);
        chatroomAdapter=new ChatroomAdapter(getActivity());
        chatrooms.setAdapter(chatroomAdapter);
        getlocalChatRooms();

        getChatrooms();

        return view;
    }


    //우선 로컬에서 채팅방가져오고 난 뒤 로컬에 없는 신규 채팅방을 추가
    //채팅이 올때마다도 업데이트 되므로 챗룸객체도 겹치는게 존재시 실시간 업데이트
    public void getChatrooms(){

        dr.orderByChild("userids/"+userData.userid).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatRoom tmpc=dataSnapshot.getValue(ChatRoom.class);
                    boolean checkroom=false;
                    int idx=-1;
                    //업데이트
                    for(ChatRoom chatRoom: chatRooms){
                        if(chatRoom.roomid.equals(tmpc.roomid)){
                            //새 유저가 들어온 경우
                            if(tmpc.userids.size()!=chatRoom.userids.size()){
                                updateroom(tmpc);
                            }
                            checkroom=true;
                            idx=chatRooms.indexOf(chatRoom);
                            chatRooms.set(idx,tmpc);
                        }
                    }
                    //룸아이디 체크해 존재하지 않는 경우만 추가
                    //이 경우 로컬에도 저장
                    if(!checkroom){
                        chatRooms.add(tmpc);
                        saverooms(tmpc);
                    }
                    Log.d("채팅 추가",chatRooms.size()+", "+chatRooms.get(chatRooms.size()-1).roomid);
                }
                chatroomAdapter.notifyDataSetChanged();
                Log.d("채팅방수",chatroomAdapter.getCount()+"");



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("채팅읽기 실패",error.toString());
            }
        });
    }

    //나중에 사용자들 목록을 usertmp에 넣는식으로만 수정하면 될듯
    //추가 되므로 로컬에도 저장
    public void addChatrooms(){
        SimpleDateFormat sdf=new SimpleDateFormat("HHmmss");
        Date date=new Date(System.currentTimeMillis());

        ChatRoom tmp=new ChatRoom();
        tmp.roomid=sdf.format(date);
        Log.d("채팅룸 아이디",tmp.roomid);
        tmp.roomname="test";
        Map<String,Boolean> usertmp=new HashMap<>();
        usertmp.put("ob_ua6RyFxqm66pBjej9gJ0VDyatPHLDu81RRis__xY",true);
        usertmp.put("C7VynmLzbvX9yXxViYZZxMQQpqeASDbQKg6XFuAnivY",true);
        tmp.userids=usertmp;

        saverooms(tmp);

        dr.child(tmp.roomid).setValue(tmp);
    }

    public void addEveryChatrooms(){
        SimpleDateFormat sdf=new SimpleDateFormat("HHmmss");
        Date date=new Date(System.currentTimeMillis());

        ChatRoom tmp=new ChatRoom();
        tmp.roomid=sdf.format(date);
        Log.d("채팅룸 아이디",tmp.roomid);
        tmp.roomname="testevery";
        Map<String,Boolean> usertmp=new HashMap<>();
        usertmp.put("ob_ua6RyFxqm66pBjej9gJ0VDyatPHLDu81RRis__xY",true);
        usertmp.put("qy-LTaqG2gfFXY3JbNJmRVInup3ensNQBhEBjPou-DM",true);
        usertmp.put("SsNtgRDgtZSjD0GI37M476ixp0p9d7NjKmN9SHlX04o", true);
        usertmp.put("t8hCqWDoYJUZsmTK0FW0EWfZJjO2LkIOJYqqyM22FJU", true);
        usertmp.put("C7VynmLzbvX9yXxViYZZxMQQpqeASDbQKg6XFuAnivY", true);
        tmp.userids=usertmp;

        saverooms(tmp);

        dr.child(tmp.roomid).setValue(tmp);
    }

    public ArrayList<String> getlocalChatroomslist(){
        String path=getActivity().getFilesDir().getAbsolutePath() + "/messages/";
        File f;
        File[] files;
        ArrayList<String> filenames=new ArrayList<>();

        try{
            f=new File(path);

            //디렉토리가 없으면 null반환
            if(!f.isDirectory()){
                return null;
            }
            files = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase(Locale.US).endsWith("room.txt");
                }
            });
            //파일이 없으면 null반환
            if(files.length == 0){
                return null;
            }

            for(int i=0; i<files.length; ++i){
                filenames.add(files[i].getName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return filenames;
    }


    //로컬에 존재하는 챗룸 불러오기
    public void getlocalChatRooms(){
        ArrayList<ChatRoom> resultrooms=new ArrayList<>();
        ArrayList<String> filelist=getlocalChatroomslist();
        if(filelist==null){
            return;
        }
        Log.d("로컬채팅룸 파일목록",filelist.toString());
        for(String s:filelist){
            String folder= getActivity().getFilesDir().getAbsolutePath() + "/messages/";
            String result="";
            String[] results;


            File check;
            try{
                check=new File(folder);

                String dir=folder+s;
                File file=new File(dir);
                FileInputStream fis=new FileInputStream(file);
                byte[] buffer=new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                result=new String(buffer);
                results=result.split("\n");
                String[] roominfo=results[0].split("@");
                ChatRoom tmpchatroom=new ChatRoom();
                tmpchatroom.roomname=roominfo[0];
                Map<String,Boolean> users=new HashMap<>();
                for(int i=0; i<Integer.parseInt(roominfo[1]); ++i){
                    users.put(roominfo[i+2],true);
                }
                tmpchatroom.userids=users;
                tmpchatroom.roomid=s.replace("room.txt","");
                resultrooms.add(tmpchatroom);
                Log.d("로컬채팅룸 불러오기",tmpchatroom.roomid);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        chatRooms= resultrooms;
        chatroomAdapter.notifyDataSetChanged();
    }

    //수락으로 새 유저가 들어오는 경우 업데이트
    public void updateroom(ChatRoom chatRoom){
        String folder= getActivity().getFilesDir().getAbsolutePath() + "/messages/";
        String filename=chatRoom.roomid+"room.txt";
        File file_path;
        try{
            file_path=new File(folder);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
                Log.d("채팅 데이터 저장","경로 생성");
            }
            File files=new File(folder+filename);
            if(!files.exists()){
                FileWriter fileWriter=new FileWriter(folder+filename,false);
                String firstline=chatRoom.roomname+"@"+chatRoom.userids.size()+"@";
                for(String s:chatRoom.userids.keySet()){
                    firstline+=s+"@";
                }
                firstline+="\n";
                fileWriter.write(firstline);
                Log.d("채팅룸 저장",chatRoom.roomid);
                fileWriter.close();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saverooms(ChatRoom chatRoom){
        String folder= getActivity().getFilesDir().getAbsolutePath() + "/messages/";
        String filename=chatRoom.roomid+"room.txt";
        File file_path;
        try{
            file_path=new File(folder);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
                Log.d("채팅 데이터 저장","경로 생성");
            }
            File files=new File(folder+filename);
            if(!files.exists()){
                FileWriter fileWriter=new FileWriter(folder+filename,true);
                String firstline=chatRoom.roomname+"@"+chatRoom.userids.size()+"@";
                for(String s:chatRoom.userids.keySet()){
                    firstline+=s+"@";
                }
                firstline+="\n";
                fileWriter.write(firstline);
                Log.d("채팅룸 저장",chatRoom.roomid);
                fileWriter.close();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void Outroom(ChatRoom chatRoom){
        String roomid=chatRoom.roomid;
        Map<String,Object> tmp=new HashMap<>();
        tmp.put(userData.userid,false);
        dr.child(roomid).child("userids").updateChildren(tmp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                checkDel(roomid);
            }
        });
        deleteroom(roomid);
        chatRooms.remove(chatRoom);
        chatroomAdapter.notifyDataSetChanged();
    }

    public void checkDel(String roomid){
        dr.child(roomid).child("userids").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count=0;
                Map<String,Boolean> users= (Map<String, Boolean>) snapshot.getValue();
                for(String s:users.keySet()){
                    if(users.get(s)){
                        ++count;
                    }
                }
                if(count==0){
                    dr.child(roomid).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteroom(String roomid){
        String folder= getActivity().getFilesDir().getAbsolutePath() + "/messages/";
        String pathroom=folder+roomid+"room.txt";
        String pathmessage=folder+roomid+"message.txt";

        try{
            File fileroom=new File(pathroom);
            if(fileroom.exists()){
                fileroom.delete();
            }
            File filemsg=new File(pathmessage);
            if(filemsg.exists()){
                filemsg.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class ChatroomAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        Context context;

        public ChatroomAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return chatRooms.size();
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
            View view=layoutInflater.inflate(R.layout.chatroomlayout,null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(chatRooms.size()==0){
                return emptyview;
            }
            LinearLayout chatroombody=view.findViewById(R.id.body_chatroom);
            TextView chatroomname=view.findViewById(R.id.chatroomname);
            Button outrooms=view.findViewById(R.id.outroom);
            TextView lastmsg=view.findViewById(R.id.lastmsg);
            TextView usernum=view.findViewById(R.id.usernum);

            Log.d("채팅방 세팅",chatRooms.get(position).roomname);
            chatroomname.setText(chatRooms.get(position).roomname);
            chatroombody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> useridslist=new ArrayList<>();
                    for(String s:chatRooms.get(position).userids.keySet()){
                        useridslist.add(s);
                    }
                    Intent intent=new Intent(getActivity(),ChatActivity.class);
                    intent.putExtra("roomid",chatRooms.get(position).roomid);
                    intent.putExtra("userids", useridslist);
                    startActivity(intent);
                }
            });

            if(chatRooms.get(position).comments!=null&&chatRooms.get(position).comments.size()!=0){
                Map<String, ChatRoom.Comment> cmap= chatRooms.get(position).comments;
                long lasttime=0;
                String laststr="";
                for(String s:cmap.keySet()){
                    long tmptime=Long.parseLong(cmap.get(s).time);
                    if(tmptime>lasttime){
                        lasttime=tmptime;
                        laststr=cmap.get(s).msg;
                    }
                }
                if(laststr.length()>50){
                    laststr=laststr.substring(0,50)+"...";
                }
                lastmsg.setText(laststr+returntime(lasttime+""));
            }

            Map<String,Boolean> users=chatRooms.get(position).userids;
            int usern=0;
            for(String s:users.keySet()){
                if(users.get(s)){
                    usern++;
                }
            }
            usernum.setText(usern+"");

            outrooms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Outroom(chatRooms.get(position));
                }
            });



            return view;
        }
    }

    public String returntime(String time){
        String y=time.substring(2,4);
        String m=time.substring(4,6);
        String d=time.substring(6,8);
        String h=time.substring(8,10);
        String min=time.substring(10,12);
        return String.format(" (%s/%s/%s %s:%s)",y,m,d,h,min);

    }

}