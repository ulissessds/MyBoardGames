package com.example.myboardgames.model;

public class User {
    private String profilePhotoId;
    private String name;
    private String email;
    private String phone;

    public User() {}

    public User(String profilePhotoId, String name, String email, String phone) {
        this.profilePhotoId = profilePhotoId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getProfilePhotoId() {
        return profilePhotoId;
    }

    public void setProfilePhotoId(String profilePhotoId) {
        this.profilePhotoId = profilePhotoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
