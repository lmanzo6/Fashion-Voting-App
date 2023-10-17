package com.example.outfitrateripro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText editTextEmail, editTextPassword, editTextFullName;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                // if the user null, they will stay on the page and choose to register or not
                if (firebaseUser != null){
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        // getting ids
        btnRegister = findViewById(R.id.btnRegister);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroup = findViewById(R.id.radioGroup);
        editTextFullName = findViewById(R.id.editTextFullName);

        btnRegister.setOnClickListener(view -> {
            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            final RadioButton radioButton = findViewById(selectedRadioButtonId);
            // check if user has selected something
            if (radioButton.getText() == null){
                return;
            }
            // parsing user information
            final String userEmail = editTextEmail.getText().toString();
            final String userPassword = editTextPassword.getText().toString();
            final String userFullName = editTextFullName.getText().toString();

            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                     // check if user creation is successful
                    if (!task.isSuccessful()){
                        Toast.makeText(RegistrationActivity.this, "Sign-up error", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").
                                                          child(radioButton.getText().toString()).child(userId).child("name");
                        currentUserDb.setValue(userFullName);


                    }
                }
            });
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}