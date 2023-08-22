package com.example.chat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.chat.R;
import com.example.chat.databinding.ActivitySignUpBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.example.chat.utilities.RegexRules;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    private FirebaseAuth myAuth;
    private Bitmap defaultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        myAuth = FirebaseAuth.getInstance();
        loadImage();
        setListeners();
    }
    private void loadImage(){
        defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.contact_defult_icon);
        binding.imageProfile.setImageBitmap(defaultIcon);
    }


    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });


    }


    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputname.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
       // user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());

        if(encodedImage != null)
            user.put(Constants.KEY_IMAGE, encodedImage);
        else {

            encodedImage = encodeImage(defaultIcon);
            user.put(Constants.KEY_IMAGE, encodedImage);

        }

        myAuth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(),
                binding.inputPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .add(user)
                        .addOnSuccessListener(documentReference -> {
                            loading(false);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                            preferenceManager.putString(Constants.KEY_NAME, binding.inputname.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                            preferenceManager.putString(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(exception -> {
                            loading(false);
                            showToast(exception.getMessage());

                        });
            }
            else {
                showToast(getString(R.string.there_is_an_error_please_contact_supports));
            }
            }

        });

    }

//    private String encodeImage(Bitmap bitmap) {
//        int previewWidth = 150;
//        int previewHeight =75;
//        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private Boolean isValidSignUpDetails() {

        Boolean isEmpty = false, doesMatch = true;
        // First and last name
        if(binding.inputname.getText().toString().isEmpty()){
            editTextsErrors(binding.inputname,getString(R.string.name),true);
            isEmpty = true;
        }else if(!RegexRules.NAME_FORM.matcher(binding.inputname.getText().toString()).matches()){
            editTextsErrors(binding.inputname,getString(R.string.name_should_include_first_and_last_name),false);
            doesMatch = false;
        }
        // Email
        if(binding.inputEmail.getText().toString().isEmpty()){

            editTextsErrors(binding.inputEmail,getString(R.string.email),true);
            isEmpty = true;
        } else if (!RegexRules.EMAIL_PATTERN.matcher(binding.inputEmail.getText().toString()).matches()) {
            editTextsErrors(binding.inputEmail,getString(R.string.email_form_only_contain_hotmail_outlook_gmail_and_yahoo),false);
            doesMatch =false;
        }
        // Password
        if(binding.inputPassword.getText().toString().isEmpty()){
            editTextsErrors(binding.inputPassword,getString(R.string.password),true);
            isEmpty = true;
        } else if (!RegexRules.PASSWORD_PATTERN.matcher(binding.inputPassword.getText().toString()).matches()) {
            editTextsErrors(binding.inputPassword,getString(R.string.password_should_contain) +"\n"+
                    getString(R.string.at_least_4_numbers)+"\n" +
                    getString(R.string.at_least_one_lower_case_letter)+"\n" +
                    getString(R.string.at_least_one_upper_case_letter)+"\n" +
                    getString(R.string.at_least_one_special_character)+"\n" +
                    getString(R.string.no_white_spaces),false);
        }
        // Confirmation Password
        if(binding.inputConfirmPassword.getText().toString().isEmpty()){
            editTextsErrors(binding.inputConfirmPassword,getString(R.string.confirm_password),true);
            isEmpty = true;
        } else if (!binding.inputConfirmPassword.getText().toString().equals(binding.inputPassword.getText().toString())) {
            editTextsErrors(binding.inputConfirmPassword,getString(R.string.password_and_confirmation_password_are_not_matching),false);
        }


        if(!isEmpty && doesMatch)
            return true;
        else
            return false;
        }






    private void editTextsErrors(EditText editText, String str, Boolean isEmpty){
        if(isEmpty)
            editText.setError(str+getString(R.string.can_t_be_empty));
        else
            editText.setError(str);
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);

        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);

        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }


}