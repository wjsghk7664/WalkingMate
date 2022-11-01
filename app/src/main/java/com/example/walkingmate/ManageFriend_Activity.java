package com.example.walkingmate;

import androidx.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ManageFriend_Activity extends AppCompatActivity {

    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference blocklist=fb.collection("blocklist");
    CollectionReference users=fb.collection("users");

    ImageButton back;
    ListView blocklistview;

    ArrayList<String> blockusers=new ArrayList<>();
    ArrayList<String> blockusersProfile=new ArrayList<>();
    UserData userData;

    BlockAdapter blockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friend);

        userData=UserData.loadData(this);

        back=findViewById(R.id.back_managefreind);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageFriend_Activity.this,FeedCalendarActivity.class));
                finish();
            }
        });
        getlist();

        blocklistview=findViewById(R.id.blocklistview);
        blockAdapter=new BlockAdapter(this);
        blocklistview.setAdapter(blockAdapter);

    }

    public void getlist(){
        blocklist.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    for(String s: (ArrayList<String>)task.getResult().get("userid")){
                        users.document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.getResult().exists()){
                                    blockusers.add(s);
                                    DocumentSnapshot user=task.getResult();
                                    String gender;
                                    if(user.get("gender").equals("M")){
                                        gender="남성";
                                    }
                                    else{
                                        gender="여성";
                                    }
                                    String userprofile=String.format("%s (%s/%s)",user.get("appname"),gender,user.get("age"));
                                    blockusersProfile.add(userprofile);
                                    blockAdapter.notifyDataSetChanged();
                                }

                            }
                        });
                    }
                }

            }
        });
    }

    public void removeblock(){
        HashMap<String, Object> fixedlist=new HashMap<>();
        fixedlist.put("userid",blockusers);
        blocklist.document(userData.userid).set(fixedlist);
    }


    public class BlockAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        Context context;

        public BlockAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return blockusers.size();
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
            View view=layoutInflater.inflate(R.layout.blockuserlist,null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(blockusers.size()==0){
                return emptyview;
            }
            Button cancel=view.findViewById(R.id.cancel_block);
            TextView profile=view.findViewById(R.id.user_block);

            profile.setText(blockusersProfile.get(position));

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    blockusers.remove(position);
                    blockusersProfile.remove(position);
                    notifyDataSetChanged();
                    removeblock();
                }
            });

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        back.performClick();
    }
}