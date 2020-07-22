package com.abhish.blogapp;

import java.io.Serializable;

public class Upload implements Serializable {
    private String visionName;
    private  String imageUrl;
    private String description;
    private String visionID;


    public String getVisionID() {
        return visionID;
    }

    public void setVisionID(String visionID) {
        this.visionID = visionID;
    }

    public String getVisionName() {
        return visionName;
    }

    public void setVisionName(String visionName) {
        this.visionName = visionName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }




    public Upload()
    {

    }

    public Upload(String visionID, String visionName, String imageUrl, String description)
    {
        if (visionName.trim().equals(""))
        {
            visionName="No Name";
        }
            this.visionName =visionName;
            this.imageUrl=imageUrl;
            this.description=description;
            this.visionID=visionID;
    }
}
