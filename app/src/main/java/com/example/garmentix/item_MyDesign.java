package com.example.garmentix;

public class item_MyDesign {
    long id;
    String name;
    String category;
    String date;
    int image;
    String previewPath;
    String designData;
    boolean favorite;

    public item_MyDesign(
            long id,
            String name,
            String category,
            String date,
            int image
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.date = date;
        this.image = image;
    }

    public item_MyDesign(long id, String name, String category, String date,
                         int image, String previewPath, String designData,
                         boolean favorite) {
        this(id, name, category, date, image);
        this.previewPath = previewPath;
        this.designData = designData;
        this.favorite = favorite;
    }
}
