package com.example.matchmaking_mate;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView tvEmail;
    private Button btnLogout, btnProfile, btnMatches,btnInbox;
    private FirebaseAuth auth;

    public HomeFragment() {
    }

    @Override
    /* convert xml to java objects and firebase user is retrieved */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false); //inflate layout

        auth = FirebaseAuth.getInstance();

        tvEmail = view.findViewById(R.id.Email);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnProfile = view.findViewById(R.id.btnMoveToProfile);
        btnMatches = view.findViewById(R.id.btnFindMatches);
        btnInbox = view.findViewById(R.id.btn_inbox);


        FirebaseUser user = auth.getCurrentUser(); //get current user thats logged in
        if (user != null) { //display email if user exists
            tvEmail.setText("Hello,\n" + user.getEmail());
        }

        btnProfile.setOnClickListener(new View.OnClickListener() { //profile screen button
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null) //allows back button
                        .commit();
            }
        });

        btnMatches.setOnClickListener(new View.OnClickListener() { //go to matches screen
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MatchesFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        btnInbox.setOnClickListener(new View.OnClickListener() { //inbox button
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Inbox())
                        .addToBackStack(null)
                        .commit();
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() { //logout button
            @Override
            public void onClick(View v) {
                auth.signOut(); //singout from firebase
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                if (getActivity() != null) { //go back to login screen
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment())
                            .commit();
                }
            }
        });

        return view;
    }
}