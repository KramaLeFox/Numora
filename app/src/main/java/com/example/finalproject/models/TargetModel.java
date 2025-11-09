package com.example.finalproject.models;

public class TargetModel {
    private int id;
    private String type;          // e.g. "amount", "percentage", "hybrid"
    private int categoryId;       // link to Category
    private Integer tagId;        // optional link to Tag
    private double amount;        // target amount
    private double percentage;    // target percentage (0–100)
    private String startDate;     // YYYY-MM-DD
    private String endDate;       // YYYY-MM-DD

    // --- Constructors ---
    public TargetModel() {
        // empty default
    }

    public TargetModel(String type, int categoryId, Integer tagId, double amount, double percentage,
                       String startDate, String endDate) {
        this.type = type;
        this.categoryId = categoryId;
        this.tagId = tagId;
        this.amount = amount;
        this.percentage = percentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public TargetModel(int id, String type, int categoryId, Integer tagId, double amount, double percentage,
                       String startDate, String endDate) {
        this.id = id;
        this.type = type;
        this.categoryId = categoryId;
        this.tagId = tagId;
        this.amount = amount;
        this.percentage = percentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public double getAmount() {
        return amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // --- Helper Method ---
    @Override
    public String toString() {
        return "TargetModel{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", categoryId=" + categoryId +
                ", tagId=" + tagId +
                ", amount=" + amount +
                ", percentage=" + percentage +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}
