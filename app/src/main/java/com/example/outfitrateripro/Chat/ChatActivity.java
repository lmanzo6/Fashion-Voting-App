package com.example.outfitrateripro.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.outfitrateripro.Matches.MatchesActivity;
import com.example.outfitrateripro.Matches.MatchesAdapter;
import com.example.outfitrateripro.Matches.MatchesObject;
import com.example.outfitrateripro.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private EditText mSendEditText;

    private Button mSendButton;
    private String currentUserID, matchId, chatId;

    DatabaseReference mDatabaseUser, mDatabaseChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        matchId = getIntent().getExtras().getString("matchId");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("ChatID");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");

        getChatId();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);

        mSendEditText = findViewById(R.id.message);
        mSendButton = findViewById(R.id.send);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

            }
        });
        fetchChatPartnerInformation(matchId);
    }

    private void fetchChatPartnerInformation(String userId) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = "";
                    String userProfileImageUrl = "";
                    if (dataSnapshot.child("name").getValue() != null) {
                        userName = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.child("profileImageUrl").getValue() != null) {
                        userProfileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }
                    updateHeader(userName, userProfileImageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
        private void sendMessage () {
            String sendMessageText = mSendEditText.getText().toString();

            if (!sendMessageText.isEmpty()) {
                DatabaseReference newMessageDb = mDatabaseChat.push();

                Map newMessage = new HashMap();
                newMessage.put("createdByUser", currentUserID);
                newMessage.put("text", sendMessageText);

                newMessageDb.setValue(newMessage);
            }
            mSendEditText.setText(null);
        }

        private void getChatId () {
            mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        chatId = dataSnapshot.getValue().toString();
                        mDatabaseChat = mDatabaseChat.child(chatId);
                        getChatMessages();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void getChatMessages () {
            mDatabaseChat.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        String message = "";
                        String createdByUser = "";

                        if (snapshot.child("text").getValue() != null) {
                            message = snapshot.child("text").getValue().toString();
                        }
                        if (snapshot.child("createdByUser").getValue() != null) {
                            createdByUser = snapshot.child("createdByUser").getValue().toString();
                        }
                        if (message != null && createdByUser != null) {
                            Boolean currentUserBoolean = false;
                            if (createdByUser.equals(currentUserID)) {
                                currentUserBoolean = true;
                            }
                            ChatObject newMessage = new ChatObject(message, currentUserBoolean);
                            resultsChat.add(newMessage);
                            mChatAdapter.notifyDataSetChanged();
                        }

                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }



    private ArrayList<ChatObject> resultsChat = new ArrayList<ChatObject>();

    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }


    private void updateHeader(String userName, String imageUrl) {
        TextView userNameTextView = findViewById(R.id.userName);
        ImageView userProfileImageView = findViewById(R.id.userProfilePic);

        userNameTextView.setText(userName);

        if (!imageUrl.isEmpty() && !imageUrl.equals("default")) {
            Glide.with(this).load(imageUrl).into(userProfileImageView);
        } else {
            // Set a default image or placeholder
            userProfileImageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

}