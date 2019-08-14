package com.saikat.skat_sd.whatsappclone.User;

import java.io.Serializable;

public class UserObject implements Serializable {
    private String uid,name,phone,notificationKey;
    private Boolean selected=false;

    public UserObject(String uid){

        this.uid=uid;

    }

    public UserObject(String uid, String name,String phone){
        this.name=name;
        this.phone=phone;
        this.uid=uid;

    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getUid() {
        return uid;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
