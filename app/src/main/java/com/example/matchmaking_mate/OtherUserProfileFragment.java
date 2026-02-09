package com.example.matchmaking_mate;

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
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class OtherUserProfileFragment extends Fragment {

    private TextView tvName, tvPhone;
    private Button btnChat, btnBack, btnAddFriend;

    private String userId;
    private String userName;
    private ChipGroup OtherchipGroupGames;

    private ChipGroup ChipSuggestFriend;


    public OtherUserProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);
        view.setBackgroundColor(Color.WHITE);

        tvName = view.findViewById(R.id.tvOtherUserName);
        tvPhone = view.findViewById(R.id.tvOtherUserPhone);
        btnChat = view.findViewById(R.id.btnOpenChat);
        btnBack = view.findViewById(R.id.btnBackToMatches);
        btnAddFriend = view.findViewById(R.id.btnFriend);

        ChipSuggestFriend = view.findViewById(R.id.ChipSuggestFriend);
        OtherchipGroupGames = view.findViewById(R.id.OtherchipGroupGames);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            userName = getArguments().getString("userName");
            String userPhone = getArguments().getString("userPhone");
            ArrayList<String> favoriteGames = getArguments().getStringArrayList("favoriteGames");


            if (userName != null) {
                tvName.setText(userName);
            }
            if (userPhone != null) {
                tvPhone.setText(userPhone);
            }
            displayFavoriteGames(favoriteGames);
            loadSuggestedFriends();

        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId != null && !userId.isEmpty()) {
                    openChat();
                } else {
                    Toast.makeText(getContext(), "Error: User ID missing", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });
        return view;
    }
    private void addFriend() {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User me = snapshot.getValue(User.class);
                if (me != null) {
                    List<String> friends = me.getFriends();
                    if (!friends.contains(userId)) {
                        friends.add(userId);
                        myRef.child("friends").setValue(friends).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Friend added!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to add friend", Toast.LENGTH_SHORT).show();
                            }
                        });
                        } else {
                        Toast.makeText(getContext(), "Already a friend", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to add friend", Toast.LENGTH_SHORT).show();
            }
        });
                            }


    private void displayFavoriteGames(List<String> games) {
        if (OtherchipGroupGames == null || getContext() == null) return;
        OtherchipGroupGames.removeAllViews();

        if (games != null && !games.isEmpty()) {
            for (String game : games) {
                Chip chip = new Chip(getContext());
                chip.setText(game);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                chip.setTextColor(Color.BLACK);
                chip.setClickable(false);
                chip.setCheckable(false);
                OtherchipGroupGames.addView(chip);
            }
        } else {
            Chip noGamesChip = new Chip(getContext());
            noGamesChip.setText("No games selected");
            OtherchipGroupGames.addView(noGamesChip);
        }
    }

    private void displaySuggestedFriend(List<String> suggested_friends) {
        if (ChipSuggestFriend == null || getContext() == null) return;
        ChipSuggestFriend.removeAllViews();

        if (suggested_friends != null && !suggested_friends.isEmpty()) {
            for (String friend : suggested_friends) {
                Chip chip = new Chip(getContext());
                chip.setText(friend);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                chip.setTextColor(Color.BLACK);
                chip.setClickable(false);
                chip.setCheckable(false);
                ChipSuggestFriend.addView(chip);
            }
        } else {
            Chip noFriendsChip = new Chip(getContext());
            noFriendsChip.setText("No friends to suggest");
            ChipSuggestFriend.addView(noFriendsChip);
        }
    }

    private void loadSuggestedFriends() {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference FriendRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);

        FriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User friend = snapshot.getValue(User.class);
                if (friend != null) {
                    List<String> friends_ofa_friend = friend.getFriends();
                    List<String> suggested_friends = new ArrayList<>();

                    for (String friendId : friends_ofa_friend) {
                        DatabaseReference FriendOfFriendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
                        FriendOfFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User friendOfFriend = snapshot.getValue(User.class);
                                if (friendOfFriend != null) {
                                    List<String> friends_of_friend_games = friendOfFriend.getFavoriteGames();

                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            User me = snapshot.getValue(User.class);
                                            if (me != null) {
                                                List<String> myGames = me.getFavoriteGames();
                                                List<String> myFriends = me.getFriends();

                                                if (!myFriends.contains(friendId) && !friendId.equals(me.getUserid())) {
                                                    for (String game : myGames) {
                                                        if (friends_of_friend_games.contains(game)) {
                                                            suggested_friends.add(friendOfFriend.getFullname());
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            displaySuggestedFriend(suggested_friends);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void openChat() {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("targetId", userId);
        args.putString("targetName", userName);
        chatFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}


