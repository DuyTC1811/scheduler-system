package org.example.model;

public class Category {
    private String eventTime;
    private String eventType;
    private String brand;
    private String price;
    private String userId;

    public Category() {
    }

    public Category(String eventTime, String eventType, String brand, String price, String userId) {
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.brand = brand;
        this.price = price;
        this.userId = userId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
