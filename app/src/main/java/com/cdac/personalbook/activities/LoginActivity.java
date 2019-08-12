package com.cdac.personalbook.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cdac.personalbook.R;
import com.cdac.personalbook.common.AlertMessage;
import com.cdac.personalbook.common.SessionManagement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnAction;
    EditText etMobile, etOtp;
    String mobile, otp, otpVerificationID;
    AVLoadingIndicatorView progressbar;
    SessionManagement sessionManagement;
    boolean btnAsSentOTP,twiceBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAsSentOTP = true;
        twiceBackPressed = false;
        sessionManagement = new SessionManagement(LoginActivity.this);
        progressbar = findViewById(R.id.progressBar);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setOnClickListener(this);
        progressbar.hide();
        enterMobile();
    }

    @Override
    public void onClick(View view) {
        if (btnAsSentOTP) {
            mobile = etMobile.getText().toString();
            if (validMobileNo(mobile)) {
                phoneAuthentication();
            } else
                Toast.makeText(this, "Enter valid mobile number", Toast.LENGTH_SHORT).show();
        }else{//button as login
            if(otpVerificationID==null||otpVerificationID.isEmpty()){
                enterMobile();
                return;
            }
            otp = etOtp.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpVerificationID,otp);
            signInWithPhoneAuthCredential(credential);
        }
        twiceBackPressed=false;
    }

    private void phoneAuthentication() {
        progressbar.show();
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber("+91" + mobile, 10L, TimeUnit.SECONDS,
                        this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                progressbar.hide();
                enterOTP();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressbar.hide();
                Log.d("1234", "onVerificationFailed: "+e.getMessage());
                AlertMessage.showMessageDialog(LoginActivity.this, "Otp could not be sent to this mobile number", new AlertMessage.OkListener() {
                    @Override
                    public void onOkClicked() {
                        enterMobile();
                    }
                });
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                otpVerificationID = s;
                etOtp.setVisibility(View.VISIBLE);
                etOtp.setEnabled(false);
                progressbar.hide();
                enterOTP();

            }
        });
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential phoneAuthCredential) {
        btnAction.setEnabled(false);
        etOtp.setEnabled(false);
        etMobile.setEnabled(false);
        progressbar.show();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        etOtp.setText(phoneAuthCredential.getSmsCode());
                        progressbar.hide();
                        if(firebaseAuth.getCurrentUser()!=null){
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    final String ID = firebaseAuth.getCurrentUser().getUid();
                                    DatabaseReference db = FirebaseDatabase.getInstance().getReference()
                                            .child("User").child(ID).child("Auth").child("Pattern");
                                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String pattern = dataSnapshot.getValue(String.class);
                                            sessionManagement.createLoginSession(ID, mobile,pattern);
                                            startActivity(new Intent(LoginActivity.this, AuthActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }, 1000);
                        }else
                            progressbar.hide();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                AlertMessage.showMessageDialog(LoginActivity.this, "Invalid OTP", "Retype OTP", "change Mobile no", new AlertMessage.YesNoListener() {
                                    @Override
                                    public void onDecision(boolean btnClicked) {
                                        if(btnClicked){//retype otp
                                            enterOTP();
                                        }else{//type another mobile no
                                            enterMobile();
                                        }
                                    }
                                });
                                progressbar.hide();
                            }
                        },1000);
                    }
                });
    }
    private boolean validMobileNo(String mobile) {
        return android.util.Patterns.PHONE.matcher(mobile).matches();
    }
    public void enterOTP(){
        etMobile.setEnabled(false);
        etOtp.setVisibility(View.VISIBLE);
        etOtp.setEnabled(true);
        btnAsSentOTP = false;
        btnAction.setText("Login");
        btnAction.setEnabled(true);
    }
    public void enterMobile(){
        etOtp.setText("");
        etOtp.setVisibility(View.GONE);
        etMobile.setEnabled(true);
        btnAction.setText("Send OTP");
        btnAction.setEnabled(true);
        btnAsSentOTP = true;
    }

    @Override
    public void onBackPressed() {
        if (twiceBackPressed)
            super.onBackPressed();
        else
            twiceBackPressed = true;
    }
}