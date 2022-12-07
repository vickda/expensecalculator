package com.example.expensecalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    // View Variables
    TextInputEditText fullName, email, pass;
    Button signupBtn;
    TextView goToLoginPage, errorMsg;
    ProgressBar signupProgressBar;

    // Firebase Variables
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initializing all View Variables
        fullName = findViewById(R.id.nameEditTextSignup);
        email = findViewById(R.id.emailEditTextSignup);
        pass = findViewById(R.id.passEditTextSignup);
        errorMsg = findViewById(R.id.errorMsg);

        signupBtn = findViewById(R.id.signupBtn);
        signupBtn.setOnClickListener(this);

        goToLoginPage = findViewById(R.id.alreadyAccountTV);
        goToLoginPage.setOnClickListener(this);

        signupProgressBar = findViewById(R.id.signUpProgressBar);

        // Getting firebase instance
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.alreadyAccountTV:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.signupBtn:
                registerUser();
                break;
        }

    }

    private void registerUser() {
        String name, emailID, password;

        name = fullName.getText().toString().trim();
        emailID = email.getText().toString().trim();
        password = pass.getText().toString().trim();

        // Check if name field is empty
        if(name.isEmpty()){
            errorMsg.setText("Name is Required");
            fullName.requestFocus();
            return;
        }

        // Check if email field is empty
        if(emailID.isEmpty()){
            errorMsg.setText("Email is Required");
            email.requestFocus();
            return;
        }

        // Check if user already exists in db
        DatabaseOperations db = new DatabaseOperations(this);
        Boolean isUserExist = db.isUserExist(emailID);

        // Show error message if user exists
        if(isUserExist) {
            errorMsg.setText("Email Already Registered");
            email.requestFocus();
            return;
        }

        // Check if pass field is empty
        if(password.isEmpty()){
            errorMsg.setText("Pass is Required");
            pass.requestFocus();
            return;
        }

        // Check for valid email
        if(!emailID.contains("@") && (!emailID.contains(".com") || !emailID.contains(".edu"))) {
            errorMsg.setText("Please enter a valid Email");
            email.requestFocus();
            return;
        }

        errorMsg.setText(""); // Set msg to null if activated

        // Enable progress bar
        signupProgressBar.setVisibility(View.VISIBLE);

        // Start registration process
        mAuth.createUserWithEmailAndPassword(emailID, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            User user = new User(name, emailID); // Create User Object

                            // Add data to firebase realtime database
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SignupActivity.this, "User Registered", Toast.LENGTH_SHORT).show();

                                                // Create User into DB
                                                DatabaseOperations db = new DatabaseOperations(SignupActivity.this);

                                                Boolean isUserCreated = db.insertUser(emailID);
                                                Log.i("isUserCreated", "User Creation Status: " + isUserCreated);

                                                mAuth.signOut();

                                                // Redirect to login page
                                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            }

                                            else {
                                                Log.e("Inside Second OnComplete", "Task Failed" );
                                                errorMsg.setText("Failed to register try again later");
                                            }

                                            signupProgressBar.setVisibility(View.GONE);
                                        }
                                    });

                        } else{
                            errorMsg.setText("Failed to register try again later");
                            Log.e("Inside First OnComplete", "Task Failed" );
                            signupProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
}