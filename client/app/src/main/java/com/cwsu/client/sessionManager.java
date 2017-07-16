package com.cwsu.client;

import android.content.Context;
import android.content.SharedPreferences;


class sessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static String userNameTag = "userName";
    private static String KuTag = "Ku";
    private static String tokenTag = "token";
    private static String adminTag = "admin";

    sessionManager(Context context){

        int PRIVATE_MODE = 0;
        String prefName = "LoginSession";
        pref = context.getSharedPreferences(prefName, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }
    void setUserName(String name){
        editor.putString(userNameTag, name);
        editor.commit();
    }
    void setKu(String Ku){
        editor.putString(KuTag, Ku);
        editor.commit();
    }

    void setToken(String token) {
        editor.putString(tokenTag, token);
        editor.commit();
    }
    void setAdmin(String isAdmin) {
        editor.putString(adminTag, isAdmin);
        editor.commit();
    }

    String getUserName(){
        return pref.getString(userNameTag, "not exist");
    }
    String getKu(){return pref.getString(KuTag, "not avaliable");}
    String getToken(){return pref.getString(tokenTag, "not avaliable");}
    String getAdmin(){return pref.getString(adminTag, "not avaliable");}
}
