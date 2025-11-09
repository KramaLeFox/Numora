package com.example.finalproject.models;

public class TransactionModel {
    private String category;
    private String type;
    private String date;
    private double amount;
    private String note;

    public TransactionModel(String category, String type, String date, double amount, String note) {
        this.category = category;
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.note = note;
    }

    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
}