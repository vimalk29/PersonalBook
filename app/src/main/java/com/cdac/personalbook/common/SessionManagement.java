package com.cdac.personalbook.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SessionManagement {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "USER_DETAILS";
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_TOKEN_ID = "TOKEN_ID";
    private static final String KEY_ID = "ID";
    private static final String IS_FIRST_RUN = "IS_APP_FIRST_RUN";
    private static final String KEY_PATTERN = "PATTERN";
    public SessionManagement(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
    }

    public void createLoginSession(String ID,String username,String patternRecieved){
        editor = preferences.edit();
        editor.putString(KEY_ID,ID);
        editor.putString(KEY_USERNAME,username);
        editor.putString(KEY_PATTERN,patternRecieved);
        editor.putBoolean(IS_LOGGED_IN,true);
        editor.apply();
    }

    public void logOut(){
        editor = preferences.edit();
        editor.putBoolean(IS_LOGGED_IN,false);
        editor.putString(KEY_PATTERN,"");
        editor.apply();
    }

    public void updateToken(String token){
        editor = preferences.edit();
        editor.putString(KEY_TOKEN_ID,token);
        editor.apply();
    }
    public String getID(){return  preferences.getString(KEY_ID,"cust");}
    public String getToken(){
        return preferences.getString(KEY_TOKEN_ID,"");
    }
    public boolean checkLogin(){
        return preferences.getBoolean(IS_LOGGED_IN,false);
    }

    public String getUsername(){
        return preferences.getString(KEY_USERNAME,"");
    }

    public boolean checkBioAuthEnabled(){
        if (preferences.getString(KEY_PATTERN,"").isEmpty())
            return false;
        return true;
    }
    public boolean isCorrectPattern(String pattern){
        return pattern.equalsIgnoreCase(preferences.getString(KEY_PATTERN, ""));
    }
    public void updatePattern(final String pattern){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("User").child(getID()).child("Auth").child("Pattern");
        databaseReference.setValue(pattern).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                editor = preferences.edit();
                editor.putString(KEY_PATTERN,pattern);
                editor.apply();
            }
        });

    }
}

