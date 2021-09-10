package com.example.chatgo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatgo.R;
import com.example.chatgo.models.User;
import com.example.chatgo.activities.chatActivity;
import com.example.chatgo.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class usersAdapter  extends RecyclerView.Adapter<usersAdapter.userViewHolder> {
    Context context;
    ArrayList<User> users;
    public usersAdapter(Context context , ArrayList<User> users){
        this.context = context;
        this.users  = users;
    }

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation , parent , false);

        return new userViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userViewHolder holder, int position) {

        User user = users.get(position);

        String senderid = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderid + user.getUid();

        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String lastMsg = snapshot.child("lastmsg").getValue(String.class);
                            long time = snapshot.child("lastmsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.msgtime.setText(dateFormat.format(new Date(time)));
                            holder.binding.lastmsg.setText(lastMsg);
                        }else{
                            holder.binding.lastmsg.setText("Tap to chat");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.username.setText(user.getName());
        Glide.with(context).load(user.getProfileimg())
                .placeholder(R.drawable.userimg)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , chatActivity.class);
                intent.putExtra("name" , user.getName());
                intent.putExtra("uid" , user.getUid());
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder{

        RowConversationBinding binding;
        public userViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
