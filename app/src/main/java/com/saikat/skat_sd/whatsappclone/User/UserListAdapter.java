package com.saikat.skat_sd.whatsappclone.User;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saikat.skat_sd.whatsappclone.R;

import java.util.ArrayList;
import java.util.HashMap;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    ArrayList<UserObject> userList;

    public UserListAdapter(ArrayList<UserObject> userList){
        this.userList=userList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,null,false);
        RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        UserListViewHolder rcv=new UserListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, int i) {
        holder.mName.setText(userList.get(i).getName());
        holder.mPhone.setText(userList.get(i).getPhone());

//        holder.mLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createChat(holder.getAdapterPosition());
//
//            }
//        });


        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });

    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder{
        TextView mName,mPhone;
        CheckBox mAdd;
        LinearLayout mLayout;

        UserListViewHolder(View view){
            super(view);
            mName=view.findViewById(R.id.name);
            mPhone=view.findViewById(R.id.phone);
            mAdd=view.findViewById(R.id.add);
            mLayout=view.findViewById(R.id.layout);



        }
    }
}
