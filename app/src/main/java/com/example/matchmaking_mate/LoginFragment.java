package com.example.matchmaking_mate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText emailInput, passwordInput;
    private Button btnLogin;
    private TextView linkRegister;
    private FirebaseAuth auth;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();

        emailInput = view.findViewById(R.id.Email);
        passwordInput = view.findViewById(R.id.Password);
        btnLogin = view.findViewById(R.id.btnLogin);
        linkRegister = view.findViewById(R.id.tvGotoRegister);

        linkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            if (getActivity() != null) {
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new HomeFragment())
                                        .commit();
                            }
                        } else {
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