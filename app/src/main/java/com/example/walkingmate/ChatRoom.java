package com.example.walkingmate;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//파이어 베이스에서 사용하려면 모든지 무조건 public으로 해야함

public class ChatRoom {
    public String roomid;
    public String roomname;
    public Map<String,Boolean> userids=new HashMap<String, Boolean>();
    public Map<String,Comment> comments=new HashMap<>();

    public ChatRoom(){
    }

    public ChatRoom(String roomid, String roomname, Map<String, Boolean> userids) {
        this.roomid = roomid;
        this.roomname = roomname;
        this.userids = userids;
    }

    public static class Comment{
        public String msg;
        public String time;
        public String userid;

        public Comment(){}
    }

}
