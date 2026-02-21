package com.example.matchmaking_mate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView tvEmail;
    private Button btnLogout, btnProfile, btnMatches,btnInbox, btn_lang;
    private FirebaseAuth auth;
    private SwitchMaterial languageSwitch;



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
        btn_lang=view.findViewById(R.id.btn_lang);


        FirebaseUser user = auth.getCurrentUser(); //get current user thats logged in
        if (user != null) { //display email if user exists
            tvEmail.setText(user.getEmail());
        }

        btnProfile.setOnClickListener(new View.OnClickListener() { //profile screen button
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_profileFragment);
            }
        });

        btnMatches.setOnClickListener(new View.OnClickListener() { //go to matches screen
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_matchesFragment);
            }
        });
        btnInbox.setOnClickListener(new View.OnClickListener() { //inbox button
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_inbox);
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() { //logout button
            @Override
            public void onClick(View view) {
                auth.signOut(); //singout from firebase
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                if (getActivity() != null) { //go back to login screen
                    Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_loginFragment);
                }
            }
        });

        btn_lang.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                String current_Lang="en";
                current_Lang=AppCompatDelegate.getApplicationLocales().get(0).getLanguage();

                if(current_Lang.equals("en")){
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("iw"));
                }
                else{
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
                }
           }
        });

        return view;
    }
}