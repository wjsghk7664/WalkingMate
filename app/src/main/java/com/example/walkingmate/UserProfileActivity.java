package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference users=fb.collection("users");
    CollectionReference blocklist=fb.collection("blocklist");

    String userid;

    CircleImageView profileImage;
    TextView appname,gender,age,title;
    Button block,feed;
    ImageButton report,back;

    UserData userData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent getIntent=getIntent();
        userid=getIntent.getStringExtra("userid");
        userData=UserData.loadData(this);

        profileImage=findViewById(R.id.profileImage_userprofile);
        appname=findViewById(R.id.appname_userprofile);
        gender=findViewById(R.id.gender_profile);
        age=findViewById(R.id.age_profile);
        title=findViewById(R.id.title_userprofile);
        block=findViewById(R.id.block_profile);
        feed=findViewById(R.id.feed_profile);
        back=findViewById(R.id.back_profile);
        report=findViewById(R.id.report_profile);

        setprofile();

        if(userData.userid.equals(userid)){
            block.setVisibility(View.INVISIBLE);
            feed.setVisibility(View.INVISIBLE);
            report.setVisibility(View.INVISIBLE);
        }


        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blocklist.document(userData.userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> blocklisttmp=new ArrayList<>();
                        if(task.getResult().exists()){
                            blocklisttmp= (ArrayList<String>) task.getResult().get("userid");
                        }
                        if(!blocklisttmp.contains(userid)){
                            blocklist.document(userData.userid).update("userid", FieldValue.arrayUnion(userid)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(UserProfileActivity.this,"차단되었습니다.",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(UserProfileActivity.this,"이미 차단한 유저입니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserProfileActivity.this, FeedActivity.class);
                intent.putExtra("iswrite",2);
                intent.putExtra("others",userid);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserProfileActivity.this,ReportActivity.class);
                intent.putExtra("userid",userid);
                startActivity(intent);
            }
        });

    }

    public void setprofile(){

        users.document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document=task.getResult();
                appname.setText((String) document.get("appname"));
                String genderstr;
                if(document.get("gender").equals("M")){
                    genderstr="남성";
                }
                else{
                    genderstr="여성";
                }
                gender.setText(genderstr);
                age.setText((String)document.get("age"));
                if(!document.get("title").equals("없음")){
                    title.setText((String)document.get("title"));
                }
                Long rel=Math.round(document.getDouble("reliability"));

                String urlstr=document.getString("profileImagebig");
                if((urlstr!=null)&&(!urlstr.equals(""))){
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setrel(Math.toIntExact(rel));
                                        profileImage.setImageBitmap(retBitmap);
                                        findViewById(R.id.profile_main).setVisibility(View.VISIBLE);
                                        findViewById(R.id.loading_profile).setVisibility(View.INVISIBLE);
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
                else{
                    findViewById(R.id.profile_main).setVisibility(View.VISIBLE);
                    findViewById(R.id.loading_profile).setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    public void setrel(int rel){
        int Max=250;
        int relhor=(Max*rel)/100;
        int relvermarlef=relhor-4;
        int heartmarlef=relhor-17;

        FrameLayout.LayoutParams lp0=new FrameLayout.LayoutParams(getdp(relhor),getdp(30));
        lp0.setMargins(0,getdp(60),0,0);
        lp0.gravity= Gravity.BOTTOM;
        findViewById(R.id.reliable_mainred_pro).setLayoutParams(lp0);

        FrameLayout.LayoutParams lp1=new FrameLayout.LayoutParams(getdp(4),getdp(30));
        lp1.setMargins(getdp(relvermarlef),0,0,getdp(15));
        lp1.gravity= Gravity.BOTTOM;
        findViewById(R.id.reliable_vertical_pro).setLayoutParams(lp1);

        FrameLayout.LayoutParams lp2=new FrameLayout.LayoutParams(getdp(30),getdp(30));
        lp2.setMargins(getdp(heartmarlef),0,0,getdp(40));
        lp2.gravity= Gravity.BOTTOM;
        TextView relnum=findViewById(R.id.reliable_number_pro);
        relnum.setLayoutParams(lp2);
        relnum.setText(rel+"");
    }

    public int getdp(int a){
        DisplayMetrics displayMetrics=getResources().getDisplayMetrics();
        return Math.round(a*displayMetrics.density);
    }

}