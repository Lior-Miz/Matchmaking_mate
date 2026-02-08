package com.example.matchmaking_mate;

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

public class OtherUserProfileFragment extends Fragment {

    private TextView tvName, tvPhone;
    private Button btnChat, btnBack;

    private String userId;
    private String userName;
    private String userPhone;

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

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            userName = getArguments().getString("userName");
            userPhone = getArguments().getString("userPhone");

            if (userName != null) {
                tvName.setText(userName);
            }
            if (userPhone != null) {
                tvPhone.setText(userPhone);
            }
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

        return view;
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