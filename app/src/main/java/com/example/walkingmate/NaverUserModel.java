package com.example.walkingmate;

import java.io.Serializable;

public class NaverUserModel implements Serializable {
    private String nickname;
    private String email;
    private String age;
    private String gender;
    private String birthyear;

    public NaverUserModel(String nickname, String email, String age, String gender, String birthyear) {
        this.nickname = nickname;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.birthyear = birthyear;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge(){
        return age;
    }

    public void setAge(String age){
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthyear() {
        return birthyear;
    }

    public void setBirthyear(String birthyear) {
        this.birthyear = birthyear;
    }
}
