package com.example.myproject2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView leftChat, rightChat;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChat = itemView.findViewById(R.id.tvLeftChat);
            rightChat = itemView.findViewById(R.id.tvRightChat);
        }

        void bind(Message message) {
            if (message.getSentBy().equals(Message.SENT_BY_ME)) {
                leftChat.setVisibility(View.GONE);
                rightChat.setVisibility(View.VISIBLE);
                rightChat.setText(message.getMessage());
            } else {
                rightChat.setVisibility(View.GONE);
                leftChat.setVisibility(View.VISIBLE);
                leftChat.setText(message.getMessage());
            }
        }
    }
}
