package com.example.matchmaking_mate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private EditText etSearch;
    private Button btnSearch, btnReset, btnBack;
    private List<User> displayList;
    private List<User> fullList;
    private DatabaseReference dbRef;

    public MatchesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMatches);
        etSearch = view.findViewById(R.id.etsearchuseredit);
        btnSearch = view.findViewById(R.id.btnsearch);
        btnReset = view.findViewById(R.id.btnreset);
        btnBack = view.findViewById(R.id.btnBackToHome);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        displayList = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new UserAdapter(getContext(), displayList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                moveToUserProfile(user);
            }
        });
        recyclerView.setAdapter(adapter);

        loadUsers();



        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = etSearch.getText().toString();
                filterUsers(query);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                filterUsers("");
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
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                displayList.clear();
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);

                    if (user != null && data.getKey() != null && !data.getKey().equals(myId)) {
                        user.setUserid(data.getKey());
                        displayList.add(user);
                        fullList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String text) {
        displayList.clear();
        if (text.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (User user : fullList) {
                if (user.getFullname() != null && user.getFullname().toLowerCase().contains(text)) {
                    displayList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void moveToUserProfile(User user) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserid());
        bundle.putString("userName", user.getFullname());
        bundle.putString("userPhone", user.getPhone());
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}