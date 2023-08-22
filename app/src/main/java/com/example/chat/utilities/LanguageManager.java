package com.example.chat.utilities;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageManager {
    
    private PreferenceManager preferenceManager;

    private Context context;

    public LanguageManager(Context context){
        this.context = context;
        preferenceManager = new PreferenceManager(context);
    }

    public void updateResource(String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
      //  Resources resources = context.getResources();
        Configuration configuration = new Configuration();
        configuration.locale = locale;

        context.getResources().updateConfiguration(configuration,context.getResources().getDisplayMetrics());
        setLanguage(code);
    }


    public void setLanguage(String code){
    preferenceManager.putString(Constants.LANGUAGE,code);
    }
    

    public void loadLanguage(){
        updateResource(preferenceManager.getString(Constants.LANGUAGE,Locale.getDefault().getLanguage()));
    }


}
