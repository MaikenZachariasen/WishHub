package com.example.wishhubproto.model;

import com.example.wishhubproto.model.User;
import com.example.wishhubproto.model.Lists;
import java.awt.image.BufferedImage;
import java.util.List;

public class Wish{

    private int wishID;
    private String wishName;
    private String description;
    private String imgDataPath;

    //The purpose of writing a blank constructor is to create
    //an instance of the User object, where I wish to have this instance populated later, rather than imidiatly at the
    //creation of its instance.
    public Wish() {

    }

    public Wish(int wishID, String wishName, String description, String imgdatapath) {
        this.wishID = wishID;
        this.wishName = wishName;
        this.description = description;
        this.imgDataPath = imgdatapath;
    }

    public int getWishID() {
        return wishID;
    }

    public String getWishName() {
        return wishName;
    }

    public String getWishDescription() {
        return description;
    }

    public String getImgDataPath() {
        return imgDataPath;
    }

    public void setWishID(int wishID) {
        this.wishID = wishID;
    }
    public void setTitle(String title) {
        this.wishName = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setImgDataPath(String imgDataPath) {
        this.imgDataPath = imgDataPath;
    }




}
