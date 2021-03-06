package com.example.chatapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    private ImageView imageViewCustomLayoutButton;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        imageViewCustomLayoutButton=view.findViewById(R.id.message_receiver_image_view);
        mAuth=FirebaseAuth.getInstance();


        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();


        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.hasChild("image"))
                {
                    String recieverImage=dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(recieverImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.recieverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        holder.recieverMessageText.setVisibility(View.GONE);
        holder.recieverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        if(fromMessageType.equals("text"))
        {


            if(fromUserID.equals(messageSenderId))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());


            }
            else
            {


                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessageText.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.reciever_messages_layout);
                holder.recieverMessageText.setTextColor(Color.BLACK);
                holder.recieverMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }


        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserID.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_image_black_24dp).into(holder.messageSenderPicture);

                //to download image default method
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);*/
                        Uri uri = Uri.parse(userMessagesList.get(position).getMessage());
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_VIEW);
                        shareIntent.setDataAndType(uri, "image/*");
                        holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent,"select an app to open the file"));
                    }
                });

            }
            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_image_black_24dp).into(holder.messageReceiverPicture);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(userMessagesList.get(position).getMessage());
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_VIEW);
                        shareIntent.setDataAndType(uri, "image/*");
                        holder.itemView.getContext().startActivity(Intent.createChooser(shareIntent,"select an app to open the file"));

                    }
                });

            }
        }
        else
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else
            {
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);
            }
        }
        usersRef.keepSynced(true);
    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, recieverMessageText;
        public CircleImageView recieverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public MessageViewHolder(@NonNull View itemView)

        {


            super(itemView);

            senderMessageText=itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=itemView.findViewById(R.id.receiver_message_text);
            recieverProfileImage=itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);


        }
    }


}
