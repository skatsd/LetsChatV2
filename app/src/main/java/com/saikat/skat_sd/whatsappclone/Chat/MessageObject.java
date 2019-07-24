package com.saikat.skat_sd.whatsappclone.Chat;

import java.util.ArrayList;

public class MessageObject {
    String messageId, senderId,message;

    ArrayList<String > mediaUriList;

    public MessageObject(String messageId,String senderId,String message,ArrayList<String > mediaUriList){
        this.messageId=messageId;
        this.senderId=senderId;
        this.message=message;
        this.mediaUriList=mediaUriList;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public ArrayList<String> getMediaUriList() {
        return mediaUriList;
    }
}
