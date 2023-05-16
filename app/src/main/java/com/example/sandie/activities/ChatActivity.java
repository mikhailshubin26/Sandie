package com.example.sandie.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.example.sandie.adapters.ChatAdapter;
import com.example.sandie.databinding.ActivityChatBinding;
import com.example.sandie.models.ChatMessage;
import com.example.sandie.models.User;
import com.example.sandie.utilities.Constants;
import com.example.sandie.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User recieverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadRecieverDetails();
        init();
        listenMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                preferenceManager.getString(Constants.ID_KEY)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.SENDER_KEY, preferenceManager.getString(Constants.ID_KEY));
        message.put(Constants.RECEIVER_KEY, recieverUser.id);
        message.put(Constants.MESSAGE_KEY, binding.inputMessage.getText().toString());
        message.put(Constants.TIMESTAMP_KEY, new Date());
        database.collection(Constants.CHAT_KEY).add(message);
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection(Constants.CHAT_KEY)
                .whereEqualTo(Constants.SENDER_KEY, preferenceManager.getString(Constants.ID_KEY))
                .whereEqualTo(Constants.RECEIVER_KEY, recieverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.CHAT_KEY)
                .whereEqualTo(Constants.SENDER_KEY, recieverUser.id)
                .whereEqualTo(Constants.RECEIVER_KEY, preferenceManager.getString(Constants.USER_KEY))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }
        if(value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.SENDER_KEY);
                    chatMessage.recieverId = documentChange.getDocument().getString(Constants.RECEIVER_KEY);
                    chatMessage.message = documentChange.getDocument().getString(Constants.MESSAGE_KEY);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.TIMESTAMP_KEY));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.TIMESTAMP_KEY);
                    chatMessages.add(chatMessage);
                    System.out.println(chatMessage.message);
                }
            }
            chatMessages.sort((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0) {
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private void loadRecieverDetails() {
        recieverUser = (User) getIntent().getSerializableExtra(Constants.USER_KEY);
        binding.textName.setText(recieverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}