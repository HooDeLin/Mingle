package com.orbital2015.mingle;

import android.graphics.Bitmap;

public class UserListItem {

    private String memberName;
    private Bitmap profilePic;
    private String status;

    public UserListItem(String memberName, Bitmap profilePic, String status){
        this.memberName = memberName;
        this.profilePic = profilePic;
        this.status = status;
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

}
