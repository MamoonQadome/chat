package com.example.chat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chat.databinding.ActivityUsersInfoBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;


public class UsersInfoActivity extends AppCompatActivity {

    private ActivityUsersInfoBinding binding;
    private User user;
    FirebaseFirestore database;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
        loadReceiverDetails();

    }


    private void setListener(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }


    private void loadReceiverDetails(){
    User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER_ID);

    binding.textName.setText(user.name);
    binding.textEmail.setText("\t"+user.email);
        byte[] bytes = Base64.decode(user.image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}