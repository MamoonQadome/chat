package com.example.chat.activities;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;


import com.example.chat.R;
import com.example.chat.databinding.ActivityProfileBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.LanguageManager;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private LanguageManager languageManager;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        languageManager = new LanguageManager(this);
        setContentView(binding.getRoot());
   //     showToast(preferenceManager.getString(Constants.KEY_NAME,""));
        loadingProfileInfo();
        setListener();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        languageManager = new LanguageManager(this);
        super.onBackPressed();
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.ButtonChangeAccountInfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                AccountInfoActivity.class)));
        binding.buttonSignOut.setOnClickListener(v -> signOut());
        binding.buttonChangeLanguage.setOnClickListener(v -> showLanguageDialog());
    }
    private void loadingProfileInfo(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE,""), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.contact_defult_icon);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME,""));

    }



    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }



    private void showLanguageDialog(){
    final String [] listLanguages = {"Arabic", "English"};
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(ProfileActivity.this);
        myBuilder.setTitle(R.string.select_language);
        myBuilder.setSingleChoiceItems(listLanguages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if (which == 0) {
                    languageManager.updateResource("ar");
//                    recreate();
                    finish();
                     startActivity(getIntent());
                } else if (which == 1) {
                    languageManager.updateResource("en");
//                   recreate();
                   finish();
                   startActivity(getIntent());
                }
                dialog.dismiss();

            }
        });
        AlertDialog myDialog = myBuilder.create();
        myDialog.show();
    }

//    public void updateResource(String code){
//        Locale locale = new Locale(code);
//        Locale.setDefault(locale);
//        Configuration configuration = new Configuration();
//        configuration.locale = locale;
//        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext()
//                .getResources().getDisplayMetrics());
//
//        languageManager.setLanguage(code);
//    }





    private void signOut() {
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID,"")
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unsent -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                            showToast("Unable to sign out");
                            loading(false);
                        }
                
                );
    }

    private void loading( Boolean isLoading){
        if (isLoading){
            binding.buttonSignOut.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignOut.setVisibility(View.VISIBLE);

        }
    }

}