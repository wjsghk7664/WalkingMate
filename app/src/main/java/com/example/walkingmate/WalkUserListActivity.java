package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WalkUserListActivity extends Activity {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference walkuser=fb.collection("walkuser");
    CollectionReference users=fb.collection("users");

    String docuid;

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

        Intent getintent=getIntent();
        docuid=getintent.getStringExtra("mydocu");

        waitlistview=findViewById(R.id.walkuserlist_waiting);
        acceptlistview=findViewById(R.id.walkuserlist_accept);

        waitAdapter=new WaitAdapter(this);
        acceptAdapter=new AcceptAdapter(this);

        waitlistview.setAdapter(waitAdapter);
        acceptlistview.setAdapter(acceptAdapter);

        getlist();



    }

    public void getlist(){
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
        if(waituser.size()>0){
            for(int i=0; i<waituser.size(); ++i){
                users.document(waituser.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document=task.getResult();
                        String result =String.format("%s (%s/%s)",document.get("appname"),document.get("gender"),document.get("age"));
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
                        String result =String.format("%s (%s/%s)",document.get("appname"),document.get("gender"),document.get("age"));
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

            textView.setText(waituserprofile.get(position));

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setstate(waituser.get(position),1);
                    acceptuser.add(waituser.get(position));

                    Log.d("수락 목록",waituser.get(position));

                    acceptuserprofile.add(waituserprofile.get(position));
                    waituser.remove(position);
                    waituserprofile.remove(position);
                    notifyDataSetChanged();
                    acceptAdapter.notifyDataSetChanged();
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

            return view;
        }
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

            textView.setText(acceptuserprofile.get(position));

            accept.setVisibility(View.INVISIBLE);

            reject.setVisibility(View.INVISIBLE);

            return view;
        }
    }
}