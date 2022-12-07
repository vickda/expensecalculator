package com.example.expensecalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // View initializations
    TextInputEditText emailID, pass;
    TextView goToSignup, loginErrorMsg;
    Button loginBtn;
    ProgressBar loginProgressBar;

    // Google Login button Variables
    ImageButton loginWithGoogleBtn;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    // Firebase init
    FirebaseAuth mauth;
    FirebaseUser muser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Edit Text & TextView
        emailID = findViewById(R.id.emailEditText);
        pass = findViewById(R.id.passEditText);
        loginErrorMsg = findViewById(R.id.loginErrorMsg);

        // Setting Listeners
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        goToSignup = findViewById(R.id.goToSignup);
        goToSignup.setOnClickListener(this);

        loginProgressBar = findViewById(R.id.loginProgressBar); // Progress bar

        // Login Via Google SignIn Button Variables
        loginWithGoogleBtn = findViewById(R.id.signinWithGooglebtn);
        loginWithGoogleBtn.setOnClickListener(this);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        // Init Firebase Auth
        mauth = FirebaseAuth.getInstance();
        muser = mauth.getCurrentUser();

        // Check if user is already logged in
        if(muser != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.goToSignup:
                startActivity(new Intent(this, SignupActivity.class));
                break;

            case R.id.loginBtn:
                userLogin();
                break;
                
            case R.id.signinWithGooglebtn:
                signInViaGoogle();
                break;
        }
    }

    // Login Button via firebase Logic
    private void userLogin() {

        // Getting String value
        String email, password;
        email = emailID.getText().toString().trim();
        password = pass.getText().toString().trim();

        // Check if email field is empty
        if(email.isEmpty()){
            loginErrorMsg.setText("Email is Required");
            emailID.requestFocus();
            return;
        }

        // Check if pass field is empty
        if(password.isEmpty()){
            loginErrorMsg.setText("Pass is Required");
            pass.requestFocus();
            return;
        }

        // Check for valid email
        if(!email.contains("@") || !email.contains(".com")) {
            loginErrorMsg.setText("Please enter a valid Email");
            emailID.requestFocus();
            return;
        }

        loginErrorMsg.setText(""); // Resetting the text

        loginProgressBar.setVisibility(View.VISIBLE);

        // Authenticate using firebase for normal login
        mauth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                        else{
                            loginErrorMsg.setText("Failed to login please try again");
                        }
                    }
                });


    }

    // SignIn via Google Code logic
    public void signInViaGoogle(){
        Intent signinIntent = gsc.getSignInIntent();
        startActivityForResult(signinIntent, 1000);
//        startActivity(signinIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                // Authenticating via firebase using google signIn
                mauth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    // Check if user already exists in db if not create a row
                                    String email = account.getEmail();
                                    DatabaseOperations db = new DatabaseOperations(LoginActivity.this);
                                    Boolean isUserExist = db.isUserExist(email);

                                    // Show error message if user exists
                                    if(!isUserExist) {
                                        db.insertUser(email);
                                    }

                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else{
                                    loginErrorMsg.setText("Oops something went wrong");
                                    finish();
                                }
                            }
                        });


            } catch (ApiException e) {
                e.printStackTrace();
                loginErrorMsg.setText("Oops something went wrong");
                finish();
            }
        }
    }
}