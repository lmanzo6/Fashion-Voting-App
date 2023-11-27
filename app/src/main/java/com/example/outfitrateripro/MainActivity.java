package com.example.outfitrateripro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.outfitrateripro.Cards.arrayAdapter;
import com.example.outfitrateripro.Cards.cards;
import com.example.outfitrateripro.Matches.MatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private com.example.outfitrateripro.Cards.arrayAdapter arrayAdapter;
    private int i;
    private FirebaseAuth mAuth;

    private String currentUId;
    private DatabaseReference usersDb;
    ListView listView;
    List<cards> rowItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        checkUserSex();

        rowItems = new ArrayList<cards>();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                incrementDislikes(userId);
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                cards obj = (cards) dataObject;
                String swipedUserId = obj.getUserId();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ID of the swiping user

                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                DatabaseReference swipedUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(swipedUserId);

                swipedUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Boolean dmEnabled = dataSnapshot.child("dmEnabled").getValue(Boolean.class);

                            currentUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Integer likes = dataSnapshot.child("likes").getValue(Integer.class);

                                    if ((dmEnabled != null && dmEnabled) || (likes != null && likes >= 1000)) {
                                        showCommentDialog(swipedUserId);
                                    } else {
                                        Toast.makeText(MainActivity.this, "User not accepting messages", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("SwipeDebug", "Error fetching likes data: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("SwipeDebug", "Error fetching DM data: " + databaseError.getMessage());
                    }
                });
                incrementLikes(swipedUserId);
                usersDb.child(swipedUserId).child("connections").child("yeps").child(currentUId).setValue(true);
            }



            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }
            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });



        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "Click!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void incrementDislikes(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("dislikes");
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentLikes = mutableData.getValue(Integer.class);
                if (currentLikes == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentLikes + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
            }
        });


    }
    private void incrementLikes(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("likes");
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentLikes = mutableData.getValue(Integer.class);
                if (currentLikes == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentLikes + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
            }
        });
    }


    private void showCommentDialog(final String userId) {
        final EditText editText = new EditText(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Send a Comment")
                .setView(editText)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String comment = editText.getText().toString();
                        if (!comment.isEmpty()) {
                            sendCommentToUser(userId, comment);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendCommentToUser(String userId, String comment) {
        String chatKey = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

        // Logic to create a new chat with the first comment
        DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("Chat").child(chatKey);
        Map newChat = new HashMap();
        newChat.put("createdByUser", currentUId);
        newChat.put("text", comment);
        newChat.put("timestamp", ServerValue.TIMESTAMP);
        chatDb.push().setValue(newChat);

        // Update Firebase to reflect the new chat
        usersDb.child(userId).child("connections").child("matches").child(currentUId).child("ChatID").setValue(chatKey);
        usersDb.child(currentUId).child("connections").child("matches").child(userId).child("ChatID").setValue(chatKey);

        Toast.makeText(MainActivity.this, "Comment sent and new chat created!", Toast.LENGTH_SHORT).show();
    }



    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Toast.makeText(MainActivity.this, "new connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(snapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatID").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(snapshot.getKey()).child("ChatID").setValue(key);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String userSex;
    private String oppositeUserSex;

    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("sex").getValue() != null){
                        userSex = dataSnapshot.child("sex").getValue().toString();
                        switch (userSex){
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        getOppositeSexUsers();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getOppositeSexUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null) {
                    if (dataSnapshot.exists()
                            && !dataSnapshot.child("connections").child("nope").hasChild(currentUId)
                            && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId)
                            && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {

                        String profileImageUrl = "default";
                        if (dataSnapshot.child("profileImageUrl").exists()) {
                            String imageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                            if (!imageUrl.equals("default")) {
                                profileImageUrl = imageUrl;
                            }
                        }
                        String clothingDescription = dataSnapshot.child("clothingDescription").exists() ? dataSnapshot.child("clothingDescription").getValue(String.class) : "Not specified";
                        String clothingCategory = dataSnapshot.child("clothingCategory").exists() ? dataSnapshot.child("clothingCategory").getValue(String.class) : "Not specified";

                        cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl, clothingDescription, clothingCategory);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
    }
}