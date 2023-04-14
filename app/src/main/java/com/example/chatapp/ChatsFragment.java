package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import notification.NotificationHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private String retImage="default_image";

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView=inflater.inflate(R.layout.fragment_chats, container, false);

        //fresco image viewer

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);
        ChatsRef.keepSynced(true);
        chatsList=(RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));



        return PrivateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {

                final String usersIDs=getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                       if(dataSnapshot.exists())
                       {

                           if(dataSnapshot.hasChild("image"))
                           {
                               retImage = dataSnapshot.child("image").getValue().toString();
                               Picasso.get().load(retImage).into(holder.profileImage);

                           }
                           final String retName= dataSnapshot.child("name").getValue().toString() ;
                           final String retStatus= dataSnapshot.child("status").getValue().toString() ;

                           holder.userName.setText(retName);


                           if(dataSnapshot.child("userState").hasChild("state"))
                           {
                               String date=dataSnapshot.child("userState").child("date").getValue().toString();
                               String state=dataSnapshot.child("userState").child("state").getValue().toString();
                               String time=dataSnapshot.child("userState").child("time").getValue().toString();

                               if(state.equals("online"))
                               {
                                   holder.userStatus.setText("Online");
                               }
                               else if(state.equals("offline"))
                               {
                                   holder.userStatus.setText("Last Seen: "+date+"\n"+time);
                               }
                           }
                           else
                           {

                               holder.userStatus.setText("Offline");
                           }


                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v)
                               {
                                   Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                   chatIntent.putExtra("visit_user_id",usersIDs);
                                   chatIntent.putExtra("visit_user_name",retName);
                                   chatIntent.putExtra("visit_image",retImage);


                                   startActivity(chatIntent);
                               }
                           });

                       }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }
    public static  class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus,userName;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage=itemView.findViewById(R.id.user_profile_image);
            userStatus=itemView.findViewById(R.id.user_status);
            userName=itemView.findViewById(R.id.user_profile_name);

        }
    }
}
