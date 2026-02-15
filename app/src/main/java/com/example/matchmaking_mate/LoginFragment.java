package com.example.matchmaking_mate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/* this fragment takes care of login screen and handles user authentication */
public class LoginFragment extends Fragment {

    private EditText emailInput, passwordInput;
    private Button btnLogin, btn_lang;
    private TextView linkRegister;
    private FirebaseAuth auth;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false); //turn xml to java objects

        auth = FirebaseAuth.getInstance();

        emailInput = view.findViewById(R.id.Email);
        passwordInput = view.findViewById(R.id.Password);
        btnLogin = view.findViewById(R.id.btnLogin);
        linkRegister = view.findViewById(R.id.tvGotoRegister);
        btn_lang=view.findViewById(R.id.btn_lang);


        linkRegister.setOnClickListener(new View.OnClickListener() { //register screen
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit();
            }
        });



        btnLogin.setOnClickListener(new View.OnClickListener() { //login button, collect info and validate and calls login function (loginuser)
            @Override
            public void onClick(View v) {
                String txtEmail = emailInput.getText().toString().trim();
                String txtPassword = passwordInput.getText().toString().trim();

                if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(txtEmail, txtPassword);
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

    private void loginUser(String email, String password) { // authenticate email and password with firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {    //if successful log in and go to home screen
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            if (getActivity() != null) {
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new HomeFragment())
                                        .commit();
                            }
                        } else {  // if it fails display error message
                            String msg = "Authentication failed";
                            if (task.getException() != null) {
                                msg = task.getException().getMessage();
                            }
                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}