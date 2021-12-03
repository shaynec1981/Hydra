package models;

import utils.DBQuery;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static models.Main.conn;

public class Product {

    private String sku, name, description, category;
    private BigDecimal cost;
    private double markup;
    private int id;
    private int possibleStock;

    public Product (int id, String sku, String name, String description, String category, BigDecimal cost, double markup){
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.cost = cost;
        this.markup = markup;
    }

    public int getPossibleStock() throws SQLException {
        setPossibleStock();
        return possibleStock;
    }

    public void setPossibleStock() throws SQLException {
        ArrayList<String> partNames = new ArrayList<>();
        ArrayList<Double> partAmounts = new ArrayList<>();
        ArrayList<String> partMeasurements = new ArrayList<>();
        String getPartsStatement = "SELECT Item_Name, Item_Amount, Item_Amount_Measurement FROM hydra_product_items" +
                " WHERE Product_ID = '" + getId() + "'";
        DBQuery.setPreparedStatement(conn, getPartsStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            partNames.add(rs.getString("Item_Name"));
            partAmounts.add(Double.parseDouble(rs.getString("Item_Amount")));
            partMeasurements.add(rs.getString("Item_Amount_Measurement"));
        }
        this.possibleStock = 0;
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


}
