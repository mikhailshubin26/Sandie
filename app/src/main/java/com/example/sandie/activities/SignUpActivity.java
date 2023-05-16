package com.example.sandie.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.sandie.databinding.ActivitySignUpBinding;
import com.example.sandie.utilities.Constants;
import com.example.sandie.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()) {
                signUp();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.NAME_KEY, binding.inputName.getText().toString());
        user.put(Constants.EMAIL_KEY, binding.inputEmail.getText().toString());
        user.put(Constants.PASSWORD_KEY, binding.inputPassword.getText().toString());
        database.collection(Constants.USERS_KEY)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.IS_SIGNED_KEY, true);
                    preferenceManager.putString(Constants.ID_KEY, documentReference.getId());
                    preferenceManager.putString(Constants.NAME_KEY, binding.inputName.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private Boolean isValidSignUpDetails() {
        if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Введите Имя");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Введите Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Введите корректный Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Введите пароль");
            return false;
        } else if (binding.confirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Подтвердите ваш пароль");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.confirmPassword.getText().toString())) {
            showToast("Пароли должны совпадать");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}