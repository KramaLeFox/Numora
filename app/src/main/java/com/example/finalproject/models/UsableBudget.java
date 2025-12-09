package com.example.finalproject.models;

public class UsableBudget {
    private double usableNecessity;
    private double usableLuxury;
    private double usableSaving;
    private double totalUsable;

    // Constructor
    public UsableBudget(double usableNecessity, double usableLuxury, double usableSaving) {
        this.usableNecessity = usableNecessity;
        this.usableLuxury = usableLuxury;
        this.usableSaving = usableSaving;
        // Calculate the total usable amount
        this.totalUsable = (usableNecessity) + (usableLuxury) + (usableSaving);
    }

    // Getters
    public double getUsableNecessity() {
        return usableNecessity;
    }

    public double getUsableLuxury() {
        return usableLuxury;
    }

    public double getUsableSaving() {
        return usableSaving;
    }

    public double getTotalUsable() {
        return totalUsable;
    }
}