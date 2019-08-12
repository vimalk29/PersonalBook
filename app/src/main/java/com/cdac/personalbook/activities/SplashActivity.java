package com.cdac.personalbook.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cdac.personalbook.R;
import com.cdac.personalbook.common.AlertMessage;
import com.cdac.personalbook.common.SessionManagement;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    final int PERMISSION_ALL = 1;
    final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!isTaskRoot()) {
            finish();
            return;
        }

        Timer timer  = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //if(hasPermissions(SplashActivity.this,PERMISSIONS)){
                    openNextActivity();
                //}else
                  //  requestPermission();
            }
        },2000);
    }

    public void openNextActivity(){
        SessionManagement sessionManagement = new SessionManagement(SplashActivity.this);
        if(sessionManagement.checkLogin())
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
        else
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

    public static boolean hasPermissions(Context context, String... permissions){
        if(context!=null && permissions!=null){
            for (String permission:permissions) {
                if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,PERMISSIONS, PERMISSION_ALL);
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_ALL:{
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                );
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openNextActivity();
                }
                else if(!showRationale){
                    notifyUser(true);
                }else{
                    // permission denied
                    notifyUser(false);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!hasPermissions(this,PERMISSIONS)) {
            requestPermission();
        }else{
            openNextActivity();
        }
    }

    private void notifyUser(final boolean openSetting){
        AlertMessage.showMessageDialog(this, "Please provide permission to continue", "Ok", "Exit", new AlertMessage.YesNoListener() {
            @Override
            public void onDecision(boolean btnClicked) {
                if(btnClicked){
                    if(openSetting) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 2);
                    }
                    else
                        requestPermission();
                }else
                    finish();
            }
        });
    }
}
