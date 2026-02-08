package com.example.matchmaking_mate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = formatTime(message.getTimestamp());

        if (message.getSenderID() != null && message.getSenderID().equals(currentUserId)) {
            holder.layoutRight.setVisibility(View.VISIBLE);
            holder.layoutLeft.setVisibility(View.GONE);
            holder.tvMsgRight.setText(message.getMessage());
            holder.tvTimeRight.setText(time);
        } else {
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.layoutRight.setVisibility(View.GONE);
            holder.tvMsgLeft.setText(message.getMessage());
            holder.tvTimeLeft.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutLeft, layoutRight;
        TextView tvMsgLeft, tvMsgRight;
        TextView tvTimeLeft, tvTimeRight;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            layoutRight = itemView.findViewById(R.id.layoutRight);
            tvMsgLeft = itemView.findViewById(R.id.tvMessageLeft);
            tvMsgRight = itemView.findViewById(R.id.tvMessageRight);
            tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
            tvTimeRight = itemView.findViewById(R.id.tvTimeRight);
        }
    }
}