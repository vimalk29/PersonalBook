package com.cdac.personalbook.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.cdac.personalbook.R;
import com.cdac.personalbook.common.SessionManagement;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AuthActivity extends AppCompatActivity {

    private static PatternLockView patternLockView;
    private SessionManagement sessionManagement;
    private TextView paraText;
    private String patternEntered;
    private boolean enteredPattern;
    private ImageView authImage;
    private Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        patternLockView = findViewById(R.id.patternView);
        paraText = findViewById(R.id.fingerprintPara);
        authImage =findViewById(R.id.fingerprintImage);
        sessionManagement = new SessionManagement(this);
        enteredPattern=false;
        if ( sessionManagement.checkBioAuthEnabled()){
            listenForLogin();
        }else{
                patternMade();
        }

    }
    private void listenForLogin(){
        paraText.setText("Please enter the your pattern");
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }
            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }
            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Log.d(getClass().getName(), "Pattern complete: " +
                        PatternLockUtils.patternToString(patternLockView, pattern));
                if(sessionManagement.isCorrectPattern(PatternLockUtils.patternToString(patternLockView, pattern))){
                    authImage.setImageResource(R.mipmap.ic_done);
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Toast.makeText(AuthActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                    runActivity();
                }else{
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(AuthActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                    clearPattern();
                }
            }
            @Override
            public void onCleared() {

            }
        });
    }
    private String patternMade(){
        paraText.setText("Please Add a pattern for better security purposes.\n" +
                "Your Pattern will be saved over our servers " +
                "so that you can log into our app from any device with the same pattern");
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }
            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }
            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (!enteredPattern){
                    Log.d(getClass().getName(), "Pattern complete: " +
                            PatternLockUtils.patternToString(patternLockView, pattern));
                    patternEntered = PatternLockUtils.patternToString(patternLockView, pattern);
                    Log.d("1234", patternEntered);
                    if(patternEntered.length()>=3){
                        paraText.setText("Please Enter the pattern once  more");
                        enteredPattern=true;
                    }else{
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        paraText.setText("Please Enter the pattern with at least 3 dots joined");
                    }
                    clearPattern();
                }else {
                    Log.d(getClass().getName(), "Pattern complete: " +
                            PatternLockUtils.patternToString(patternLockView, pattern));
                    if (PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase(patternEntered)){
                        patternEntered = PatternLockUtils.patternToString(patternLockView, pattern);
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        paraText.setText("Pattern Saved");
                        authImage.setImageResource(R.mipmap.ic_done);
                        sessionManagement.updatePattern(patternEntered);
                        runActivity();
                    }else{
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        paraText.setText("Wrong Pattern");
                        enteredPattern=false;

                    }
                    Log.d("1234", patternEntered);
                    clearPattern();
                }
            }
            @Override
            public void onCleared() {

            }
        });
        return "";
    }
    private void clearPattern(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        patternLockView.clearPattern();
                    }
                });

            }
        },500);
    }
    private void runActivity(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(AuthActivity.this, DashboardActivity.class));
                finish();
            }
        }, 1000);
    }
}
