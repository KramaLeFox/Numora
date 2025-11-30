package com.example.finalproject.models;

public class CategoryModel {
    public int id;
    public String name;
    public String type;
    public String extType;
    public String description;

    public CategoryModel(int id, String name, String type, String extType, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.extType = extType;
        this.description = description;
    }
}

