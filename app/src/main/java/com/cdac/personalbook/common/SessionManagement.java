package com.cdac.personalbook.common;

import android.content.Context;
import android.content.SharedPreferences;

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

    public SessionManagement(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
    }

    public void createLoginSession(String ID,String username){
        editor = preferences.edit();
        editor.putString(KEY_ID,ID);
        editor.putString(KEY_USERNAME,username);
        editor.putBoolean(IS_LOGGED_IN,true);
        editor.apply();
    }

    public void logOut(){
        editor = preferences.edit();
        editor.putBoolean(IS_LOGGED_IN,false);
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

}

