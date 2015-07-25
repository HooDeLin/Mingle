package com.orbital2015.mingle;

import android.graphics.Bitmap;

public class UserListItem {

    private String memberName;
    private Bitmap profilePic;

    public UserListItem(String memberName, Bitmap profilePic){
        this.memberName = memberName;
        this.profilePic = profilePic;
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

}
