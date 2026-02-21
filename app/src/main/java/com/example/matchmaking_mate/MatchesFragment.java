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
import androidx.navigation.Navigation;
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
/* displays all users except the current user, allows searching by name and game and navigate to selected user's profile */

public class MatchesFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private EditText etSearch;
    private Button btnSearch, btnReset, btnBack;
    private Spinner gameSpinner;
    private List<User> displayList;
    private List<User> fullList;
    private DatabaseReference dbRef;

    private String currentSearchQuery = ""; //current active filters (will be changed when user changes them)
    private String currentGameFilter = "All Games";

    public MatchesFragment() {
    }

    @Nullable
    @Override
    /*setup up xml, recycle view, firebase, buttons and spinner*/
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMatches);
        etSearch = view.findViewById(R.id.etsearchuseredit);
        btnSearch = view.findViewById(R.id.btnsearch);
        btnReset = view.findViewById(R.id.btnreset);
        btnBack = view.findViewById(R.id.btnBackToHome);
        gameSpinner = view.findViewById(R.id.spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        displayList = new ArrayList<>(); //initialize lists
        fullList = new ArrayList<>();

        adapter = new UserAdapter(getContext(), displayList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                moveToUserProfile(user);
            }
        });
        recyclerView.setAdapter(adapter);

        loadUsers(); //load users from firebase

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),  //spinner setup and loads game list
                R.array.games_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameSpinner.setAdapter(spinnerAdapter);

        gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //when user changes game, filter the search to it
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // ? means that it can be any type (including null)
                currentGameFilter = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() { //search button
            @Override
            public void onClick(View v) {
                currentSearchQuery = etSearch.getText().toString();
                applyFilters();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() { //reset button that clears filters
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                currentSearchQuery = "";
                gameSpinner.setSelection(0);
                currentGameFilter = "All Games";
                applyFilters();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() { //back button
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).popBackStack();
            }
        });

        return view;
    }

    private void loadUsers() { //load users from firebase (except current user), store them in a list then apply filter when needed
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) { //loop through users(children) in firebase
                    User user = data.getValue(User.class);

                    if (user != null && data.getKey() != null && !data.getKey().equals(myId)) { //skip current user
                        user.setUserid(data.getKey());
                        fullList.add(user);
                    }
                }
                applyFilters(); //apply filters
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {  //filter application
        displayList.clear();
        List<User> filteredByName = new ArrayList<>();

        if (currentSearchQuery.isEmpty()) { // filter by name
            filteredByName.addAll(fullList);
        } else {
            String lowerCaseQuery = currentSearchQuery.toLowerCase();
            for (User user : fullList) {
                if (user.getFullname() != null && user.getFullname().toLowerCase().contains(lowerCaseQuery)) {
                    filteredByName.add(user);
                }
            }
        }

        if (currentGameFilter.equals("All Games")) { //filter by selected game from spinner
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


    private void moveToUserProfile(User user) { //navigation to other user profile, passes the data using bundle for future use
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

        Navigation.findNavController(requireView())
                .navigate(R.id.action_matchesFragment_to_otherUserProfileFragment, bundle);
    }


}