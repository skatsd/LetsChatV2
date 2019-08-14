package com.saikat.skat_sd.whatsappclone;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saikat.skat_sd.whatsappclone.User.UserListAdapter;
import com.saikat.skat_sd.whatsappclone.User.UserObject;
import com.saikat.skat_sd.whatsappclone.Utils.CountryToPhonePrefix;

import java.util.ArrayList;
import java.util.HashMap;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserLayoutManager;

    ArrayList<UserObject> userList,contactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        contactList=new ArrayList<>();
        userList=new ArrayList<>();

        Button mCreate=findViewById(R.id.create);
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });

        initialiseRecyclerView();
        getContactList();
        
    }


    private void createChat(){
        String key= FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        //now updating user database
        DatabaseReference chatInfoDb=FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");

        DatabaseReference userDb=FirebaseDatabase.getInstance().getReference().child("user");



        HashMap newChatMap=new HashMap();
        newChatMap.put("id",key);
        newChatMap.put("users/"+ FirebaseAuth.getInstance().getUid(),true);
        //places id in users, and sets to true, so that JSON saves it when it sees value in key

        Boolean validChat=false;

        for(UserObject mUser:userList){
            if(mUser.getSelected()){
                validChat=true;
                newChatMap.put("users/"+mUser.getUid(),true);
                userDb.child(mUser.getUid()).child("chat").child(key).setValue(true);

            }
        }

        if(validChat){
            chatInfoDb.updateChildren(newChatMap);

            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);

            Toast.makeText(getApplicationContext(),"Chat Room Created",Toast.LENGTH_SHORT).show();

        }

    }

    private void getContactList(){

        String isoPrefix=getCountryISO();

        Cursor phones=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

        while(phones.moveToNext()){
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phone=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


            phone=phone.replace(" ","");
            phone=phone.replace("-","");
            phone=phone.replace("(","");
            phone=phone.replace(")","");

            if(!String.valueOf(phone.charAt(0)).equals("+"))
                phone=isoPrefix+phone;

            UserObject mContact=new UserObject("",name,phone);
            contactList.add(mContact);
            //mUserListAdapter.notifyDataSetChanged();

            getUserDetails(mContact);
        }
    }

    private void getUserDetails(UserObject mContact) {
        DatabaseReference mUserDB= FirebaseDatabase.getInstance().getReference().child("user");
        Query query=mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String phone="",name="";
                    for(DataSnapshot childsnapShot: dataSnapshot.getChildren()){
                        if(childsnapShot.child("phone").getValue()!=null)
                            phone=childsnapShot.child("phone").getValue().toString();
                        if(childsnapShot.child("name").getValue()!=null)
                            name=childsnapShot.child("name").getValue().toString();

                        UserObject mUser=new UserObject(childsnapShot.getKey(),name,phone);

                        if(name.equals(phone)){
                           for(UserObject mContactIterator:contactList){
                               if(mContactIterator.getPhone().equals(mUser.getPhone())){
                                   mUser.setName(mContactIterator.getName());
                               }
                           }
                        }

                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getCountryISO(){
        String iso=null;

        TelephonyManager telephonyManager=(TelephonyManager)getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null){
            if(!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso=telephonyManager.getNetworkCountryIso().toString();
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initialiseRecyclerView() {
        mUserList=findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserLayoutManager=new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL,false);
        mUserList.setLayoutManager(mUserLayoutManager);

        mUserListAdapter=new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);


    }
}
