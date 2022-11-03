package com.example.walkingmate;

import static android.graphics.Color.*;
import static androidx.annotation.Dimension.DP;
import static com.example.walkingmate.R.drawable.bottom_navigation;
import static com.example.walkingmate.R.drawable.selected_day;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.CalendarWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Challenge;

public class FeedCalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MaterialCalendarView calendarView;
    Button plusBtn,feedBtn;
    private String TAG=this.getClass().getSimpleName();



    View reliable_main, reliable_hori;
    TextView yearText, titletxt, usertitle, username, userreliable_txt;
    CalendarDay selectedDay;

    ImageView userimage;

    FeedData feedData;
    ArrayList<String> feedlist;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerview;

    LinearLayout mainlayout;

    boolean start=true;
    FrameLayout frameLayout;

    private FragmentManager fragmentManager;

    int selected=1;

    UserData userData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_calendar);

        fragmentManager=getSupportFragmentManager();

        LocationManager LocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        titletxt=findViewById(R.id.fragtitle);

        userData=UserData.loadData(this);

        mainlayout=findViewById(R.id.mainLayout_calendar);
        frameLayout=findViewById(R.id.container);




        WalkFragment walkFragment=new WalkFragment();
        fragmentManager.beginTransaction().replace(R.id.container, walkFragment,"walk").commitAllowingStateLoss();



        NavigationBarView navigationBarView=findViewById(R.id.bottom_navigation);
        View walkitem=navigationBarView.findViewById(R.id.walk);


        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentManager fragmentManager=getSupportFragmentManager();

                switch(item.getItemId()){
                    case R.id.walk:
                        selected=1;

                        titletxt.setText("Walking Map");
                        frameLayout.removeView(mainlayout);

                        if(fragmentManager.findFragmentByTag("walk")!=null){
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("walk")).commit();
                        }
                        else{
                            fragmentManager.beginTransaction().add(R.id.container, new WalkFragment(),"walk").commit();
                        }
                        if(fragmentManager.findFragmentByTag("trip")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("trip")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("chat")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("chat")).commit();
                        }


                        return true;
                    case R.id.trip:
                        selected=2;

                        titletxt.setText("여행 메이트 게시판");
                        frameLayout.removeView(mainlayout);
                        if(fragmentManager.findFragmentByTag("walk")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("walk")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("trip")!=null){
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("trip")).commit();
                        }
                        else{
                            fragmentManager.beginTransaction().add(R.id.container, new TripFragment(),"trip").commit();
                        }
                        if(fragmentManager.findFragmentByTag("chat")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("chat")).commit();
                        }


                        return true;
                    case R.id.trace:
                        selected=3;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        walkitem.performClick();
                                    }
                                });
                            }
                        }).start();
                        if (!LocMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(FeedCalendarActivity.this, "GPS가 꺼져있습니다.", Toast.LENGTH_LONG).show();
                            Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsIntent);
                        }
                        else{
                            Intent GoMap =new Intent(FeedCalendarActivity.this, MapActivity.class);
                            startActivity(GoMap);
                        }
                        return true;
                    case R.id.feed:

                        if(fragmentManager.findFragmentByTag("walk")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("walk")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("trip")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("trip")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("chat")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("chat")).commit();
                        }

                        titletxt.setText("피드 캘린더");
                        if(selected!=4){
                            frameLayout.addView(mainlayout);
                        }
                        selected=4;
                        return true;
                    case R.id.chat:
                        selected=5;
                        titletxt.setText("채팅");
                        frameLayout.removeView(mainlayout);
                        if(fragmentManager.findFragmentByTag("walk")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("walk")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("trip")!=null){
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("trip")).commit();
                        }
                        if(fragmentManager.findFragmentByTag("chat")!=null){
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("chat")).commit();
                        }
                        else{
                            fragmentManager.beginTransaction().add(R.id.container, new ChatFragment(),"chat").commit();
                        }

                        return true;

                }
                return true;
            }
        });

        drawerLayout=findViewById(R.id.Calendar_Layout);
        navigationView=findViewById(R.id.navigationView_calendar);
        headerview=navigationView.getHeaderView(0);

        usertitle=headerview.findViewById(R.id.title_sidebar);
        username=headerview.findViewById(R.id.username_sidebar);
        userimage=headerview.findViewById(R.id.userimage_sidebar);

        reliable_hori=headerview.findViewById(R.id.reliable_horizontal);
        reliable_main=headerview.findViewById(R.id.reliable_mainred);
        userreliable_txt=headerview.findViewById(R.id.reliable_number);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView.setNavigationItemSelectedListener(this);

        ImageButton menuBtn=findViewById(R.id.menu_calendar);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(navigationView);
            }
        });


        calendarView=findViewById(R.id.calendarView);
        yearText=findViewById(R.id.year);
        plusBtn=findViewById(R.id.plusbtn);
        feedBtn=findViewById(R.id.feedbtn);

        selectedDay=null;

        feedlist=new ArrayList<>();

        FeedData feedData=new FeedData();
        feedlist=feedData.scanFeedList(this);


        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);
        calendarView.setLeftArrow(android.R.color.white);
        calendarView.setRightArrow(android.R.color.white);

        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);

        calendarView.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                LocalDate inputDate=day.getDate();
                String[] calenderHeaderElements = inputDate.toString().split("-");
                switch (calenderHeaderElements[1]){
                    case "01":calenderHeaderElements[1]="JANUARY"; break;
                    case "02":calenderHeaderElements[1]="FEBRUARY"; break;
                    case "03":calenderHeaderElements[1]="MARCH"; break;
                    case "04":calenderHeaderElements[1]="APRIL"; break;
                    case "05":calenderHeaderElements[1]="MAY"; break;
                    case "06":calenderHeaderElements[1]="JUNE"; break;
                    case "07":calenderHeaderElements[1]="JULY"; break;
                    case "08":calenderHeaderElements[1]="AUGUST"; break;
                    case "09":calenderHeaderElements[1]="SEPTEMBER"; break;
                    case "10":calenderHeaderElements[1]="OCTOBER"; break;
                    case "11":calenderHeaderElements[1]="NOVEMBER"; break;
                    case "12":calenderHeaderElements[1]="DECEMBER"; break;
                    default:
                }

                yearText.setText(calenderHeaderElements[0]);

                StringBuilder calenderStringBuilder=new StringBuilder();
                calenderStringBuilder.append(calenderHeaderElements[1]);
                return calenderStringBuilder.toString();

            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.d("캘린더 날짜",date.toString());
                if(selectedDay!=date){
                    selectedDay=date;
                }
                else{
                    calendarView.clearSelection();
                    selectedDay=null;
                }

            }
        });

        CheckWrittenDays(CalendarDay.today().getYear(),CalendarDay.today().getMonth());

        //달력로 목록 갱신
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                CheckWrittenDays(date.getYear(),date.getMonth());
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gofeed=new Intent(FeedCalendarActivity.this, FeedActivity.class);
                if(selectedDay==null){
                    gofeed.putExtra("iswrite",1);
                    startActivity(gofeed);
                }
                else{
                    gofeed.putExtra("year",selectedDay.getYear());
                    gofeed.putExtra("month",selectedDay.getMonth());
                    gofeed.putExtra("day",selectedDay.getDay());
                    gofeed.putExtra("iswrite",1);
                    startActivity(gofeed);
                }
            }
        });

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gofeed=new Intent(FeedCalendarActivity.this, FeedActivity.class);
                if(selectedDay==null){
                    gofeed.putExtra("iswrite",2);
                    startActivity(gofeed);
                }
                else{
                    gofeed.putExtra("year",selectedDay.getYear());
                    gofeed.putExtra("month",selectedDay.getMonth());
                    gofeed.putExtra("day",selectedDay.getDay());
                    gofeed.putExtra("iswrite",2);
                    startActivity(gofeed);
                }
            }
        });



    }

    public int getdp(int a){
        DisplayMetrics displayMetrics=getResources().getDisplayMetrics();
        return Math.round(a*displayMetrics.density);
    }

    public void setReliable(){
        int horih,mainh,relitxt;

        userData=UserData.loadData(this);
        Long relivalue=userData.reliability;
        int relimax=headerview.findViewById(R.id.reliable_background).getHeight();
        Log.d("높이",relimax+"");
        mainh= (int) ((relimax*relivalue)/100);
        horih=mainh+getdp(9);
        relitxt=horih-getdp(13);

        FrameLayout.LayoutParams lp0=new FrameLayout.LayoutParams(getdp(30),mainh);
        lp0.setMargins(0,0,0,getdp(13));
        lp0.gravity= Gravity.BOTTOM;
        reliable_main.setLayoutParams(lp0);

        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(getdp(30),getdp(4));
        lp.setMargins(getdp(15),0,0,horih);
        lp.gravity= Gravity.BOTTOM;
        reliable_hori.setLayoutParams(lp);

        FrameLayout.LayoutParams lp2=new FrameLayout.LayoutParams(getdp(30),getdp(30));
        lp2.setMargins(getdp(35),0,0,relitxt);
        lp2.gravity= Gravity.BOTTOM;
        userreliable_txt.setLayoutParams(lp2);
        userreliable_txt.setText(relivalue.toString());

        //사이드바 프로필 설정
        String usertitlestr;
        if(userData.title.equals("없음")){
            usertitlestr="[칭호를 설정해주세요]";
        }
        else{
            usertitlestr="["+userData.title+"]";
        }
        usertitle.setText(usertitlestr);
        username.setText(userData.appname);
        Bitmap userimagebmp=UserData.loadImageToBitmap(this);
        Log.d("유저 프로필",(userimagebmp==null)+"");
        if(userimagebmp!=null){
            userimage.setImageBitmap(userimagebmp);
        }
        else{
            userimage.setImageResource(R.drawable.blank_profile);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setReliable();
    }

    //뒤로가기로 나갔을시 홈버튼으로 나간것처럼 만들음. 종료로 인한 오류 방지
    @Override
    public void onBackPressed() {
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(start){
            frameLayout.removeView(mainlayout);
            start=false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckWrittenDays(CalendarDay.today().getYear(),CalendarDay.today().getMonth());
    }


    private final ActivityResultLauncher<Intent> setProfileResult= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() ==RESULT_OK){
                    setReliable();
                }
            });


    public void CheckWrittenDays(int year, int month){
        FirebaseFirestore fb=FirebaseFirestore.getInstance();
        CollectionReference feeddata=fb.collection("feedlist");
        ArrayList<CalendarDay> result=new ArrayList<>();

        feeddata.whereEqualTo("year", year).whereEqualTo("month",month).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(int i=0; i<task.getResult().size(); ++i){
                    String tmp= (String) task.getResult().getDocuments().get(i).get("title");
                    String[] tmps=tmp.split("_");
                    CalendarDay tmpcal=CalendarDay.from(Integer.parseInt(tmps[0].replace("년","")),
                            Integer.parseInt(tmps[1].replace("월","")),
                            Integer.parseInt(tmps[2].replace("일","")));
                    result.add(tmpcal);
                }

                calendarView.clearSelection();
                selectedDay=null;
                feedData=new FeedData();
                feedlist=feedData.scanFeedList(FeedCalendarActivity.this);
                calendarView.removeDecorators();

                calendarView.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new blurSatDecorator(),
                        new blurSunDecorator(), new blurDecorator(), new selDecorator(FeedCalendarActivity.this),
                        new DotDecorator(RED, findDays(feedlist)));
                calendarView.addDecorator(new WrittenDecorator(FeedCalendarActivity.this,result));

            }
        });
    }


    public ArrayList<CalendarDay> findDays(ArrayList<String> feedlist){
        ArrayList<CalendarDay> result=new ArrayList<>();
        if(feedlist==null){
            return result;
        }
        for(int i=0; i<feedlist.size(); ++i){
            CalendarDay tmp=strTocal(feedlist.get(i));
            if(!result.contains(tmp)){
                result.add(tmp);
            }
        }
        return result;
    }

    public CalendarDay strTocal(String dayinput){
        String y,m,d,day;
        day=dayinput.replace("(최근)","");
        y=day.substring(0,4);
        m=day.substring(6,8);
        d=day.substring(10,12);
        Log.d("strTocal",y+m+d);
        return CalendarDay.from(Integer.parseInt(y),Integer.parseInt(m),Integer.parseInt(d));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        Intent intent=new Intent();
        if(id==R.id.challenge){
            intent=new Intent(this, challenge_activity.class);
            startActivity(intent);
        }
        else if(id==R.id.managefriends){
            intent=new Intent(this, ManageFriend_Activity.class);
            startActivity(intent);
        }
        else if(id==R.id.helpinfo){
            intent=new Intent(this, HelpInfo_Activity.class);
            startActivity(intent);
        }
        else if(id==R.id.appinfo){
            intent=new Intent(this, AppInfo_Activity.class);
            startActivity(intent);
        }
        else if(id==R.id.settingprofile){
            intent=new Intent(this, EditUserProfileActivity.class);
            setProfileResult.launch(intent);
        }
        return true;
    }
}

class SaturdayDecorator implements DayViewDecorator{
    
    public SaturdayDecorator(){}
    
    @Override
    public boolean shouldDecorate(CalendarDay day){
        DayOfWeek dayOfWeek=day.getDate().getDayOfWeek();
        return dayOfWeek.getValue()==Calendar.FRIDAY;
    }

    @Override
    public void decorate(DayViewFacade view){
        view.addSpan(new ForegroundColorSpan(BLUE));
    }
}

class SundayDecorator implements DayViewDecorator{

    public SundayDecorator(){}

    @Override
    public boolean shouldDecorate(CalendarDay day){
        DayOfWeek dayOfWeek=day.getDate().getDayOfWeek();
        return dayOfWeek.getValue()==Calendar.SATURDAY;
    }

    @Override
    public void decorate(DayViewFacade view){
        view.addSpan(new ForegroundColorSpan(RED));
    }
}

class blurSatDecorator implements DayViewDecorator{

    private CalendarDay date;

    public blurSatDecorator(){
        date=CalendarDay.today();
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        DayOfWeek dayOfWeek=day.getDate().getDayOfWeek();
        return day.isAfter(date)&&dayOfWeek.getValue()==Calendar.FRIDAY;
    }

    @Override
    public void decorate(DayViewFacade view){

        view.addSpan(new ForegroundColorSpan(argb(50,0,0,255)));
    }
}

class blurSunDecorator implements DayViewDecorator{

    private CalendarDay date;

    public blurSunDecorator(){
        date=CalendarDay.today();
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        DayOfWeek dayOfWeek=day.getDate().getDayOfWeek();
        return day.isAfter(date)&&dayOfWeek.getValue()==Calendar.SATURDAY;
    }

    @Override
    public void decorate(DayViewFacade view){

        view.addSpan(new ForegroundColorSpan(argb(50,255,0,0)));
    }
}

class blurDecorator implements DayViewDecorator{

    private CalendarDay date;

    public blurDecorator(){
        date=CalendarDay.today();
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        DayOfWeek dayOfWeek=day.getDate().getDayOfWeek();
        return day.isAfter(date)&&dayOfWeek.getValue()!=Calendar.SATURDAY&&dayOfWeek.getValue()!=Calendar.FRIDAY;
    }

    @Override
    public void decorate(DayViewFacade view){
        view.addSpan(new ForegroundColorSpan(argb(50,0,0,0)));
    }
}

class selDecorator implements DayViewDecorator{

    private final Drawable drawable;


    public selDecorator(Activity activity){
        drawable=activity.getResources().getDrawable(selected_day);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        return true;
    }

    @Override
    public void decorate(DayViewFacade view){
        view.setSelectionDrawable(drawable);
    }
}

class DotDecorator implements DayViewDecorator{

    private final int color;
    private final ArrayList<CalendarDay> days;

    public DotDecorator(int color, ArrayList<CalendarDay> days){
        this.color=color;
        this.days=days;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        return days.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view){
        view.addSpan(new DotSpan(7,color));
    }
}

class WrittenDecorator implements DayViewDecorator{

    private final Drawable drawablerec;
    private final ArrayList<CalendarDay> wdays;


    public WrittenDecorator(Activity activity,ArrayList<CalendarDay> wdays){
        drawablerec=activity.getResources().getDrawable(R.drawable.written_selector);
        this.wdays=wdays;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day){
        return wdays.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view){
        view.setSelectionDrawable(drawablerec);
    }
}