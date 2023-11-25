package com.example.outfitrateripro.Matches;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.outfitrateripro.R;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolders> {
    private List<MatchesObject> matchesList;
    private Context context;



    public MatchesAdapter(List<MatchesObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }
    @NonNull
    @Override
    public MatchesViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matches, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        MatchesViewHolders rcv = new MatchesViewHolders(layoutView);
        return rcv;
    }


    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolders holder, int position) {
        MatchesObject currentMatch = matchesList.get(position);
        holder.mMatchId.setText(currentMatch.getUserId());
        holder.mMatchName.setText(currentMatch.getName());
        holder.latestMessageTextView.setText(currentMatch.getLatestMessage());
        holder.latestMessageTimestampTextView.setText(currentMatch.getLatestMessageTimestamp());


        if(!currentMatch.getProfileImageUrl().equals("default") && currentMatch.getProfileImageUrl() != null) {
            Glide.with(context)
                    .load(currentMatch.getProfileImageUrl())
                    .into(holder.mMatchImage);
        } else {
            // Set a default image or placeholder
            holder.mMatchImage.setImageResource(R.mipmap.ic_launcher); // Replace 'default_image' with your actual default image resource
        }
    }



    @Override
    public int getItemCount() {
        return matchesList.size();
    }
}
