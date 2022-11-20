package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpInfo_Activity extends AppCompatActivity {

    ImageButton back;

    String[] titles={"마이 페이지", "도전과제", "차단 유저 관리","프로필 설정", "약속 목록", "산책 게시판", "여행 게시판", "도보 기록", "피드", "채팅", "프로필 페이지"};

    String[] contents={"-상단 왼쪽의 메뉴 버튼을 누르면 나오는 사이드바.\n" +
            "-자신의 프로필(선택한 칭호, 닉네임, 프로필 이미지, 신뢰도)을 확인할 수 있다.\n" +
            "-도전과제, 차단유저관리, 프로필 설정, 약속목록 페이지로 이동할 수 있다.",
            "-어플을 사용하면서 특정 조건에 도달하면 유저는 도전과제를 달성한다. \n" +
                    "-도전과제를 달성하면, 도전과제 달성 단계에 따른 칭호를 얻게 되고, ‘프로필 설정’ 페이지에서 원하는 칭호를 선택하여 자신의 프로필에 나타낼 수 있다. \n" +
                    "-도전과제 항목은 총 4개이고 각각 5단계로 구성된다.\n" +
                    "\n" +
                    "\t-건강한 워커: 어플의 도보기록 기능을 사용하면서 이동한 걸음 수.\n" +
                    "\t1단계: 10만보, 2단계: 30만보, 3단계: 50만보, 4단계: 70만보 5단계: 100만보\n" +
                    "\n" +
                    "\t-믿음직한 워커: 유저가 도달한 신뢰도 점수.\n" +
                    "\t1단계: 55점, 2단계: 65점, 3단계: 75점, 4단계: 85점, 5단계: 95점\n" +
                    "\n" +
                    "\t-꾸준한 워커: 주 3회 이상의 도보기록이 지속된 기간.\n" +
                    "\t1단계: 2주, 2단계: 4주, 3단계: 7주, 4단계: 10주, 5단계: 15주\n" +
                    "\n" +
                    "\t-사교적인 워커: 약속을 잡은 횟수\n" +
                    "\t1단계: 3회, 2단계: 5회, 3단계: 10회, 4단계: 20회, 5단계: 40회",
            "-차단한 유저의 목록을 확인하고 관리한다.\n" +
            "-유저를 차단 해제할 수 있다.",
    "-자신의 프로필을 수정할 수 있다.\n" +
            "-‘RESET’ 버튼을 누르면 프로필을 설정하지 않은 기본 이미지로 돌아가고 ‘EDIT’ 버튼을 누르면 기기에 저장된 사진 파일 중에서 프로필 이미지를 선택할 수 있다. \n" +
            "-자신이 얻은 칭호 중에서 원하는 칭호를 선택해서 다른 유저들에게 보여줄 수 있다.\n" +
            "-닉네임을 수정할 수 있다. 밑에 자신의 현재 닉네임이 뜨고, 닉네임을 수정할 때에는 ‘중복확인’을 눌러서 다른 유저와 닉네임이 겹치지 않는지 확인해야 한다.\n" +
            "-‘완료’ 버튼을 누르면 수정한 내용이 프로필에 적용된다.",
            "-자신이 수락 혹은 신청한 약속의 목록을 확인 및 관리하는 페이지이다.\n" +
                    "-리뷰작성’ 버튼은 신청한 약속의 출발시간부터 예상 이동시간이 경과된 뒤에 활성화가 되며, 누르면 약속 상대의 리뷰를 별점으로 남길 수 있고, 여기서 남긴 리뷰는 신뢰도에 영향을 준다.\n" +
                    "-‘삭제’ 버튼을 누르면 목록에서 약속 기록이 삭제된다.",
            "1. 필터링\n" +
                    "\n" +
                    "-지도 상단의 필터링을 통해 유저가 원하는 조건의 게시물만 볼 수 있다.\n" +
                    "-성별은 여성/남성/무관으로 나눠져 있고, 나이는 10대, 20대, 30대…와 같은 방식으로 나눠져 있으며, 보기 항목에서는 ‘내 게시물 제외’(본인이 작성한 게시물은 제외하고 보기), ‘내 게시물’(본인이 작성한 게시물만 보기), ‘신청 게시물’(본인이 메이트를 신청한 게시물만 보기), ‘전체 게시물’(모든 게시물을 보기)를 선택할 수 있다.\n" +
                    "-게시물의 필터링 조건과 게시판의 필터링 조건에 양측이 모두 부합하는 게시물만 지도에 나타난다. \n" +
                    "\tex1. 게시물의 성별 필터링을 남자로 설정하면, 게시판 이용유저가 여자일 때, 해당 게시물은 지도에 뜨지 않음. \n" +
                    "\tex2. 게시판의 성별 필터링을 여자로 설정하면, 게시자의 성별이 남자일 때 해당 게시물은 뜨지 않음.\n" +
                    "\n" +
                    "2. 게시물 보기\n" +
                    "\n" +
                    "-지도에서 게시물을 확인할 수 있는 구성이다. 게시물에 기재된 산책 출발위치가 지도에 핑과 함께 나타난다. \n" +
                    "-핑을 선택하면 게시물의 세부내용(게시자의 닉네임, 연령대, 출발시간, 출발 장소)이 상단에 뜬다. \n" +
                    "-하단의 ‘새로고침’ 버튼(좌측)을 누르면 게시판을 새로고침할 수 있다.\n" +
                    "-세부내용창 하단의 ‘메이트 신청하기’ 버튼을 눌러서 해당 약속에 대해 게시자에게 신청할 수 있다.\n" +
                    "-세부내용창의 유저 프로필 이미지 또는 유저 정보를 선택하면 해당 유저의 프로필 페이지로 이동한다.\n" +
                    "\n" +
                    "3. 신청 및 수락 목록\n" +
                    "\n" +
                    "-게시자는 자신의 게시물을 선택해서 해당 약속의 신청 목록과 수락 목록을 확인 및 관리할 수 있다. \n" +
                    "-1대1 만남 뿐만 아니라, 게시자가 여러 개의 신청을 수락하면 다수의 약속 또한 가능하다.\n" +
                    "-신청 목록에는 각 신청에 대해서 ‘채팅’, ‘수락’, ‘거절’ 버튼이 있다. \n" +
                    "\t-‘채팅’ 버튼을 누르면 수락 전 대화할 수 있는 게시자와 신청자의 1대1 채팅방이 생성된다. \n" +
                    "\t-‘수락’ 버튼을 누르면 신청이 수락되어 신청 목록에서 수락 목록으로 이동하며, 수락 채팅방(약속 확정을 의미)이 생성된다. \n" +
                    "\t-‘거절’ 버튼을 누르면 신청이 거절되고 신청목록에서 삭제된다.\n" +
                    "-맨 하단의 ‘게시물 삭제’ 버튼으로 게시물을 삭제할 수 있다.\n" +
                    "\n" +
                    "4. 게시물 작성\n" +
                    "\n" +
                    "-게시판 하단의 ‘+’ 버튼을 누르면 새로운 게시물을 작성할 수 있다.\n" +
                    "-게시물을 작성할 때에는 산책을 출발할 장소, 시간, 예상 이동 시간을 필수로 적고, 원하는 메이트 조건 필터링은 선택적으로 이용할 수 있다. ",
            "1. 필터링\n" +
                    "\n" +
                    "-상단 오른쪽 버튼을 누르면 필터링 창이 나타난다.\n" +
                    "-여행 지역, 여행 시작 날짜와 종료 날짜, 선호하는 성별과 나이대를 설정할 수 있다.\n" +
                    "-‘설정 적용’ 버튼을 누르면 설정한 필터링이 게시판에 적용되고, ‘설정 초기화’ 버튼을 누르면 필터가 초기 설정으로 돌아간다.\n" +
                    "-필터기능은 산책게시판과 같이 작동한다.\n" +
                    "-‘보기’ 항목에서 ‘내 게시물’은 본인이 작성한 게시물만 띄우고, ‘신청 게시물’은 메이트를 신청한 게시물만 띄우고, ‘전체 게시물’은 모든 게시물을 띄우는 설정이다.\n" +
                    "\n" +
                    "2. 게시물 보기\n" +
                    "\n" +
                    "-게시물 리스트가 띄워지고 각 항목에는 게시물 제목, 여행기간, 작성일시, 작성자 닉네임이 나타난다. \n" +
                    "-게시판 하단의 왼쪽 버튼을 누르면 리스트 맨 위로 이동한다.\n" +
                    "-게시물을 선택하면 세부정보 페이지로 이동한다. 세부정보 페이지에서는 방문할 장소, 여행 동선, 메이트에게 남기는 말, 작성자의 세부 정보를 추가로 볼 수 있다.\n" +
                    "-세부정보 페이지 하단에서 메이트 신청하기 버튼을 눌러 신청한다.\n" +
                    "-본인이 작성한 게시물이라면, 신청 버튼 대신 ‘신청자 목록 확인’ 버튼이 있다. 이 버튼을 누르면 산책게시판과 같은 방식으로 신청자를 확인 및 관리할 수 있다. \n" +
                    "-1대1 만남 뿐만 아니라, 게시자가 여러 개의 신청을 수락하면 다수의 약속 또한 가능하다.\n" +
                    "\n" +
                    "3. 게시물 작성\n" +
                    "\n" +
                    "-게시판 하단의 오른쪽 ‘+’ 버튼을 누르면 게시물 작성 페이지로 이동한다.\n" +
                    "-게시물의 제목, 출발지와 방문할 여행지, 여행날짜와 기간, 필터링, 메이트에게 전할 메시지를 작성한다.\n" +
                    "-‘목적지 추가’ \n" +
                    "\t-‘목적치 추가’ 항목을 누르면 위치 검색창으로 이동한다. \n" +
                    "\t-맨 상단에서 장소를 검색할 수 있다.\n" +
                    "\t-검색칸에 장소를 입력한 상태에서 하단 오른쪽의 ‘이동’ 버튼을 누르면 지도가 이동하여 입력한 장소에 핑이 나타난다. \n" +
                    "\t-지도에서 장소를 확인한 후 ‘추가’ 버튼을 누르면 해당 장소가 여행 경로에 순서대로 추가된다. 장소의 방문 순서는 변경할 순서와 원하는 순서를 차례대로 누르면 변경할 수 있다. \n" +
                    "\t-‘경로 탐색’ 버튼을 누르면 지도에 현재 등록된 장소 순서에 맞춰서 이동 동선을 확인할 수 있다. \n" +
                    "\t-맨 하단의 ‘완료’ 버튼을 누르면 경로가 저장되고 게시물 작성페이지로 돌아간다.",
            "-도보기록은 자신이 걸었던 기록을 남기는 기능이다.\n" +
                    "\n" +
                    "1. 경로추적\n" +
                    "\n" +
                    "-하단 바에서 가운데에 있는 ‘도보기록’ 버튼을 누르면 경로 추적이 시작된다.\n" +
                    "-어플 내에서는 실시간으로 내가 이동하는 경로가 선으로 나타나고, 도보기록을 시작하고부터 걸음 수, 이동거리, 이동시간을 볼 수 있다.\n" +
                    "-하단 오른쪽은 정지버튼을 누르면 경로추적이 종료된다.\n" +
                    "-경로 추적 종료 시, 기록 종료 창에 출발 시간, 종료시간, 걸음 수, 이동거리를 보여주는 창이 뜬다. 해당 창 하단에 오른쪽 버튼을 누르면 홈화면인 산책게시판으로 이동하고, 왼쪽 버튼을 누르면 바로 도보에 상세 기록을 작성하는 페이지로 이동한다.\n" +
                    "\n" +
                    "2. 장소 저장\n" +
                    "\n" +
                    "-경로추적 기능을 사용하면서 걷다가 특정 장소에 대한 기록을 남기고 싶을 때 그 장소에서 마킹 버튼을 누르면 해당 장소의 위치가 핑과 함께 저장되고, 그 장소를 기억하기 위한 간단한 메모를 작성할 수 있다.\n" +
                    "\n" +
                    "3. 알림창\n" +
                    "\n" +
                    "-경로추적을 하면서 기록 페이지를 나오면 알림바에 어플 알림창이 나타난다. \n" +
                    "-알림창에서도 경로추적 종료 버튼과 마킹 버튼을 사용할 수 있다.\n" +
                    "-경로추적 종료 시, 알림창도 사라진다.",
            "1. 피드 작성\n" +
                    "\n" +
                    "-경로추적을 종료한 직후 외에, 피드 게시판에서 하단의 오른쪽 +’ 버튼을 누르면 경로추적은 했으나 게시물로 작성하지 않은 추적기록 목록이 뜨고, 작성하고 싶은 기록을 선택하여 게시물을 작성할 수 있다. 목록에서 ‘삭제’ 버튼을 누르면 해당 추적기록은 삭제된다.\n" +
                    "-특정 기록을 선택하면 게시물 작성 페이지로 이동한다. 작성페이지의 최상단에는 출발 일시가 나타난다. \n" +
                    "-지도에는 추적된 경로와 마킹한 장소가 표시된다.\n" +
                    "-그날 도보를 하면서 느낀 감정과 날씨를 아이콘을 선택할 수 있다. \n" +
                    "-추적한 거리와 걸음 수, 이동시간이 기록되어 있다.\n" +
                    "-도보하면서 찍은 사진을 일기와 함께 올릴 수 있다. Picture 창에 하단 오른쪽 버튼을 누르면 로컬 갤러리로 이동하고, 선택한 사진은 게시물에 올라간다. 여러 장의 사진을 올릴 수 있고, 가로 슬라이드로 사진을 넘겨볼 수 있으며, 하단 왼쪽 버튼을 누르면 현재 화면의 사진이 기록에서 삭제된다.\n" +
                    "-일기처럼 도보에 대한 기록을 남길 수 있는 입력창이 있고, 그 밑에는 도보 중 마킹했던 장소와 해당 장소의 메모를 확인 할 수 있다. 마킹 장소를 터치하면 지도에서 해당 마킹이 강조되어 나타난다.\n" +
                    "-최하단에서는 해당 게시물을 다른 유저에게 공개할 지, 비공개할 지를 설정할 수 있다.\n" +
                    "\n" +
                    "2. 피드 캘린더\n" +
                    "\n" +
                    "-피드 기록은 캘린더 형식으로 나타내는데, 작성한 피드가 있는 날짜는 파란 네모로 표현되고, 아직 작성하지 않은 추적기록이 남아 있다면 날짜 밑에 빨간 점이 찍혀 있다.\n" +
                    "-달력은 가로 슬라이드로 달 단위 이동을 할 수 있다.\n" +
                    "-달력 하단 왼쪽 버튼은 게시물을 작성하기 위한 버튼이고, 오른쪽 버튼은 작성된 피드를 확인하는 버튼이다. 오른쪽 버튼을 누르면 작성된 피드 목록이 나타나고, 목록에서 특정 피드를 선택하면, 게시물 페이지로 이동한다.\n" +
                    "\n" +
                    "3. 피드 게시물 페이지\n" +
                    "-게시물 페이지 상단 오른쪽에 삭제 버튼을 누르면 게시물이 삭제된다.\n" +
                    "-공개/비공개 설정 외에는 수정이 불가능하다.",
    "1.채팅방 생성\n" +
            "\n" +
            "-채팅방이 생성되는 경우는 2가지이다. \n" +
            "\t1. 신청 목록에서 채팅 버튼을 눌러 1대1 채팅방을 만들었을 때\n" +
            "\t2. 신청자를 수락하여 약속 참여자들의 채팅방이 생성되었을 때.\n" +
            "\n" +
            "2. 채팅방 리스트\n" +
            "\n" +
            "-채팅방 리스트에서 각 항목은 채팅방 이름, 인원수, 마지막 메시지가 표시되고, 맨 오른쪽에는 나가기 버튼이 있다. 나가기 버튼을 누르면 채팅방에서 나갈 수 있다.\n" +
            "-채팅방 이름은 약속 게시물의 정보를 나타내는데 다음과 같은 내용과 순서로 이루어진다.\n" +
            "\t-산책게시판 게시물이면 [산책] / 여행게시판 게시물이면 [여행]\n" +
            "\t-신청 목록의 채팅버튼을 눌러 만들어진 1대1 채팅방이면 [개인] / 확정된(수락된 신청자와 게시자) 약속 참가자들이 모인 채팅방이면 [수락]\n" +
            "\t-약속 게시물의 출발 시간\n" +
            "\n" +
            "3. 채팅방\n" +
            "\n" +
            "-상단 오른쪽 버튼을 누르면 채팅방에 있는 유저 목록을 확인할 수 있다. 특정 유저를 선택하면 해당 유저의 프로필 페이지로 이동한다.\n" +
            "-채팅을 주고받을 수 있고, 타임스탬프와 상대 유저의 프로필 이미지, 닉네임은 분 단위 별로 마지막 메시지에 한 번만 나타난다.",
    "-채팅방과 게시물 상세페이지에서 다른 유저의 프로필 페이지로 이동할 수 있다.\n" +
            "-유저의 나이, 성별, 닉네임, 프로필 이미지, 도전과제 칭호, 신뢰도를 확인할 수 있다.\n" +
            "-하단에 ‘차단’ 버튼을 누르면 해당 유저를 차단할 수 있고 차단된 유저의 메이트 신청은 무시된다. (목록에 나타나지 않는다.) 차단된 유저의 게시물 또한 게시판에 나타나지 않는다.\n" +
            "-프로필 페이지 상단 오른쪽에는 신고 버튼이 있다. 신고 버튼을 누르면 신고 창이 뜨고, 신고사유를 선택 후 접수 버튼을 누르면 신고가 접수된다."};


    Button[] btns;
    ListView listView;
    HelpAdapter helpAdapter;

    int curitem=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_info);

        back=findViewById(R.id.back_helpinfo);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent getintent=getIntent();
        curitem=getintent.getIntExtra("curitem",0);

        btns= new Button[]{(Button) findViewById(R.id.mypagebtn),(Button)findViewById(R.id.challengebtn),(Button)findViewById(R.id.managefriendsbtn),
                (Button)findViewById(R.id.setprofilebtn),(Button)findViewById(R.id.schedulebtn),(Button)findViewById(R.id.walkbtn),
                (Button)findViewById(R.id.tripbtn),(Button)findViewById(R.id.recordbtn),(Button)findViewById(R.id.feedbtn),
                (Button)findViewById(R.id.chatbtn),(Button)findViewById(R.id.profilebtn)};

        listView=findViewById(R.id.helplist);
        helpAdapter=new HelpAdapter(getApplicationContext());
        listView.setAdapter(helpAdapter);
        listView.setSelectionFromTop(curitem,0);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int position=i;
                if(i%2!=0){
                    position-=1;
                }
                position/=2;
                if(position==9&&i1>=3){
                    position=10;
                }
                for(int k=0; k<btns.length; ++k){
                    if(k==position){
                        btns[k].setBackgroundResource(R.drawable.orangebtn);
                        btns[k].setTextColor(getResources().getColor(R.color.white));
                    }
                    else {
                        btns[k].setBackgroundResource(R.drawable.plusbtn);
                        btns[k].setTextColor(getResources().getColor(R.color.black));
                    }
                }
            }
        });



        for(int i=0; i<btns.length; ++i){
            int finalI = i;
            btns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listView.setSelectionFromTop(finalI*2,0);
                    Log.d("스크롤 위치",finalI*2+"");
                }
            });
        }
    }


    public class HelpAdapter extends BaseAdapter {

        LayoutInflater layoutInflater;
        Context context;

        public HelpAdapter(Context context){
            this.context=context;
            layoutInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return titles.length+contents.length;
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
            if(position%2==0){
                view=layoutInflater.inflate(R.layout.layout_helptitle,null);
                TextView title=view.findViewById(R.id.helptitle_txt);
                title.setText(titles[position/2]);
            }
            else{
                view=layoutInflater.inflate(R.layout.layout_helpcontent,null);
                TextView content=view.findViewById(R.id.helpcontent_txt);
                content.setText(contents[position/2]);
            }


            return view;
        }
    }
}