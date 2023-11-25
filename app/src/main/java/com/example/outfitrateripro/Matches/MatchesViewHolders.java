package com.example.outfitrateripro.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.outfitrateripro.Chat.ChatActivity;
import com.example.outfitrateripro.R;

import java.text.BreakIterator;

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mMatchId, mMatchName, latestMessageTextView, latestMessageTimestampTextView;
    public ImageView mMatchImage;


    public MatchesViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        latestMessageTimestampTextView = (TextView) itemView.findViewById(R.id.Timestamp);
        mMatchId = (TextView) itemView.findViewById(R.id.Matchid);
        mMatchName = (TextView) itemView.findViewById(R.id.MatchName);
        latestMessageTextView = (TextView) itemView.findViewById(R.id.LatestMessage);
        mMatchImage = (ImageView) itemView.findViewById(R.id.MatchImage);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", mMatchId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
