package com.example.chatapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String messageRecieverID,messageRecieverName,messageRecieverImage,messageSenderID,currentUserID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;
    private static final String TAG = "ChatActivity";
    private int GALLERY = 1, CAMERA = 2;
    private ProgressDialog progressDialog;
    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;


    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private  String saveCurrentTime, saveCurrentDate;
    private String checker="",myUrl;
    private StorageTask uploadTask;
    private Uri fileUri,downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final String[] uri = new String[1];
        
        messageRecieverID=getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("visit_user_name").toString();
        messageRecieverImage=getIntent().getExtras().get("visit_image").toString();

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        InitializeControllers();

        userName.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profilepicture).into(userImage);



        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            {
                SendMessage();
                
            }
        });
        DisplayLastSeen();


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "images","PDF files","Ms Word files","current location"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @SuppressLint("ServiceCast")
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which==0)
                        {
                            checker="image";

                           /* Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);*/

                           showPictureDialog();
                        }
                        else
                            if(which==1)
                            {
                                checker="PDF files";
                                Intent intent=new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/pdf");
                                startActivityForResult(intent.createChooser(intent,"Select pdf file"),438);

                            }
                            else if(which==2)
                            {
                                checker="docx";
                                Intent intent=new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/msword");
                                startActivityForResult(intent.createChooser(intent,"Select MS Word file"),438);

                            }
                        /*    else if(which==3)
                            {
                                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0
                                        );
                                    }
                                locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                                    @Override
                                    public void onLocationChanged(@NonNull Location location) {
                                        double latitude=location.getLatitude();
                                        double longitude=location.getLongitude();
                                        uri[0]=String.format(Locale.ENGLISH,"geo:%.2f,%.2f",latitude,longitude);
                                        String messageText = uri[0];
                                        Geocoder geocoder=new Geocoder(ChatActivity.this);
                                        try{
                                            List<Address> list=geocoder.getFromLocation(latitude,longitude,1);
                                            String country=list.get(0).getCountryName();
                                            String locality=list.get(0).getLocality();
                                            String address= list.get(0).getAddressLine(0);
                                            messageText="Latitude :"+latitude+", Longitude :"+longitude+"\n Address"+address+" "+locality+" "+country;
                                        }
                                        catch (Exception e)
                                        {

                                        }




                                            String messageSenderRef= "Messages/"+messageSenderID+"/"+messageRecieverID;
                                            String messageRecieverRef="Messages/"+messageRecieverID+"/"+messageSenderID;

                                            DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                                                    .child(messageSenderID)
                                                    .child(messageRecieverID).push();
                                            String messagePushID= userMessageKeyRef.getKey();
                                            Map messageTextBody=new HashMap();
                                            messageTextBody.put("message",messageText);
                                            messageTextBody.put("type","text");
                                            messageTextBody.put("from",messageSenderID);
                                            messageTextBody.put("to",messageRecieverID);
                                            messageTextBody.put("messageID",messagePushID);
                                            messageTextBody.put("time",saveCurrentTime);
                                            messageTextBody.put("date",saveCurrentDate);

                                            Map messageBodyDetails =new HashMap();
                                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                                            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

                                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(ChatActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                                    }
                                                    MessageInputText.setText("");
                                                }
                                            });

                                    }
                                });
                            }*/
                            else if (which==3) {
                                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                                locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        double latitude = location.getLatitude();
                                        double longitude = location.getLongitude();
                                        uri[0]=String.format(Locale.ENGLISH,"geo:%.2f,%.2f",latitude,longitude);
                                        String messageText = uri[0];
                                        Geocoder geocoder=new Geocoder(ChatActivity.this);
                                        try{
                                            List<Address> list=geocoder.getFromLocation(latitude,longitude,1);
                                            String country=list.get(0).getCountryName();
                                            String locality=list.get(0).getLocality();
                                            String address= list.get(0).getAddressLine(0);
                                            messageText="Latitude :"+latitude+", Longitude :"+longitude+"\n Address"+address+" "+locality+" "+country;
                                            String messageSenderRef= "Messages/"+messageSenderID+"/"+messageRecieverID;
                                            String messageRecieverRef="Messages/"+messageRecieverID+"/"+messageSenderID;

                                            DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                                                    .child(messageSenderID)
                                                    .child(messageRecieverID).push();
                                            String messagePushID= userMessageKeyRef.getKey();
                                            Map messageTextBody=new HashMap();
                                            messageTextBody.put("message",messageText);
                                            messageTextBody.put("type","text");
                                            messageTextBody.put("from",messageSenderID);
                                            messageTextBody.put("to",messageRecieverID);
                                            messageTextBody.put("messageID",messagePushID);
                                            messageTextBody.put("time",saveCurrentTime);
                                            messageTextBody.put("date",saveCurrentDate);

                                            Map messageBodyDetails =new HashMap();
                                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                                            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

                                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(ChatActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                                    }
                                                    MessageInputText.setText("");
                                                }
                                            });
                                        }
                                        catch (Exception e)
                                        {

                                        }
                                        Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {
                                    }
                                };
                                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                } else {
                                    // Permissions are granted, request a single location update
                                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                                }

                            }
                    }
                });
                builder.show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                }
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }
    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, 438);
    }
    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 438);
    }
    private void InitializeControllers()
    {

        progressDialog=new ProgressDialog(ChatActivity.this);

        ChatToolBar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionView);

        userImage =(CircleImageView) findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_user_last_seen);

        SendMessageButton=findViewById(R.id.send_message_btn);
        SendFilesButton=findViewById(R.id.send_files_btn);
        MessageInputText= findViewById(R.id.input_message);

        messageAdapter =new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_os_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");

        saveCurrentTime=currentTime.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
        {
            progressDialog.setTitle("Sending file");
            progressDialog.setMessage("Please wait,\nwhile file is sending...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            fileUri=data.getData();
            if(!checker.equals("image"))
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("document Files");

                final String messageSenderRef= "Messages/"+messageSenderID+"/"+messageRecieverID;
                final String messageRecieverRef="Messages/"+messageRecieverID+"/"+messageSenderID;

                final DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                        .child(messageSenderID)
                        .child(messageRecieverID).push();
                final String messagePushID= userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushID+"."+checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        downloadUri=uriTask.getResult();
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                        if(task.isSuccessful())
                        {
                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",downloadUri.toString());
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageRecieverID);
                            messageTextBody.put("messageID",messagePushID);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetails =new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            progressDialog.dismiss();
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double p=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage((int)p+"% uploading");
                    }
                });

            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef= "Messages/"+messageSenderID+"/"+messageRecieverID;
                final String messageRecieverRef="Messages/"+messageRecieverID+"/"+messageSenderID;

                final DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                        .child(messageSenderID)
                        .child(messageRecieverID).push();
                final String messagePushID= userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushID+"."+"jpg");

                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()  {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUri=task.getResult();
                            myUrl=downloadUri.toString();
                            //String messagePushID= userMessageKeyRef.getKey();
                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageRecieverID);
                            messageTextBody.put("messageID",messagePushID);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetails =new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                                    }
                                    MessageInputText.setText("");
                                }
                            });


                        }
                    }
                });
            }
            else
            {
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageSenderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String date=dataSnapshot.child("userState").child("date").getValue().toString();
                            String state=dataSnapshot.child("userState").child("state").getValue().toString();
                            String time=dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("online"))
                            {
                                userLastSeen.setText("Online");
                            }
                            else if(state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: "+date+"\n"+time);
                            }
                        }
                        else
                        {

                            userLastSeen.setText("Offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageRecieverID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);



                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
    }

    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "message field is empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef= "Messages/"+messageSenderID+"/"+messageRecieverID;
            String messageRecieverRef="Messages/"+messageRecieverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                    .child(messageSenderID)
                    .child(messageRecieverID).push();
            String messagePushID= userMessageKeyRef.getKey();
            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageRecieverID);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails =new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();

                    }
                    MessageInputText.setText("");
                }
            });
        }
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
    private void VerifyUserExistence()
    {
        String currentUserID=mAuth.getCurrentUser().getUid();

        RootRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name")).exists())

                {
                   // Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //    SendUserToSettingsActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");

        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap =new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID=mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);


    }
}