package com.example.chatgo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatgo.R;
import com.example.chatgo.databinding.ItemRecieverBinding;
import com.example.chatgo.databinding.ItemSendBinding;
import com.example.chatgo.models.Message;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final  int ITEM_RECIEVED = 2;

    String senderRoom ;
    String recieverRoom;
    public MessagesAdapter(Context context , ArrayList<Message> messages , String senderRoom ,String recieverRoom){
        this.context = context;
        this.messages = messages;
        this.senderRoom =senderRoom;
        this.recieverRoom = recieverRoom;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send , parent , false);
            return new SentViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_reciever , parent , false);
            return new RecieverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }else{
            return ITEM_RECIEVED;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int reactions[] =  new int[]{
                R.drawable.like,
                R.drawable.heart,
                R.drawable.laughing,
                R.drawable.surprised,
                R.drawable.sad,
                R.drawable.angry,
        };


        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder)holder;

                viewHolder.binding.msgfeelingsender.setImageResource(reactions[pos]);
                viewHolder.binding.msgfeelingsender.setVisibility(View.VISIBLE);
            }else{
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.binding.msgfeelingreciever.setImageResource(reactions[pos]);
                viewHolder.binding.msgfeelingreciever.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(recieverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if(message.getMessage().equals("Photo")){
                viewHolder.binding.imagepic.setVisibility(View.VISIBLE);
                viewHolder.binding.msgtxtsender.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageurl()).into(viewHolder.binding.imagepic);
            }
            viewHolder.binding.msgtxtsender.setText(message.getMessage());

            if(message.getFeeling() >= 0){
                //message.setFeeling(reactions[message.getFeeling()]);
                viewHolder.binding.msgfeelingsender.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.msgfeelingsender.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.msgfeelingsender.setVisibility(View.GONE);
            }

//            viewHolder.binding.msgtxtsender.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    popup.onTouch(v, event);
//                    return false;
//
//                }
//            });

       }else{
            RecieverViewHolder viewHolder = (RecieverViewHolder)holder;
            if(message.getMessage().equals("Photo")){
                viewHolder.binding.imagerec.setVisibility(View.VISIBLE);
                viewHolder.binding.msgtxtreciever.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageurl()).into(viewHolder.binding.imagerec);
            }

            viewHolder.binding.msgtxtreciever.setText(message.getMessage());


            if(message.getFeeling() >= 0){
                //message.setFeeling(reactions[(int)message.getFeeling()]);
                viewHolder.binding.msgfeelingreciever.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.msgfeelingreciever.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.msgfeelingreciever.setVisibility(View.GONE);
            }

            viewHolder.binding.msgtxtreciever.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemSendBinding binding ;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding =  ItemSendBinding.bind(itemView);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder{

        ItemRecieverBinding binding;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieverBinding.bind(itemView);
        }
    }
}
