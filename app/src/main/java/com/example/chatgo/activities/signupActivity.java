package com.example.chatgo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatgo.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class signupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    private String phonenumber;
    private String otpid;
    FirebaseAuth firebaseauth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseauth = FirebaseAuth.getInstance();

        otpid = getIntent().getStringExtra("auth");

        binding.verifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verifyotp = binding.otpno.getText().toString();
                if(!verifyotp.isEmpty()){
                    PhoneAuthCredential credential  = PhoneAuthProvider.getCredential(otpid , verifyotp);
                    signin(credential);

                }else{
                    Toast.makeText(signupActivity.this, "Please enter correct otp", Toast.LENGTH_SHORT).show();

                }

            }
        });


    }

    private  void signin(PhoneAuthCredential credential){
        firebaseauth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    sendMain();
                }else{
                    Toast.makeText(signupActivity.this, "verification failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curruser = firebaseauth.getCurrentUser();
        if(curruser !=null){
            sendMain();
        }
    }

    private void sendMain(){
        startActivity(new Intent(signupActivity.this , setupprofileActivity.class));
        finish();
    }
}