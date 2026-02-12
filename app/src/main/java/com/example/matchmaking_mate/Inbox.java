package com.example.matchmaking_mate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Inbox extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private EditText etSearch;
    private Button btnSearch, btnReset, btnBack;
    private List<User> displayList;
    private List<User> fullList;
    private DatabaseReference dbRef;

    private String currentSearchQuery = "";
    private String currentGameFilter = "All Chats";

    public Inbox(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewInbox);
        etSearch = view.findViewById(R.id.etSearchUserEditInbox);
        btnSearch = view.findViewById(R.id.btnSearchInbox);
        btnReset = view.findViewById(R.id.btnResetInbox);
        btnBack = view.findViewById(R.id.btnBackToHomeFromInbox);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        displayList = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new UserAdapter(getContext(), displayList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                openChat(user);
            }
        });
        recyclerView.setAdapter(adapter);

        loadUsers();



        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSearchQuery = etSearch.getText().toString();
                applyFilters();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                currentSearchQuery = "";
                currentGameFilter = "All Chats";
                applyFilters();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        return view;
    }

    private void loadUsers() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("Chats");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Message msg = data.getValue(Message.class);
                    android.util.Log.d("Inbox", "Message: " + msg);
                    String chatWith;

                    if (msg != null && msg.getReceiverID() != null && msg.getSenderID() != null) {
                        if (msg.getSenderID().equals(myId))
                            chatWith = msg.getSenderID();
                        else if(msg.getReceiverID().equals(myId))
                            chatWith = msg.getReceiverID();
                        else
                            continue;

                        convertToUser(chatWith);

                    }
                    applyFilters();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertToUser(String userID) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fullList.add(user);
                    applyFilters();
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                }

    });

    }

    private void applyFilters() {
        displayList.clear();
        List<User> filteredByName = new ArrayList<>();

        if (currentSearchQuery.isEmpty()) {
            filteredByName.addAll(fullList);
        } else {
            String lowerCaseQuery = currentSearchQuery.toLowerCase();
            for (User user : fullList) {
                if (user.getFullname() != null && user.getFullname().toLowerCase().contains(lowerCaseQuery)) {
                    filteredByName.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }



    private void openChat(User user) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("targetId", user.getUserid());
        args.putString("targetName", user.getFullname());
        chatFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }


}
