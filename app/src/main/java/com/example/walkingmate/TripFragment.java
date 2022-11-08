package com.example.walkingmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TripFragment extends Fragment {
    FirebaseFirestore fb=FirebaseFirestore.getInstance();
    CollectionReference tripdata=fb.collection("tripdatalist");

    ListView triplist;
    TripAdapter tripAdapter;

    ImageButton addtrip;
    String curitem="0";

    ArrayList<String> tripdocuids=new ArrayList<>();
    HashMap<String,String> titles=new HashMap<>();
    HashMap<String, String> dates=new HashMap<>();
    HashMap<String, ArrayList<String>> locations=new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_trip, container, false);

        triplist=view.findViewById(R.id.triplist);
        addtrip=view.findViewById(R.id.add_triplist);
        addtrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),TripwriteActivity.class));
            }
        });



        return view;
    }

    public void getlist(){
        tripdata.whereGreaterThan("writetime",curitem).orderBy("writetime").limit(25).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    QuerySnapshot documents=task.getResult();
                    for(DocumentSnapshot document: documents){
                        tripdocuids.add(document.getId());
                        titles.put(document.getId(), (String) document.get("title"));
                        try {
                            dates.put(document.getId(),getdatestr(document.get("year"),document.get("month"),document.get("day"),document.get("hour"),document.get("minute"),document.get("takentime")));
                        } catch (ParseException e) {
                            dates.put(document.getId()," ");
                        }
                        locations.put(document.getId(), (ArrayList<String>) document.get("locations_name"));
                    }
                }
            }
        });
    }

    public String getdatestr(Object y, Object m, Object d, Object h, Object min, Object taken) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm");
        String start=String.format("%04d/%02d/%02d %02d:%02d",y,m,d,h,min);
        Date date=sdf.parse(start);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE,(int)taken);
        return sdf.format(calendar.getTime());
    }


    public class TripAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;


        public TripAdapter(Context context){
            this.layoutInflater = LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return tripdocuids.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.list_layout, null);
            View emptyview=layoutInflater.inflate(R.layout.list_layout_empty,null);
            if(tripdocuids.size()==0){
                return emptyview;
            }

            View bodyView = view.findViewById(R.id.trip_body);
            bodyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(),TripViewActivity.class);
                    intent.putExtra("docuid",tripdocuids.get(position));
                    startActivity(intent);
                }
            });

            TextView title=view.findViewById(R.id.title_trip);
            TextView datetxt=view.findViewById(R.id.trip_date);


            return view;

        }
    }
}