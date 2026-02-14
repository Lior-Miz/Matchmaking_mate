package com.example.matchmaking_mate;

import java.util.ArrayList;
import java.util.List;


/*user constructor for name, email, phone, id, favorite games and friends */
public class User {

    private String fullname;
    private String email;
    private String phone;
    private String userid;
    private List<String> favoriteGames;
    private List<String> Friends;


    public User() {
        this.favoriteGames = new ArrayList<>();
    }

    public User(String fullname, String email, String phone, String userid) {
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.userid = userid;
        this.favoriteGames = new ArrayList<>();
        this.Friends = new ArrayList<>();
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<String> getFavoriteGames() {
        if (favoriteGames == null) {
            favoriteGames = new ArrayList<>();
        }
        return favoriteGames;
    }

    public void setFavoriteGames(List<String> favoriteGames) {
        this.favoriteGames = favoriteGames;
    }
    public void setFriends(List<String> friends){ this.Friends=friends; }

    public List<String> getFriends() {
        if (Friends == null) {
            Friends = new ArrayList<>();
        }
        return Friends;
    }
    public void addGame(String gameName) {
        if (this.favoriteGames == null) {
            this.favoriteGames = new ArrayList<>();
        }
        if (!this.favoriteGames.contains(gameName)) {
            this.favoriteGames.add(gameName);
        }
    }

    public void removeGame(String gameName) {
        if (this.favoriteGames != null) {
            this.favoriteGames.remove(gameName);
        }
    }
}