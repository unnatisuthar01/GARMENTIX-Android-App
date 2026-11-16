package com.example.garmentix;

public class item_Fabric {

    int id;
    String category;
    String name;
    int image;
    boolean textureAvailable;

    public item_Fabric(int id, String category,String name,int image){
        this(id, category, name, image, true);
    }

    public item_Fabric(int id, String category, String name, int image,
                       boolean textureAvailable) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.image = image;
        this.textureAvailable = textureAvailable;
    }
}
