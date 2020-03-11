package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Switch aSwitch;

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;

    private static final int GalleyPick=1;

    private Uri downloadUri;
    private Uri fileUri;
    private Uri ImageUri;

    private Toolbar SettingsToolBar;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }


        });

        RetrieveUserInfo();
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    hideSystemUI();

                }
                else
                    {
                       showSystemUI();
                }
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleyPick);

            }
        });
    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if((dataSnapshot.exists())
                                && (dataSnapshot.hasChild("name")
                        &&(dataSnapshot.hasChild("image"))))
                        {

                            userProfileImage.setImageURI(downloadUri);

                        }

                            if((dataSnapshot.exists())
                                && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            if(dataSnapshot.hasChild("image")) {

                                String retrieveImage= Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                Glide.with(SettingsActivity.this).load(retrieveImage).into(userProfileImage);
                               /* Toast.makeText(SettingsActivity.this,
                                        retrieveImage,
                                        Toast.LENGTH_LONG).show();*/
                            }

                        }
                            else
                            {

                                userName.setVisibility(View.VISIBLE);
                                Toast.makeText(SettingsActivity.this,
                                        "please set and update your profile information",
                                        Toast.LENGTH_LONG).show();

                            }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }

    private void UpdateSettings()
    {
        String setUserName= userName.getText().toString();
        String setStatus= userStatus.getText().toString();
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(SettingsActivity.this,"Please write your status"
            ,Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingsActivity.this,"write your name first"
                    ,Toast.LENGTH_LONG).show();
        }
        else
        {
            HashMap<String, Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this,"Profile updated successfully"
                                        ,Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(SettingsActivity.this,"Error: "+task.getException().toString()
                                        ,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    private void InitializeFields()

    {
        aSwitch=findViewById(R.id.screenFullSwitch);
        progressDialog=new ProgressDialog(SettingsActivity.this);
        UpdateAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.set_profile_image);


        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        SettingsToolBar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));

        getSupportActionBar().setLogo(R.drawable.ic_settings_black_24dp);
        getSupportActionBar().getThemedContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Details");

    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleyPick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Set profile Image");
                progressDialog.setMessage("Please wait your profile image is updating...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                assert result != null;
                final Uri resultUri = result.getUri();

                final StorageReference filePath=UserProfileImageRef.child(currentUserID+".jpg");
                final UploadTask uploadTask=filePath.putFile(resultUri);
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {



                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        progressDialog.setMessage("please wait");
                        progressDialog.show();

                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();


                            Toast.makeText(SettingsActivity.this,"Profile image uploaded success",Toast.LENGTH_LONG).show();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUri.toString());
                            Glide.with(SettingsActivity.this).load(downloadUri).into(userProfileImage);
                            if(TextUtils.isEmpty(downloadUri.toString()))
                                UpdateSettings();

                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,"Error :"+task.getException().getMessage().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        downloadUri=uriTask.getResult();



                        Toast.makeText(SettingsActivity.this,"Profile image uploaded success",Toast.LENGTH_LONG).show();

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void SendUserToMainActivity() {

        Intent loginIntent=new Intent(this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

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
    private void showSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_VISIBLE
                        );
    }

    public String getFileExt(Uri uri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap map=MimeTypeMap.getSingleton();


        return  map.getExtensionFromMimeType(
                contentResolver.getType(uri));

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
