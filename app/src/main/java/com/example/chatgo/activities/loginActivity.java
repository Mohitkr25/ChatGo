package com.example.chatgo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.chatgo.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class loginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth mAuth;
    ProgressDialog dialoge;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        dialoge = new ProgressDialog(this);
        dialoge.setTitle("Sending OTP");
        dialoge.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();

        binding.googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(loginActivity.this, "Google Authentication current unavailable at this time", Toast.LENGTH_SHORT).show();
            }
        });


        binding.codepicker.registerCarrierNumberEditText(binding.phoneno);
        binding.nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(binding.phoneno.getText().toString())){
                    Toast.makeText(loginActivity.this, "Enter number", Toast.LENGTH_SHORT).show();
                }else if(binding.phoneno.getText().toString().replace(" ", "").length()!=10){
                    Toast.makeText(loginActivity.this, "Enter correct number", Toast.LENGTH_SHORT).show();
                }else{
                    String completenumber = binding.codepicker.getFullNumberWithPlus().replace(" " , "").toString();
                    dialoge.show();

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(completenumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(loginActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();;
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signin(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(loginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                dialoge.dismiss();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent otpintent = new Intent(loginActivity.this , signupActivity.class);
                        otpintent.putExtra("auth" , s);
                        startActivity(otpintent);
                    }
                } ,6000);


            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if(currentuser !=  null){
            sendMain();
        }
    }

    private void sendMain(){
        Intent Mintent = new  Intent(loginActivity.this , MainActivity.class);
        startActivity(Mintent);
        finish();
    }

    private  void signin(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    sendMain();;
                }else{
                    Toast.makeText(loginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}