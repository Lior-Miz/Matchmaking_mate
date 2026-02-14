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

/*this adapter connects the list of messages to the recycler view and shows them on screen*/
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messageList; //message list
    private String currentUserId; //id of currently logged in user

    public ChatAdapter(List<Message> messageList) { //constructor
        this.messageList = messageList;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            /*get the id of the currently logged in user and store it in the currentUserId variable for later use*/
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //creates a layout for one message row when recycle view needs a new item view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false); //turn xml to real java objects
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = formatTime(message.getTimestamp()); //HH:mm

        if (message.getSenderID() != null && message.getSenderID().equals(currentUserId)) { //my message
            holder.layoutRight.setVisibility(View.VISIBLE);     //show mine and hide theirs
            holder.layoutLeft.setVisibility(View.GONE);         //show right messages and hide left messages
            holder.tvMsgRight.setText(message.getMessage());
            holder.tvTimeRight.setText(time);
        } else {  // their message
            holder.layoutLeft.setVisibility(View.VISIBLE); //hide mine and show theirs
            holder.layoutRight.setVisibility(View.GONE);   //hide right messages and show left messages
            holder.tvMsgLeft.setText(message.getMessage());
            holder.tvTimeLeft.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size(); //how many messages we need to add to recycle view (count)
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutLeft, layoutRight;
        TextView tvMsgLeft, tvMsgRight;
        TextView tvTimeLeft, tvTimeRight;

        public ChatViewHolder(@NonNull View itemView) { //text views for messages and times
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