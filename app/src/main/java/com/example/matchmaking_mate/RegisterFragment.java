package com.example.matchmaking_mate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etPhone;
    private Button btnRegister;
    private CheckBox cbFifa, cbFortnite, cbCod, cbMinecraft, cbGta, cbNba;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    public RegisterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        etName = view.findViewById(R.id.etRegisterName);
        etEmail = view.findViewById(R.id.etRegisterEmail);
        etPassword = view.findViewById(R.id.etRegisterPassword);
        etPhone = view.findViewById(R.id.etRegisterPhone);
        btnRegister = view.findViewById(R.id.btnRegister);

        cbFifa = view.findViewById(R.id.cbFifa);
        cbFortnite = view.findViewById(R.id.cbFortnite);
        cbCod = view.findViewById(R.id.cbCod);
        cbMinecraft = view.findViewById(R.id.cbMinecraft);
        cbGta = view.findViewById(R.id.cbGta);
        cbNba = view.findViewById(R.id.cbNba);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return view;
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 chars", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                saveUserToDatabase(userId, name, email, phone);
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                            Toast.makeText(getContext(), "Registration Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email, String phone) {
        List<String> selectedGames = new ArrayList<>();

        if (cbFifa.isChecked()) selectedGames.add("FIFA");
        if (cbFortnite.isChecked()) selectedGames.add("Fortnite");
        if (cbCod.isChecked()) selectedGames.add("Call of Duty");
        if (cbMinecraft.isChecked()) selectedGames.add("Minecraft");
        if (cbGta.isChecked()) selectedGames.add("GTA V");
        if (cbNba.isChecked()) selectedGames.add("NBA 2K");

        User newUser = new User(name, email, phone, userId);
        newUser.setFavoriteGames(selectedGames);

        dbRef.child("Users").child(userId).setValue(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new HomeFragment())
                                    .commit();
                        } else {
                            Toast.makeText(getContext(), "Database Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}