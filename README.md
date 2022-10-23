# WalkingMate

액티비티 및 서비스에 대한 설명

------------이 아래는 FrontEnd-------------------------

0. 로딩화면
 -StartActivity: 처음 로딩화면. 바로 메인화면으로 넘어감.

1. 메인화면
 -MainActivity: 임시 메인화면. 권한 체크 후 필요권한 체크가 안되어 있으면 해당 설정 화면을 띄움. 각 기능별 화면으로 넘어감.

2. 도보 기록 
  -MapActivity: 도보 기록 시작시 켜지는 액티비티, single task로 화면 재시작시 기존 켜져있던 화면이 켜지도록 설정되어 있음.
  -LocationService: 실시간 위치 좌표 수집과 마커,중지 버튼이 포함된 노티바. MapActivity가 onPause단계로 넘어가면 백그라운드로 실행이 된다.
  -TimecheckingService: 실시간 시간 측정. 도보추적 시작과 동시에 백그라운드로 실행된다.
  -StepCounterService: 실시간 걸음수 측정. 도보 추적 시작과 동시에 백그라운드로 실행된다. 
  -EndtrackingActivity: 도보 기록 종료 후 바로 피드작성으로 넘어갈지 홈 화면으로 넘어갈지 선택하는 팝업창.
 
3. 피드 기록 및 확인-캘린더
 -FeedCalendarActivity: 캘린더 화면. 날짜별 미작성 피드와 작성 피드, 그리고 전체 미작성 피드와 작성 피드를 볼 수 있음.
 -FeedActivity: 미작성 피드 확인을 할 시 실행되는 화면. 설정별로 날짜별, 전체피드목록 확인가능

4. MyPage: 다른 화면들 상에 NavigationView로 띄우는 사이드바.
 -challenge_activity: 도전과제 화면
 -HelpInfo_Activity: 도움말 화면
 -AppInfo_Activity: 어플 정보 화면
 -ManageFriend_Activity: 친구관리 화면

5. 피드 작성 
 -FeedWrite_Activity:피드 작성 화면. 받아온 도보경로와 마커를 표시

6. 여행 메이트 구하기 게시물 작성
 -TripwriteActivity: 여행 메이트 구하는 게시물 작성 페이지. 목적지와 경로탐색은 TestActivity에서받고 나머지 정보는 이 페이지에서 작성
 -TestActivity: 주소검색을 통한 지도이동, 목적지 선택, 목적지 주소값 가져오기, 경로탐색. 이중 주소검색은 SearchAcivity로 요청을 보내 받아옴.
 -SearchActivity: 주소지 검색을 위한 Acitivity. 검색된 주소명을 TestActivity로 보냄.
 
 7.산책 메이트 구하기 게시물 작성
 -WalkWriteActivity: 산책 메이트 구하는 게시물 작성 페이지. 출발지는 SetLocationWalkActivity에서 받고 나머지는 이 페이지에서 작성
 -SetLocationWalkActivity: 출발지 선택 페이지. 주소검색을 통한 지도이동과 출발지 선택을 한다.
 -SearchActivity: 여행메이트에 사용된 액티비티와 동일 액티비티. 검색된 주소명을 SetLocationWalkActivity로 보냄

8. 기타 사용 클래스
 -Constants: 고정 상수값들을 저장하기 위한 클래스. Notification채널 아이디와 서비스의 Action 문자열이 저장되어 있음.
 -FeedData: 도보 기록 데이터를 다루기 위한 클래스. 도보기록 정보를 문자열로 encode, decode, save, load, 저장 기록 탐색, 저장 기록 삭제 기능이 존재.

9. UitestingActivity: UI작성시 테스트를 위한 임시 액티비티.




------------이 아래는 BackEnd-------------------------
