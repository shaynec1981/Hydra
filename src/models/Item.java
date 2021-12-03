package models;

import java.math.BigDecimal;

public class Item {

    private String sku, name, description, category, vendor, measurement;
    private BigDecimal cost;
    private double markup, amount;
    private int id, curStock, minStock, maxStock;
    private boolean sellable;

    public Item (int id, String sku, String name, String description, String category, BigDecimal cost, double markup, String vendor, int curStock, int minStock, int maxStock, double amount, String measurement, boolean sellable){
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.cost = cost;
        this.markup = markup;
        this.vendor = vendor;
        this.curStock = curStock;
        this.minStock = minStock;
        this.maxStock = maxStock;
        this.amount = amount;
        this.measurement = measurement;
        this.sellable = sellable;
    }

    public boolean isSellable() {
        return sellable;
    }

    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public double getMarkup() {
        return markup;
    }

    public void setMarkup(double markup) {
        this.markup = markup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurStock() {
        return curStock;
    }

    public void setCurStock(int curStock) {
        this.curStock = curStock;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock;
    }

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
