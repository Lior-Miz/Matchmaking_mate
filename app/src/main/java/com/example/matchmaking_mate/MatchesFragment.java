package com.example.matchmaking_mate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    private Spinner gameSpinner;
    private List<User> displayList;
    private List<User> fullList;
    private DatabaseReference dbRef;

    private String currentSearchQuery = "";
    private String currentGameFilter = "All Games";

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
        gameSpinner = view.findViewById(R.id.spinner);

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

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.games_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameSpinner.setAdapter(spinnerAdapter);

        gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentGameFilter = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
                gameSpinner.setSelection(0);
                currentGameFilter = "All Games";
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
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);

                    if (user != null && data.getKey() != null && !data.getKey().equals(myId)) {
                        user.setUserid(data.getKey());
                        fullList.add(user);
                    }
                }
                applyFilters();
                adapter.notifyDataSetChanged();
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

        if (currentGameFilter.equals("All Games")) {
            displayList.addAll(filteredByName);
        } else {
            for (User user : filteredByName) {
                if (user.getFavoriteGames() != null && user.getFavoriteGames().contains(currentGameFilter)) {
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
        if (user.getFavoriteGames() != null) {
            bundle.putStringArrayList("favoriteGames", new ArrayList<>(user.getFavoriteGames()));
        }
        if (user.getFavoriteGames() != null) {
            bundle.putStringArrayList("friends", new ArrayList<>(user.getFriends()));
        }
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


}
