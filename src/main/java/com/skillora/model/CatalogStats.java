package com.skillora.model;

import java.util.HashMap;
import java.util.Map;

public class CatalogStats {
    private int totalProducts;
    private double avgPrice;
    private double minPrice;
    private double maxPrice;
    private final Map<String, Integer> countByType = new HashMap<>();

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Map<String, Integer> getCountByType() {
        return countByType;
    }
}
