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

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private ChipGroup chipGroupGames;
    private Button btnLogout;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvPhone = view.findViewById(R.id.tvProfilePhone);
        chipGroupGames = view.findViewById(R.id.chipGroupGames);
        btnLogout = view.findViewById(R.id.btnLogout);

        if (currentUser != null) {
            loadUserProfile(currentUser.getUid());
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadUserProfile(String userId) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvName.setText(user.getFullname());
                    tvEmail.setText(user.getEmail());
                    tvPhone.setText(user.getPhone());

                    List<String> games = user.getFavoriteGames();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}