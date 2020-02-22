package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String messageRecieverID,messageRecieverName,messageRecieverImage,messageSenderID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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
    }

    private void InitializeControllers()
    {


        ChatToolBar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionView);

        userImage =(CircleImageView) findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen=findViewById(R.id.custom_user_last_seen);

        SendMessageButton=findViewById(R.id.send_message_btn);
        MessageInputText= findViewById(R.id.input_message);

        messageAdapter =new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_os_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageRecieverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override

                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
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
}
