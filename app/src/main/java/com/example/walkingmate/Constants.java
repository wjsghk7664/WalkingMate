package com.example.walkingmate;

//작동 코드관련 고정 상수값들을 모아두는 클래스

public class Constants {
    //위치 기록 서비스와 노티바
    static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    static final int LOCATION_SERVICE_ID=175;


    //발걸음 서비스
    static final String ACTION_START_STEP_COUNTER_SERVICE="startStepCounterService";
    static final String ACTION_STOP_STEP_COUNTER_SERVICE="stopStepCounterService";

    //지난 시간 기록
    static final String ACTION_START_TIMECEHCKING_SERVICE="startTimechecking" ;
    static final String ACTION_STOP_TIMECHECKING_SERVICE="endTimechecking";



}
