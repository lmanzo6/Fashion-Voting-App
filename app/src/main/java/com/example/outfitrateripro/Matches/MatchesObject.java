package com.example.outfitrateripro.Matches;



public class MatchesObject {
    private String userId;
    private String name;
    private String profileImageUrl;
    private String latestMessage;
    private String latestMessageTimestamp;
    public MatchesObject (String userId, String name, String profileImageUrl, String latestMessage, String latestMessageTimestamp){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.latestMessage = latestMessage;
        this.latestMessageTimestamp = latestMessageTimestamp;
    }

    public void setLatestMessageTimestamp(String latestMessageTimestamp) {
        this.latestMessageTimestamp = latestMessageTimestamp;
    }

    public String getLatestMessageTimestamp() {
        return latestMessageTimestamp;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }
}
