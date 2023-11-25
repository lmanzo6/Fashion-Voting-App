package com.example.outfitrateripro.Cards;

public class cards {
    private String userId;
    private String name;
    private String profileImageUrl;
    private String clothingDescription; // New field
    private String clothingCategory; // New field
    public cards (String userId, String name, String profileImageUrl, String clothingDescription, String
                  clothingCategory){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.clothingDescription = clothingDescription;
        this.clothingCategory = clothingCategory;
    }
    public String getClothingDescription() {
        return clothingDescription;
    }

    public void setClothingDescription(String clothingDescription) {
        this.clothingDescription = clothingDescription;
    }

    public String getClothingCategory() {
        return clothingCategory;
    }

    public void setClothingCategory(String clothingCategory) {
        this.clothingCategory = clothingCategory;
    }
    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }
}
