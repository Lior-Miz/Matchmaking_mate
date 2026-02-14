package com.example.matchmaking_mate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*initizialized firebase authentication, check if user is logged in or not and go to appropriate screen*/
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    /*firebase initialization and check if user is logged in or not*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); //firebase connection

        if (savedInstanceState == null) { //if there was a log in before simply use it from android previous state
            FirebaseUser user = mAuth.getCurrentUser(); //check if a user is already logged in or not

            if (user != null) {//user session exists then just go directly to home screen

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            } else {      //if no user logged in then go to register screen
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit();
            }
        }
    }
}