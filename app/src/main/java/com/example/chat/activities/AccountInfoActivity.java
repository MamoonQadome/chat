package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.chat.R;
import com.example.chat.databinding.ActivityAccountInfoBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.RegexRules;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class AccountInfoActivity extends AppCompatActivity {
    private ActivityAccountInfoBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseUser firebaseUser;
    private FirebaseAuth myAuth;
    private String encodedImage;
    private Boolean doubleBackToExitPressedOnce = false;
    private Boolean isPasswordEmpty = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountInfoBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(this);
        setContentView(binding.getRoot());
        init();
        setListener();
        loadProfileInfo();
    }

    private void init(){
        myAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        firebaseUser = myAuth.getCurrentUser();
        encodedImage = "";
    }


    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce){
           super.onBackPressed();

        }

        if(!TextUtils.isEmpty(binding.inputCurrentPassword.getText().toString())
            ||
            !binding.inputname.getText().toString().equals(preferenceManager.getString(Constants.KEY_NAME,""))
                ||
                !TextUtils.isEmpty(binding.inputConfirmPassword.getText().toString())
        )
        {
            doubleBackToExitPressedOnce = true;
            showToast(getString(R.string.click_again_to_forget_changes));

        }
        else
            super.onBackPressed();




        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        },2000);

    }

    private void loadProfileInfo(){
        binding.inputname.setText(preferenceManager.getString(Constants.KEY_NAME,""));
        binding.inputEmail.setHint(preferenceManager.getString(Constants.KEY_EMAIL,""));
        binding.inputEmail.setEnabled(false);
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE,""), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void setListener(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.removeImageText.setOnClickListener(v -> {
            Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.contact_defult_icon);
            encodedImage = encodeImage(defaultIcon);
            binding.imageProfile.setImageBitmap(defaultIcon);
        });
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseUser.equals("")){
                    showToast(getString(R.string.something_went_wrong));
                }
                else
                    saveNewInfo();
            }
        });

    }

    private void saveNewInfo(){
        loading(true);

        if(!binding.inputname.getText().toString().equals(preferenceManager.getString(Constants.KEY_NAME,""))){
            changeDataBaseInfo(Constants.KEY_NAME, binding.inputname.getText().toString());

        }

        if(!encodedImage.equals("")){
            changeDataBaseInfo(Constants.KEY_IMAGE,encodedImage);
        }

        if(isPasswordEmpty || encodedImage.equals("") ||
                binding.inputname.getText().toString().
                        equals(preferenceManager.getString(Constants.KEY_NAME,""))

        ) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else if(isValidInputEditTexts()){
            String currentPassword = binding.inputCurrentPassword.getText().toString();
            String newPassword = binding.inputNewPassword.getText().toString();
            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),currentPassword);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    changePassword(newPassword);
                    else
                        showToast(getString(R.string.something_went_wrong));



                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });




        loading(false);

        }

        else {
            loading(false);
        }




    }
    private void changeDataBaseInfo(String itemName , String newItem){
        final String[] name = {newItem};
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL , preferenceManager.getString(Constants.KEY_EMAIL,""))
//                .whereEqualTo(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME, ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()){

                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            String documentId = documentSnapshot.getId();
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(documentId)
                                    .update(itemName,newItem)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            name[0] = preferenceManager.getString(itemName,"");
                                        }
                                    });



                        }
                    }

                });

        preferenceManager.putString(itemName , name[0]);


    }


    private void changePassword(String newPassword){
        firebaseUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast(getString(R.string.password_changed_successfully));
                }
                else{
                    showToast(getString(R.string.something_went_wrong));
                }
            }
        });
    }

    private Boolean isValidInputEditTexts(){
        Boolean isValid = true;
        if(binding.inputCurrentPassword.getText().toString().isEmpty()){
            binding.inputCurrentPassword.setError(getString(R.string.this_field_can_t_be_empty));
            isPasswordEmpty = true;
            isValid = false;
        }
        if(binding.inputNewPassword.getText().toString().isEmpty()){
            binding.inputNewPassword.setError(getString(R.string.this_field_can_t_be_empty));
            isPasswordEmpty = true;
            isValid = false;
        }else if(!RegexRules.PASSWORD_PATTERN.matcher(binding.inputNewPassword.getText().toString()).matches()){
            binding.inputname.setError(getString(R.string.password_should_contain) +"\n"+
                    getString(R.string.at_least_4_numbers)+"\n" +
                    getString(R.string.at_least_one_lower_case_letter)+"\n" +
                    getString(R.string.at_least_one_upper_case_letter)+"\n" +
                    getString(R.string.at_least_one_special_character)+"\n" +
                    getString(R.string.no_white_spaces));
            isValid = false;
        }
        if (binding.inputConfirmPassword.getText().toString().isEmpty()){
            binding.inputConfirmPassword.setError(getString(R.string.this_field_can_t_be_empty));
            isPasswordEmpty = true;
            isValid = false;

        } else if (!binding.inputNewPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            binding.inputConfirmPassword.setError(getString(R.string.password_and_confirmation_password_are_not_matching));
            isValid = false;
        }

        if(isValid)
            return true;
        else
            return false;

    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);

                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

//    private String encodeImage(Bitmap bitmap) {
//        int previewWidth = 150;
//        int previewHeight =75;
//        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(bytes, Base64.DEFAULT);
//
//
//
//    }
private String encodeImage(Bitmap bitmap) {
    int previewWidth = binding.imageProfile.getWidth();
    int previewHeight = binding.imageProfile.getHeight();
    Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
    byte[] bytes = byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(bytes, Base64.DEFAULT);

}

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);

        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSave.setVisibility(View.VISIBLE);

        }
    }

}