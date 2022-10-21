# WalkingMate

액티비티 및 서비스에 대한 설명

------------이 아래는 FrontEnd-------------------------

0. 로딩화면
-StartActivity: 처음 로딩화면. 바로 메인화면으로 넘어감.

1. 메인화면
-MainActivity: 임시 메인화면. 권한 체크 후 필요권한 체크가 안되어 있으면 해당 설정 화면을 띄움. 각 기능별 화면으로 넘어감.

2. 도보 기록 추적 
 -MapActivity: 도보 기록 시작시 켜지는 액티비티, single task로 화면 재시작시 기존 켜져있던 화면이 켜지도록 설정되어 있음.
 -LocationService: 실시간 위치 좌표 수집과 마커,중지 버튼이 포함된 노티바. MapActivity가 onPause단계로 넘어가면 백그라운드로 실행이 된다.
 -TimecheckingService: 실시간 시간 측정. 도보추적 시작과 동시에 백그라운드로 실행된다.
 -StepCounterService: 실시간 걸음수 측정. 도보 추적 시작과 동시에 백그라운드로 실행된다. 
 
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
-TestActivity: 여행 메이트 구하는 게시물 작성 페이지. 주소 검색(SearchActivity)을 통해 해당 위치로 지도 이동. 원하는 시작점과 경유지를 지도에서 선택(해당 주소값이 나옴).
               이후 해당 지점들이 연결된 경로 표시.
-SearchActivity: 주소지 검색을 위한 Acitivity. 검색된 주소의 좌표를 TestActivity로 보냄.

7. 기타 사용 클래스
-Constants: 고정 상수값들을 저장하기 위한 클래스. Notification채널 아이디와 서비스의 Action 문자열이 저장되어 있음.
-FeedData: 도보 기록 데이터를 다루기 위한 클래스. 도보기록 정보를 문자열로 encode, decode, save, load, 저장 기록 탐색, 저장 기록 삭제 기능이 존재.

8. UitestingActivity: UI작성시 테스트를 위한 임시 액티비티.




------------이 아래는 BackEnd-------------------------
