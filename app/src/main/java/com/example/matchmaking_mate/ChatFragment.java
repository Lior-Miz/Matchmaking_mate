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
/*this fragments takes care of full chat screen: Shows messages on recycle view
and reads messages from firebase in real time*/
public class ChatFragment extends Fragment {

    private RecyclerView recyclerView; //ui
    private EditText etMessage;
    private Button btnSend, btnBack;
    private TextView tvUserName;

    private ChatAdapter chatAdapter; //adapter for messages
    private List<Message> messageList;

    private FirebaseUser user; //current logged in user
    private DatabaseReference dbRef;

    private String currentUserID; //current logged in user
    private String targetUserID; //friend im talking to

    public ChatFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false); //turn xml to java objects

        recyclerView = view.findViewById(R.id.recyclerChat);
        etMessage = view.findViewById(R.id.etMessageInput);
        btnSend = view.findViewById(R.id.btnSendMessage);
        btnBack = view.findViewById(R.id.btnBackFromChat);
        tvUserName = view.findViewById(R.id.tvChatUserName);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true); //like whatsapp
        recyclerView.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser(); //current logged in user
        if (user != null) {
            currentUserID = user.getUid();
        }

        if (getArguments() != null) {
            targetUserID = getArguments().getString("targetId");
            String targetName = getArguments().getString("targetName");

            if (targetName != null) { //display the name of friend im talking to
                tvUserName.setText(targetName);
            }
        }

        btnBack.setOnClickListener(new View.OnClickListener() { //back button
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() { //send button
            @Override
            public void onClick(View v) {
                String msg = etMessage.getText().toString();

                if (currentUserID == null || targetUserID == null) { //double check if user exists in firebase
                    Toast.makeText(getContext(), "Error: User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!TextUtils.isEmpty(msg)) { //no empty message
                    sendMessage(currentUserID, targetUserID, msg);
                } else {
                    Toast.makeText(getContext(), "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                etMessage.setText(""); //empty text field
            }
        });

        if (currentUserID != null && targetUserID != null) {
            readMessages(currentUserID, targetUserID);
        }

        return view;
    }

    private void sendMessage(String senderID, String receiverID, String message) {
        /*send messages to firebase and store messages twice, for reciever to sender + sender to reciever*/
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderID", senderID);
        hashMap.put("receiverID", receiverID);
        hashMap.put("message", message);
        hashMap.put("timestamp", System.currentTimeMillis());

        reference.child("Chats").child(senderID).child(receiverID).push().setValue(hashMap); //save under sender chat
        reference.child("Chats").child(receiverID).child(senderID).push().setValue(hashMap); //save under reciever chat
    }

    private void readMessages(final String myID, final String userID) {
        /*when data for messages changes in firebase, clears local list, rebuilt message list and update recycleview*/
        dbRef = FirebaseDatabase.getInstance().getReference("Chats").child(myID).child(userID);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //when firebase gets changed update
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //message loop in firebase
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) { //auto scroll to new message
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { //if firebase fails
            }
        });
    }
}