package com.example.walkingmate;

import static android.graphics.Color.*;
import static com.example.walkingmate.R.drawable.selected_day;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.CalendarWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Challenge;

public class FeedCalendarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MaterialCalendarView calendarView;
    Button plusBtn,feedBtn;
    private String TAG=this.getClass().getSimpleName();


    TextView yearText;
    CalendarDay selectedDay;

    FeedData feedData;
    ArrayList<String> feedlist;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_calendar);

        drawerLayout=findViewById(R.id.Calendar_Layout);
        navigationView=findViewById(R.id.navigationView_calendar);

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

        calendarView.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new blurSatDecorator(),
                new blurSunDecorator(), new blurDecorator(), new selDecorator(FeedCalendarActivity.this),
                new DotDecorator(RED, findDays(feedlist)));

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

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gofeed=new Intent(FeedCalendarActivity.this, FeedActivity.class);
                if(selectedDay==null){
                    startActivity(gofeed);
                }
                else{
                    gofeed.putExtra("year",selectedDay.getYear());
                    gofeed.putExtra("month",selectedDay.getMonth());
                    gofeed.putExtra("day",selectedDay.getDay());
                    startActivity(gofeed);
                }
            }
        });

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedDay==null){

                }
                else{

                }
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        feedData=new FeedData();
        feedlist=feedData.scanFeedList(FeedCalendarActivity.this);
        calendarView.removeDecorators();
        calendarView.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new blurSatDecorator(),
                new blurSunDecorator(), new blurDecorator(), new selDecorator(FeedCalendarActivity.this),
                new DotDecorator(RED, findDays(feedlist)));
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
        }
        else if(id==R.id.managefriends){
            intent=new Intent(this, ManageFriend_Activity.class);
        }
        else if(id==R.id.helpinfo){
                intent=new Intent(this, HelpInfo_Activity.class);
        }
        else if(id==R.id.appinfo){
                intent=new Intent(this, AppInfo_Activity.class);
        }
        startActivity(intent);
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