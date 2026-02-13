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

    private List<String> OtherprofileFriends;

    public OtherUserProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);


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
            OtherprofileFriends = getArguments().getStringArrayList("friends");

            if (userName != null) {
                tvName.setText(userName);
            }
            if (userPhone != null) {
                tvPhone.setText(userPhone);
            }
            displayFavoriteGames(favoriteGames);
            loadSuggestedFriends();

            String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User me = snapshot.getValue(User.class);
                    if (me != null) {
                        if (me.getFriends().contains(userId)) {
                            btnAddFriend.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
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
                btnAddFriend.setVisibility(View.GONE);
                //loadSuggestedFriends();
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
                    if (friends == null) friends = new ArrayList<>();

                    if (!friends.contains(userId)) {
                        friends.add(userId);
                        myRef.child("friends").setValue(friends).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Friend added!", Toast.LENGTH_SHORT).show();
                                loadSuggestedFriends();
                            } else {
                                Toast.makeText(getContext(), "Failed to add friend", Toast.LENGTH_SHORT).show();
                                btnAddFriend.setVisibility(View.VISIBLE);
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

    private void loadSuggestedFriends() {
        if (ChipSuggestFriend != null) ChipSuggestFriend.removeAllViews();

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(myId);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot mySnapshot) {
                User me = mySnapshot.getValue(User.class);
                if (me == null) return;

                if (me.getFriends() == null || !me.getFriends().contains(userId)) {
                    displayPrivacyMessage();
                    return;
                }

                DatabaseReference FriendRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                FriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User friend = snapshot.getValue(User.class);
                        if (friend != null && friend.getFriends() != null) {
                            List<String> friends_ofa_friend = friend.getFriends();
                            List<String> candidates = new ArrayList<>();

                            for (String friendId : friends_ofa_friend) {
                                if (friendId.equals(me.getUserid())) continue;
                                if (me.getFriends() != null && me.getFriends().contains(friendId)) continue;
                                if (candidates.contains(friendId)) continue;
                                candidates.add(friendId);
                            }

                            if (candidates.isEmpty()) {
                                displayNoFriendsMessage();
                                return;
                            }

                            final int totalFofCount = candidates.size();
                            final int[] currentFofCount = {0};
                            final boolean[] foundMatch = {false};

                            for (String friendId : candidates) {
                                DatabaseReference FriendOfFriendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
                                FriendOfFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        currentFofCount[0]++;
                                        User friendOfFriend = snapshot.getValue(User.class);

                                        if (friendOfFriend != null) {
                                            List<String> fofGames = friendOfFriend.getFavoriteGames();
                                            List<String> myGames = me.getFavoriteGames();

                                            if (fofGames != null && myGames != null) {
                                                for (String game : myGames) {
                                                    if (fofGames.contains(game)) {
                                                        addSingleSuggestedFriendChip(friendId);
                                                        foundMatch[0] = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if (currentFofCount[0] == totalFofCount && !foundMatch[0]) {
                                            displayNoFriendsMessage();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        currentFofCount[0]++;
                                        if (currentFofCount[0] == totalFofCount && !foundMatch[0]) {
                                            displayNoFriendsMessage();
                                        }
                                    }
                                });
                            }
                        } else {
                            displayNoFriendsMessage();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addSingleSuggestedFriendChip(String friendId) {
        if (ChipSuggestFriend == null || getContext() == null) return;
        if (ChipSuggestFriend.findViewWithTag(friendId) != null) return;

        DatabaseReference FriendRef = FirebaseDatabase.getInstance().getReference("Users").child(friendId);
        FriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                User friendUser = friendSnapshot.getValue(User.class);
                if (friendUser != null) {
                    if (ChipSuggestFriend.findViewWithTag(friendUser.getUserid()) != null) return;

                    Chip chip = new Chip(getContext());
                    chip.setText(friendUser.getFullname());
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
                    chip.setTextColor(Color.BLACK);
                    chip.setClickable(true);
                    chip.setCheckable(false);
                    chip.setTag(friendUser.getUserid());

                    chip.setOnClickListener(v -> {
                        moveToOtherID(friendUser);
                        Toast.makeText(getContext(), friendUser.getFullname() + " was clicked", Toast.LENGTH_SHORT).show();
                    });
                    ChipSuggestFriend.addView(chip);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayPrivacyMessage() {
        if (ChipSuggestFriend == null || getContext() == null) return;
        ChipSuggestFriend.removeAllViews();
        Chip chip = new Chip(getContext());
        chip.setText("Must add as a friend to view suggested friends");
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
        chip.setTextColor(Color.BLACK);
        chip.setClickable(false);
        chip.setCheckable(false);
        ChipSuggestFriend.addView(chip);
    }

    private void displayNoFriendsMessage() {
        if (ChipSuggestFriend == null || getContext() == null) return;
        if (ChipSuggestFriend.getChildCount() > 0) return;

        Chip noFriendsChip = new Chip(getContext());
        noFriendsChip.setText("No other friends to suggest");
        noFriendsChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));
        noFriendsChip.setTextColor(Color.BLACK);
        noFriendsChip.setClickable(false);
        noFriendsChip.setCheckable(false);
        ChipSuggestFriend.addView(noFriendsChip);
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

    private void moveToOtherID(User user) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserid());
        bundle.putString("userName", user.getFullname());
        bundle.putString("userPhone", user.getPhone());
        if (user.getFavoriteGames() != null) {
            bundle.putStringArrayList("favoriteGames", new ArrayList<>(user.getFavoriteGames()));
        }
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}


