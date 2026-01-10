package com.example.myproject2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerSupportAdapter extends RecyclerView.Adapter<CustomerSupportAdapter.MessageViewHolder> {

    private final List<ChatMessage> chatList;
    private final String currentUserId;

    public CustomerSupportAdapter(List<ChatMessage> chatList, String currentUserId) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // By REMOVING the 'static' keyword, this inner class now has access to the
    // 'currentUserId' field of the outer CustomerSupportAdapter instance.
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView leftChat, rightChat;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChat = itemView.findViewById(R.id.tvLeftChat);
            rightChat = itemView.findViewById(R.id.tvRightChat);
        }

        void bind(ChatMessage message) {
            // The 'currentUserId' variable is now accessible from the outer class.
            if (currentUserId != null && currentUserId.equals(message.getSenderId())) {
                // User's message: show on the right
                leftChat.setVisibility(View.GONE);
                rightChat.setVisibility(View.VISIBLE);
                rightChat.setText(message.getMessage());
            } else {
                // Support agent's message: show on the left
                rightChat.setVisibility(View.GONE);
                leftChat.setVisibility(View.VISIBLE);
                leftChat.setText(message.getMessage());
            }
        }
    }
}
