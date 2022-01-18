package com.example.myboardgames.model;

public class ItemBG {
    private String photoUrl;
    private String name;
    private String category;
    private String numberOfPlayers;
    private String playTime;
    private Double price;

    public ItemBG() {}

    public ItemBG(String photoUrl, String name, String category, String numberOfPlayers, String playTime, Double price) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.category = category;
        this.numberOfPlayers = numberOfPlayers;
        this.playTime = playTime;
        this.price = price;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(String numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }
}
