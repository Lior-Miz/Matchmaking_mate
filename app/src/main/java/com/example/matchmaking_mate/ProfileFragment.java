package com.example.matchmaking_mate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
/*display selected logged in user profile, shows email, phone, games and friends*/
public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private ChipGroup chipGroupGames, chipGroupFriends;
    private Button btnLogout;

    private Button btnBackProf;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    /*get current user and shows their data*/
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false); //xml to java objects

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser(); //get current user


        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvPhone = view.findViewById(R.id.tvProfilePhone);
        chipGroupGames = view.findViewById(R.id.chipGroupGames);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnBackProf = view.findViewById(R.id.btnBackProf);
        chipGroupFriends = view.findViewById(R.id.chipGroupFriends);


        if (currentUser != null) {  //prevents crash
            loadUserProfile(currentUser.getUid());
        }

        btnBackProf.setOnClickListener(new View.OnClickListener() { //back button
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).popBackStack();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() { //allows loguout
            @Override
            public void onClick(View v) {
                auth.signOut();
                androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true) // Clears the history so they can't press 'Back' to get in
                        .build();

                // 2. Navigate directly to the login screen
                androidx.navigation.Navigation.findNavController(v)
                        .navigate(R.id.loginFragment, null, navOptions);
            }
        });

        return view;
    }

    /*retrieve user data from firebase*/
    private void loadUserProfile(String userId) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId); //get user data from firebase
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvName.setText(user.getFullname());
                    tvEmail.setText(user.getEmail());
                    tvPhone.setText(user.getPhone());

                    List<String> games = user.getFavoriteGames(); //shows favorite games
                    chipGroupGames.removeAllViews();

                    if (games != null && !games.isEmpty()) {
                        for (String game : games) {
                            Chip chip = new Chip(getContext());
                            chip.setText(game);
                            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                            chip.setTextColor(Color.BLACK);
                            chip.setClickable(false);
                            chip.setCheckable(false);
                            chipGroupGames.addView(chip);
                        }
                    } else {
                        Chip noGamesChip = new Chip(getContext());
                        noGamesChip.setText("No games selected");
                        chipGroupGames.addView(noGamesChip);
                    }
                }
                chipGroupFriends.removeAllViews();
                List<String> friendsIds = user.getFriends();

                if (friendsIds != null && !friendsIds.isEmpty()) {  //shows friends
                    for (String friendId : friendsIds) {
                        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId); //if friend exists, show their name and create chip
                        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String friendName = snapshot.child("fullname").getValue(String.class);

                                if (friendName != null) {

                                    Chip chip = new Chip(getContext());  //nested firebase read data from firebase, retrieve friend name and create chip
                                    chip.setText(friendName);
                                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                                    chip.setTextColor(Color.BLACK);
                                    chip.setClickable(false);
                                    chip.setCheckable(false);
                                    chipGroupFriends.addView(chip);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                } else {
                    Chip noFriendsChip = new Chip(getContext());
                    noFriendsChip.setText("No friends yet");
                    chipGroupFriends.addView(noFriendsChip);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {  //if firebase fails shows error
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}