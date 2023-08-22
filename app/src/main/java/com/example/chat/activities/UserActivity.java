package com.example.chat.activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;


import com.example.chat.adapters.UserAdapter;
import com.example.chat.databinding.ActivityUserBinding;
import com.example.chat.listeners.UserListener;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {
    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loading(false);
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
//    private void getUsers(){
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .get()
//                .addOnCompleteListener(task -> {
//                    loading(false);
//                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID,"");
//                    if(task.isSuccessful() && task.getResult()!=null){
//                        List<User> users = new ArrayList<>();
//                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
//                            if(currentUserId.equals(queryDocumentSnapshot.getId())) {
//                                continue;
//                            }
//                            User user = new User();
//                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
//                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
//                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
//                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
//                            user.id = queryDocumentSnapshot.getId();
//                            users.add(user);
//                        }
//                        if(users.size() > 0){
//                            UserAdapter userAdapter = new UserAdapter(users,this);
//                            binding.userRecycleView.setAdapter(userAdapter);
//                            binding.userRecycleView.setVisibility(View.VISIBLE);
//                        }
//                        else{
//                            showErrorMessage();
//                        }
//
//                    }
//                    else{
//                        showErrorMessage();
//                    }
//                });
//
//    }

    private void getUsers(){
        binding.searchForUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if(binding.searchForUser.getText().toString().equals("")){
                    binding.userRecycleView.setVisibility(View.INVISIBLE);

//                }
//                else {
//                    searchUsers();
//                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(binding.searchForUser.getText().toString().matches("")){
                    binding.userRecycleView.setVisibility(View.INVISIBLE);

                }
                else {
                    searchUsers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.searchForUser.getText().toString().matches("")){
                    binding.userRecycleView.setVisibility(View.INVISIBLE);

                }
                else {
                    searchUsers();
                }
            }
        });

    }

    private void searchUsers(){
//        if(binding.searchForUser.getText().toString().matches(""))
//            return;

        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID,"");
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            String input = binding.searchForUser.getText().toString();
                            String searchedUser = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            if(searchedUser.contains(input)) {
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                            }
                        }
                        if(binding.searchForUser.getText().toString().matches("")) {
                         //   showErrorMessage(true);
                            return;
                        }
                        if(users.size() > 0){
                          //  showErrorMessage(false);
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userRecycleView.setAdapter(userAdapter);
                            binding.userRecycleView.setVisibility(View.VISIBLE);
                        }


                    }

                });
    }


//    private void showErrorMessage(Boolean isError){
//        binding.textErrorMessage.setText(String.format("%s","No user available"));
//        if(isError)
//            binding.textErrorMessage.setVisibility(View.VISIBLE);
//        else
//            binding.textErrorMessage.setVisibility(View.INVISIBLE);
//    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }
    private void loading(Boolean isLoading){
        if(isLoading)
            binding.progressBar.setVisibility(View.VISIBLE);
        else
            binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}