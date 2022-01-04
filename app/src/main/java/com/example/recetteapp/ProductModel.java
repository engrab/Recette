package com.example.recetteapp;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductModel implements Parcelable {

    private String id;
    private String name;
    private String images;
    private int price;
    private int quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static Creator<ProductModel> getCREATOR() {
        return CREATOR;
    }

    public ProductModel(String id, String name, String images, int price, int quantity) {
        this.id = id;
        this.name = name;
        this.images = images;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.images);
        dest.writeInt(this.price);
        dest.writeInt(this.quantity);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.name = source.readString();
        this.images = source.readString();
        this.price = source.readInt();
        this.quantity = source.readInt();
    }

    public ProductModel() {
    }

    protected ProductModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.images = in.readString();
        this.price = in.readInt();
        this.quantity = in.readInt();
    }

    public static final Parcelable.Creator<ProductModel> CREATOR = new Parcelable.Creator<ProductModel>() {
        @Override
        public ProductModel createFromParcel(Parcel source) {
            return new ProductModel(source);
        }

        @Override
        public ProductModel[] newArray(int size) {
            return new ProductModel[size];
        }
    };
}
