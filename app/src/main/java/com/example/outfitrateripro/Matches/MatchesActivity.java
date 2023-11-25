package com.example.outfitrateripro.Matches;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.outfitrateripro.MainActivity;
import com.example.outfitrateripro.R;
import com.example.outfitrateripro.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mMatchesAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mMatchesLayoutManager = new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);
        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), MatchesActivity.this);
        mRecyclerView.setAdapter(mMatchesAdapter);

        fetchMatchesData();


    }
    protected void onResume() {
        super.onResume();
        fetchMatchesData(); // Call this method to refresh data when the activity resumes
    }

    private void fetchMatchesData() {
        // Clear existing data
        resultsMatches.clear();
        if (mMatchesAdapter != null) {
            mMatchesAdapter.notifyDataSetChanged();
        }

        // Fetch data logic
        getUserMatchId();
    }

    private void getUserMatchId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot match : snapshot.getChildren()){
                        FetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void FetchMatchInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("Chat");
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String userId = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class) != null ? snapshot.child("profileImageUrl").getValue(String.class) : "default";

                    String chatId = snapshot.child("connections").child("matches").child(currentUserID).child("ChatID").getValue(String.class);
                    chatDb.child(chatId).orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("ChatDebug", "DataSnapshot from Firebase: " + dataSnapshot.toString());

                            String latestMessage = "No messages";
                            String timestamp = "Unknown";
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    latestMessage = childSnapshot.child("text").getValue(String.class);
                                    if (childSnapshot.hasChild("timestamp")) {
                                        Long rawTimestamp = childSnapshot.child("timestamp").getValue(Long.class);
                                        timestamp = formatTimestamp(rawTimestamp);
                                    }
                                }
                            }

                            boolean exists = false;
                            for (MatchesObject match : resultsMatches) {
                                if (match.getUserId().equals(userId)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                MatchesObject obj = new MatchesObject(userId, name, profileImageUrl, latestMessage, timestamp);
                                resultsMatches.add(obj);
                                Log.d(String.valueOf(resultsMatches.size()), "matches size");
                                mMatchesAdapter.notifyDataSetChanged();
                            }
                        }
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String formatTimestamp(Long rawTimestamp) {
        if (rawTimestamp == null) return "Unknown";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(rawTimestamp));
    }

    private ArrayList<MatchesObject> resultsMatches = new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetMatches() {
        return resultsMatches;
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(MatchesActivity.this, MainActivity.class);
        startActivity(intent);
    }


}