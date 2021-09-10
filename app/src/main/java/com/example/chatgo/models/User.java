package com.example.chatgo.models;

public class User {
    private String uid , name , phonenumber , profileimg;

    public User(){}

    public User(String uid, String name, String phonenumber, String profileimg) {

        this.uid = uid;
        this.name = name;
        this.phonenumber = phonenumber;
        this.profileimg = profileimg;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getProfileimg() {
        return profileimg;
    }

    public void setProfileimg(String profileimg) {
        this.profileimg = profileimg;
    }
}
