package com.example.matchmaking_mate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend, btnBack;
    private TextView tvUserName;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private FirebaseUser user;
    private DatabaseReference dbRef;

    private String currentUserID;
    private String targetUserID;

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerChat);
        etMessage = view.findViewById(R.id.etMessageInput);
        btnSend = view.findViewById(R.id.btnSendMessage);
        btnBack = view.findViewById(R.id.btnBackFromChat);
        tvUserName = view.findViewById(R.id.tvChatUserName);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserID = user.getUid();
        }

        if (getArguments() != null) {
            targetUserID = getArguments().getString("targetId");
            String targetName = getArguments().getString("targetName");

            if (targetName != null) {
                tvUserName.setText(targetName);
            }
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMessage.getText().toString();

                if (currentUserID == null || targetUserID == null) {
                    Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(msg)) {
                    sendMessage(currentUserID, targetUserID, msg);
                } else {
                    Toast.makeText(getContext(), "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                etMessage.setText("");
            }
        });

        if (currentUserID != null && targetUserID != null) {
            readMessages(currentUserID, targetUserID);
        }

        return view;
    }

    private void sendMessage(String senderID, String receiverID, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderID", senderID);
        hashMap.put("receiverID", receiverID);
        hashMap.put("message", message);
        hashMap.put("timestamp", System.currentTimeMillis());

        reference.child("Chats").child(senderID).child(receiverID).push().setValue(hashMap);
        reference.child("Chats").child(receiverID).child(senderID).push().setValue(hashMap);
    }

    private void readMessages(final String myID, final String userID) {
        dbRef = FirebaseDatabase.getInstance().getReference("Chats").child(myID).child(userID);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}