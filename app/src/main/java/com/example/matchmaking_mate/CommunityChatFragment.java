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

public class CommunityChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend, btnBack;
    private TextView tvUserName;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private FirebaseUser user;
    private DatabaseReference dbRef;

    private String currentUserID;
    private String communityID; //this is the "COM..." id

    public CommunityChatFragment() {

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
            communityID = getArguments().getString("targetId");
            String communityName = getArguments().getString("targetName");

            if (communityName != null) {
                tvUserName.setText(communityName);
            }
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Inbox())
                        .commit();
            }
        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String msg = etMessage.getText().toString();

                if (currentUserID == null || communityID == null) {
                    Toast.makeText(getContext(), "Error: Connection failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(msg)) {
                    sendCommunityMessage(currentUserID, communityID, msg);
                } else {
                    Toast.makeText(getContext(), "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                etMessage.setText("");
            }
        });

        if (communityID != null) {
            readCommunityMessages(communityID);
        }

        return view;
    }

    private void sendCommunityMessage(String senderID, String groupID, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(senderID);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String senderName = snapshot.child("fullname").getValue(String.class);
                    String message_with_sender = senderName + ": \n" + message;
                    List<Message> messageList = new ArrayList<>();
                    Message msg = new Message(senderID, groupID, message_with_sender, System.currentTimeMillis());
                    messageList.add(msg);


                    reference.child("Groups").child(groupID).push().setValue(messageList); //save message only once in the shared group path
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readCommunityMessages(final String groupID) {
        dbRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot iDsnapshot : dataSnapshot.getChildren()) {
                    for(DataSnapshot message_snapshot : iDsnapshot.getChildren()) {
                        Message message = message_snapshot.getValue(Message.class);
                        if (message != null && !messageList.contains(message)) {
                            messageList.add(message);
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}