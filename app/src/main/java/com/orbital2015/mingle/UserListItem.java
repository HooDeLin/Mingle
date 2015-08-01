package com.orbital2015.mingle;

import android.graphics.Bitmap;

public class UserListItem {

    private String memberName;
    private Bitmap profilePic;
    private String status;
    private String id;

    public UserListItem(String memberName, Bitmap profilePic, String status, String id){
        this.memberName = memberName;
        this.profilePic = profilePic;
        this.status = status;
        this.id = id;
    }

    public String getMemberName(){
        return memberName;
    }

    public void setMemberName(String newMemberName){
        memberName = newMemberName;
    }

    public Bitmap getProfilePic(){
        return profilePic;
    }

    public void setProfilePic(Bitmap newProfilePic){
        profilePic = newProfilePic;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String newStatus){
        status = newStatus;
    }

    public String getId(){
        return id;
    }

    public void setId(String newId){
        id = newId;
    }

}
