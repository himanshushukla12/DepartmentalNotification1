package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity
{

    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference UsersRef,RootRef;

    private static final String TAG = "MainActivity";

    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword,ForgetPasswordEditText;
    private Button resetPasswordButton;
    private TextView NeedNewAccountLink,ForgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        mAuth=FirebaseAuth.getInstance();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);
        currentUser=mAuth.getCurrentUser();
        InitialiseFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendUserToRegisterActivity();
            }
        });
        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog=new Dialog(LoginActivity.this);
                dialog.setContentView(R.layout.forgot_password_custom_layout);
                //dialog.setCancelable(false);
                ForgetPasswordEditText=dialog.findViewById(R.id.emailEditText);
                resetPasswordButton=dialog.findViewById(R.id.resetPasswordButtonCustom);
                resetPasswordButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        mAuth.sendPasswordResetEmail(ForgetPasswordEditText.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(LoginActivity.this,"password reset link\nhas been sent to your\nregistered email id",Toast.LENGTH_LONG).show();
                                            dialog.dismiss();

                                        }
                                        else
                                        {
                                            Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                    }
                });
                dialog.show();
            }
        });
        
        LoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AllowUserLogin();
            }

            private void AllowUserLogin()
            {
                String email=UserEmail.getText().toString();
                final String password=UserPassword.getText().toString();
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this,"Please enter email",Toast.LENGTH_LONG).show();

                }

                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this,"Please enter email",Toast.LENGTH_LONG).show();
                }

                else {

                    progressDialog.setTitle("Logging in");
                    progressDialog.setMessage("please wait");
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        String currentUserID=mAuth.getCurrentUser().getUid();
                                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                        UsersRef.child(currentUserID).child("device_token")
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            SendUserToMainActivity();

                                                            progressDialog.dismiss();
                                                            Toast.makeText(LoginActivity.this,
                                                                    "Login success",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });





                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this,
                                                "Error : "+task.getException().toString(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                }
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

    }

    private void InitialiseFields()
    {


        LoginButton=findViewById(R.id.login_button);
        PhoneLoginButton=findViewById(R.id.phone_login_button);
        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        NeedNewAccountLink=findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=findViewById(R.id.forget_password_link);
        progressDialog=new ProgressDialog(LoginActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser!=null)
        {
            SendUserToMainActivity();
        }

    }


    private void SendUserToMainActivity()
    {

        Intent loginIntent=new Intent(this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
    private void SendUserToRegisterActivity() {

        Intent loginIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(loginIntent);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void enablePersistence() {
        // [START rtdb_enable_persistence]


        RootRef.keepSynced(true);

        RootRef.orderByValue().limitToLast(4).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "The " +dataSnapshot.getKey() + " dinosaur's score is " + dataSnapshot.getValue());

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // [END rtdb_enable_persistence]
    }



}
