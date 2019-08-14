package com.saikat.skat_sd.whatsappclone;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.saikat.skat_sd.whatsappclone.Chat.ChatObject;
import com.saikat.skat_sd.whatsappclone.Chat.MediaAdapter;
import com.saikat.skat_sd.whatsappclone.Chat.MessageAdapter;
import com.saikat.skat_sd.whatsappclone.Chat.MessageObject;
import com.saikat.skat_sd.whatsappclone.User.UserObject;
import com.saikat.skat_sd.whatsappclone.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat,mMedia;
    private RecyclerView.Adapter mChatAdapter,mMediaAdapater;
    private RecyclerView.LayoutManager mChatLayoutManager,mMediaLayoutManager;

    ArrayList<MessageObject> messageList;
    //String chatID;

    ChatObject mChatObject;

    DatabaseReference mChatMessageDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //chatID=getIntent().getExtras().getString("chatID");

        mChatObject=(ChatObject) getIntent().getSerializableExtra("chatObject");

        mChatMessageDb=FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

        Button mSend=findViewById(R.id.send);
        Button mAddMedia=findViewById(R.id.addMedia);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        initialiseMessage();
        initialiseMedia();
        getChatMessages();
    }



    private void getChatMessages() {
        mChatMessageDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    String text="",creatorID="";

                    ArrayList<String> mediaUriList=new ArrayList<>();

                    if(dataSnapshot.child("text").getValue()!=null)
                        text=dataSnapshot.child("text").getValue().toString();

                    if(dataSnapshot.child("creator").getValue()!=null)
                        creatorID=dataSnapshot.child("creator").getValue().toString();

                    if(dataSnapshot.child("media").getChildrenCount()>0)
                        for(DataSnapshot mediaSnapShot:dataSnapshot.child("media").getChildren())
                            mediaUriList.add(mediaSnapShot.getValue().toString());

                    MessageObject mMessage=new MessageObject(dataSnapshot.getKey(),creatorID,text,mediaUriList);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size()-1);
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    int totalMediaUploaded=0;
    ArrayList<String> mediaIdList=new ArrayList<>();
    EditText mMessage;

    private void sendMessage(){
        mMessage=findViewById(R.id.messageDb);
        //if(!mMessage.getText().toString().isEmpty()){

            String messageId=mChatMessageDb.push().getKey();
            final DatabaseReference newMessageDb= mChatMessageDb.child(messageId);
            final Map newMessageMap=new HashMap<>();

            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
            if(!mMessage.getText().toString().isEmpty())
                newMessageMap.put("text",mMessage.getText().toString());




            if(!mediaUriList.isEmpty()){
                for(String mediaUri:mediaUriList){
                    String mediaId=newMessageDb.child("media").push().getKey();
                    mediaIdList.add(mediaId);
                    final StorageReference filePath=FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                    UploadTask uploadTask=filePath.putFile(Uri.parse(mediaUri));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newMessageMap.put("/media/"+mediaIdList.get(totalMediaUploaded)+"/",uri.toString());
                                    totalMediaUploaded++;
                                    if(totalMediaUploaded==mediaUriList.size())
                                        updateDatabaseWithNewMessage(newMessageDb,newMessageMap);

                                }
                            });
                        }
                    });

                }
            }
            else{
                if(!mMessage.getText().toString().isEmpty())
                    updateDatabaseWithNewMessage(newMessageDb,newMessageMap);
            }

        }
       // mMessage.setText(null);
   // }


    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb,Map newMessageMap){
        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        mMediaAdapater.notifyDataSetChanged();

        //sending notification to everyone
        String message;
        //check if msg is media or text

        if(newMessageMap.get("text")!=null)//got a text
            message=newMessageMap.get("text").toString();
        else
            message="Image received";


        for(UserObject mUser:mChatObject.getUserObjectArrayList()){
            if(!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message,"New message",mUser.getNotificationKey());
            }
        }
    }

    private void initialiseMessage() {
        messageList=new ArrayList<>();
        mChat=findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager=new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL,false);
        mChat.setLayoutManager(mChatLayoutManager);

        mChatAdapter=new MessageAdapter(messageList);
        mChat.setAdapter(mChatAdapter);


    }



    int PICK_IMAGE_INTENT=1;
    ArrayList<String > mediaUriList=new ArrayList<>();


    private void initialiseMedia() {
        mediaUriList=new ArrayList<>();
        mMedia=findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager=new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL,false);
        mMedia.setLayoutManager(mMediaLayoutManager);

        mMediaAdapater=new MediaAdapter(getApplicationContext(),mediaUriList);
        mMedia.setAdapter(mMediaAdapater);


    }


    private void openGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture(s)"),PICK_IMAGE_INTENT);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==PICK_IMAGE_INTENT){
                if(data.getClipData()==null) {//picked only one
                    mediaUriList.add(data.getData().toString());
                }
                else{
                    for(int i=0;i<data.getClipData().getItemCount();i++){
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaAdapater.notifyDataSetChanged();
            }
        }
    }
}
