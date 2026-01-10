package com.example.myproject2;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myproject2.databinding.ActivityChatBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;

    // API Configuration - **IMPORTANT: DO NOT SHIP WITH HARDCODED KEYS**

    // private static final String API_KEY = "My api key";

    private static final String API_KEY = "AIzaSyD3qvGO6CHMeoN8PQv3vkq4rbLbZaCmoEc";

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;


    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- 1. Toolbar Setup ---
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- 2. Setup Recycler View ---
        messageList = new ArrayList<>();
        messageList.add(new Message("Hello! I am your JazzCash AI Assistant. How can I help you today?", Message.SENT_BY_BOT));

        chatAdapter = new ChatAdapter(messageList);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true); // Auto-scroll to bottom
        binding.chatRecyclerView.setLayoutManager(llm);

        // --- 3. Handle Send Click ---
        binding.sendButton.setOnClickListener(v -> {
            String question = binding.messageEditText.getText().toString().trim();
            if (question.isEmpty()) return;

            addToChat(question, Message.SENT_BY_ME);
            binding.messageEditText.setText("");
            setSendingInProgress(true); // Disable input while processing
            callGeminiAPI(question);
        });
    }

    // --- Handle Toolbar Back Button ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            chatAdapter.notifyDataSetChanged();
            binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
        });
    }

    // --- Toggles UI during API call ---
    void setSendingInProgress(boolean inProgress) {
        runOnUiThread(() -> {
            binding.sendButton.setEnabled(!inProgress);
            binding.messageEditText.setEnabled(!inProgress);
        });
    }

    void callGeminiAPI(String question) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", question);
            partsArray.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(content);

            jsonBody.put("contents", contentsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                addToChat("Failed to connect. Please check internet.", Message.SENT_BY_BOT);
                setSendingInProgress(false); // Re-enable input on failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String aiText = parts.getJSONObject(0).getString("text");

                        addToChat(aiText, Message.SENT_BY_BOT);

                    } catch (JSONException e) {
                        addToChat("Error parsing AI response.", Message.SENT_BY_BOT);
                        e.printStackTrace();
                    }
                } else {
                    addToChat("AI Error: " + response.code() + " " + response.message(), Message.SENT_BY_BOT);
                }
                setSendingInProgress(false); // Re-enable input after response
            }
        });
    }
}
