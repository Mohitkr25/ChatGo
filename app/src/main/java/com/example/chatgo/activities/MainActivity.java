package com.example.chatgo.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatgo.R;
import com.example.chatgo.adapters.TopStatusAdapter;
import com.example.chatgo.models.Status;
import com.example.chatgo.models.User;
import com.example.chatgo.databinding.ActivityMainBinding;
import com.example.chatgo.adapters.usersAdapter;
import com.example.chatgo.models.userStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    usersAdapter usersadapter;
    TopStatusAdapter statusAdapter;
    ArrayList<userStatus> userStatuses;
    ProgressDialog dialogue;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dialogue = new ProgressDialog(this);
        dialogue.setMessage("Uploading Image");
        dialogue.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        users = new ArrayList<>();
        userStatuses = new ArrayList<>();
        usersadapter = new usersAdapter(this , users);
        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        statusAdapter =new TopStatusAdapter(this , userStatuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);
        binding.recyclerviews.setAdapter(usersadapter);

        binding.recyclerviews.showShimmerAdapter();

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user);
                }

                binding.recyclerviews.hideShimmerAdapter();
                usersadapter.notifyDataSetChanged();
           }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                userStatuses.clear();
                for(DataSnapshot storysnapshot : snapshot.getChildren()){
                    userStatus status = new userStatus();
                    status.setName(storysnapshot.child("name").getValue(String.class));
                    status.setProfileImage(storysnapshot.child("profileImage").getValue(String.class));
                    status.setLastUpdated(storysnapshot.child("lastUpdated").getValue(Long.class));

                    ArrayList<Status> statues = new ArrayList<>();
                    for(DataSnapshot statusnapshot :storysnapshot.child("statuses").getChildren() ){
                        Status samplestatus = statusnapshot.getValue(Status.class);
                        statues.add(samplestatus);
                    }
                    status.setStatuses(statues);

                    userStatuses.add(status);

                }
                statusAdapter.notifyDataSetChanged();
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               switch (item.getItemId()){
                   case R.id.status:
                       Intent intent = new Intent();
                       intent.setType("image/*");
                       intent.setAction(Intent.ACTION_GET_CONTENT);
                       startActivityForResult(intent , 75);
                       break;
               }
               return false;
           }
       });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(data.getData() !=  null){
                dialogue.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                 userStatus userstatus = new userStatus();
                                 userstatus.setName(user.getName());
                                 user.setProfileimg(user.getProfileimg());
                                 userstatus.setLastUpdated(date.getTime());
                                    HashMap<String , Object> obj = new HashMap<>();
                                    obj.put("name" , userstatus.getName());
                                    obj.put("profileImage" , userstatus.getProfileImage());
                                    obj.put("lastUpdated" , userstatus.getLastUpdated());

                                    String imgurl  = uri.toString();
                                    Status  status = new Status(imgurl , userstatus.getLastUpdated());


                                 database.getReference().child("stories")
                                         .child(FirebaseAuth.getInstance().getUid())
                                         .updateChildren(obj);

                                 database.getReference().child("stories")
                                         .child(FirebaseAuth.getInstance().getUid())
                                         .child("statuses")
                                         .push()
                                         .setValue(status);

                                    dialogue.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search is Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "Setting clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:
                Toast.makeText(this, "Invited clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                auth.signOut();

                Intent intent = new Intent(MainActivity.this , loginActivity.class);
                startActivity(intent);
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu , menu);
        return super.onCreateOptionsMenu(menu);
    }
}