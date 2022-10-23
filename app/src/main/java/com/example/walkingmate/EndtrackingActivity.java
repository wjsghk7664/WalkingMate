package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class EndtrackingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_endtracking);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Intent intent=getIntent();
        String filename=intent.getStringExtra("filename");

        FeedData feedData=new FeedData();
        feedData=feedData.loadfeed(filename,this);

        TextView trackinfo=findViewById(R.id.trackinfo);
        String info=String.format("시작 시간: %s\n종료 시간: %s\n걸음 수: %d보\n이동 거리: %.3fkm",
                feedData.timecheck[0].replace("_"," "),feedData.timecheck[1].replace("_"," ")
                ,feedData.step,feedData.displacement);
        trackinfo.setText(info);

        findViewById(R.id.gofeed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gofeed=new Intent(EndtrackingActivity.this, FeedWrite_Activity.class);
                gofeed.putExtra("filename",filename);
                startActivity(gofeed);
                finish();
            }
        });

        findViewById(R.id.home_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EndtrackingActivity.this,MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}