package com.example.chatgo.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.chatgo.R;
import com.example.chatgo.adapters.MessagesAdapter;
import com.example.chatgo.databinding.ActivityChatBinding;
import com.example.chatgo.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class chatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom;
    String recieverRoom;
    String recieveruid;
    String senderUid;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database =FirebaseDatabase.getInstance();
        storage =FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Inage");
        dialog.setCancelable(false);
        messages =  new ArrayList<>();
        adapter =  new MessagesAdapter(this , messages , senderRoom ,recieverRoom);
       binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);

        String name  = getIntent().getStringExtra("name");
         recieveruid  = getIntent().getStringExtra("uid");
         senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + recieveruid;
        recieverRoom = recieveruid + senderUid;


        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messagetxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message =  new Message(messagetxt , senderUid , date.getTime());
                binding.messageBox.setText("");
                String randomkey =  database.getReference().push().getKey();

                HashMap<String , Object> lastmsgobj = new HashMap<>();
                lastmsgobj.put("lastmsg" , message.getMessage());
                lastmsgobj.put("lastmsgTime" , date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                database.getReference().child("chats").child(recieverRoom).updateChildren(lastmsgobj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomkey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(recieverRoom)
                                .child("messages")
                                .child(randomkey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                        HashMap<String , Object> lastmsgobj = new HashMap<>();
                        lastmsgobj.put("lastmsg" , message.getMessage());
                        lastmsgobj.put("lastmsgTime" , date.getTime());

                        database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                        database.getReference().child("chats").child(recieverRoom).updateChildren(lastmsgobj);
                    }
                });
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent , 25);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25){
            if(data !=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calender = Calendar.getInstance();
                    StorageReference reference =  storage.getReference().child("chats").child(calender.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                     String filepath = uri.toString();

                                        String messagetxt = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message =  new Message(messagetxt , senderUid , date.getTime());
                                        message.setImageurl(filepath);
                                        message.setMessage("Photo");
                                        binding.messageBox.setText("");
                                        String randomkey =  database.getReference().push().getKey();

                                        HashMap<String , Object> lastmsgobj = new HashMap<>();
                                        lastmsgobj.put("lastmsg" , message.getMessage());
                                        lastmsgobj.put("lastmsgTime" , date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                                        database.getReference().child("chats").child(recieverRoom).updateChildren(lastmsgobj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomkey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(recieverRoom)
                                                        .child("messages")
                                                        .child(randomkey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                                HashMap<String , Object> lastmsgobj = new HashMap<>();
                                                lastmsgobj.put("lastmsg" , message.getMessage());
                                                lastmsgobj.put("lastmsgTime" , date.getTime());

                                                database.getReference().child("chats").child(senderRoom).updateChildren(lastmsgobj);
                                                database.getReference().child("chats").child(recieverRoom).updateChildren(lastmsgobj);
                                            }
                                        });

                                        Toast.makeText(chatActivity.this, filepath, Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chat_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}