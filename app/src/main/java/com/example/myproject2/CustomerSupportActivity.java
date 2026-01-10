package com.example.myproject2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myproject2.databinding.ActivityCustomerSupportBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerSupportActivity extends AppCompatActivity {

    private static final String TAG = "CustomerSupportActivity";
    private static final String SUPPORT_AGENT_ID = "support_agent";

    private ActivityCustomerSupportBinding binding;
    private CustomerSupportAdapter adapter;
    private List<ChatMessage> chatList;

    private FirebaseFirestore db;
    private CollectionReference userChatCollection;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerSupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- 1. Toolbar Setup ---
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- 2. Firebase & User Setup ---
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to use support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        db = FirebaseFirestore.getInstance();
        userChatCollection = db.collection("customer_support_chats")
                               .document(currentUserId)
                               .collection("messages");

        // --- 3. RecyclerView Setup (This is the fix) ---
        chatList = new ArrayList<>();
        // The currentUserId MUST be passed to the adapter
        adapter = new CustomerSupportAdapter(chatList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.supportChatRecyclerView.setLayoutManager(layoutManager);
        binding.supportChatRecyclerView.setAdapter(adapter);

        // --- 4. Listen for new messages ---
        listenForMessages();

        // --- 5. Handle Send Click ---
        binding.sendButton.setOnClickListener(v -> {
            String messageText = binding.messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText, currentUserId);
            }
        });
    }

    private void listenForMessages() {
        userChatCollection.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        chatList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ChatMessage chatMessage = doc.toObject(ChatMessage.class);
                            chatList.add(chatMessage);
                        }
                        adapter.notifyDataSetChanged();
                        binding.supportChatRecyclerView.scrollToPosition(chatList.size() - 1);
                    }
                });
    }

    private void sendMessage(String messageText, String senderId) {
        binding.messageEditText.setText("");

        ChatMessage userMessage = new ChatMessage(messageText, senderId);
        userChatCollection.add(userMessage)
                .addOnSuccessListener(documentReference -> {
                    // Only send auto-reply if the message was from the user
                    if (senderId.equals(currentUserId)) {
                        sendAutoReply();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    binding.messageEditText.setText(messageText); // Restore text
                });
    }

    private void sendAutoReply() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String replyText = "Thank you for your message. A support agent will be with you shortly.";
            ChatMessage autoReply = new ChatMessage(replyText, SUPPORT_AGENT_ID);
            userChatCollection.add(autoReply);
        }, 1000); // 1-second delay
    }

    // --- Handle Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
