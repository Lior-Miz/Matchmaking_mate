package com.example.matchmaking_mate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

/*initizialized firebase authentication, check if user is logged in or not and go to appropriate screen*/
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    /*firebase initialization and check if user is logged in or not*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); //firebase connection

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView2);

        // 2. Get the NavController
        NavController navController = navHostFragment.getNavController();

        // 3. Inflate your navigation graph manually
        NavInflater inflater = navController.getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.navgraph);

        if (savedInstanceState == null) { //if there was a log in before simply use it from android previous state
            FirebaseUser user = mAuth.getCurrentUser(); //check if a user is already logged in or not

            if (user != null) {//user session exists then just go directly to home screen
                graph.setStartDestination(R.id.homeFragment);

            } else {      //if no user logged in then go to register screen
                graph.setStartDestination(R.id.registerFragment);
            }
            navController.setGraph(graph);
        }
    }
}