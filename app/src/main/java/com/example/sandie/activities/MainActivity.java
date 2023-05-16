package com.example.sandie.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sandie.databinding.ActivityMainBinding;
import com.example.sandie.utilities.Constants;
import com.example.sandie.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getToken();
        setListeners();
    }

    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.addChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_KEY).document(
                        preferenceManager.getString(Constants.ID_KEY)
                );
        documentReference.update(Constants.FCM_TOKEN_KEY, token)
                .addOnSuccessListener(unused -> showToast("Токен успешно обновлён"))
                .addOnFailureListener(e -> showToast("Не получилось обновить токен"));
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.USERS_KEY).document(
                    preferenceManager.getString(Constants.ID_KEY)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.FCM_TOKEN_KEY, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Не удалось выйти"));
    }
}