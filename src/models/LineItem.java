package models;

import controllers.SalesController;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.math.BigDecimal;

public class LineItem {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal subtotal;
    private Button removeButton;

    public LineItem (String sku, String name, String description, BigDecimal price, BigDecimal tax, BigDecimal discount,
                     BigDecimal subtotal) {
        Image buttonImage = new Image ("/images/removeButton.png");
        ImageView buttonImageView = new ImageView(buttonImage);
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.tax = tax;
        this.discount = discount;
        this.subtotal = subtotal;
        this.removeButton = new Button("");
        removeButton.setStyle("-fx-padding: 0 0 0 0;");
        removeButton.setPrefSize(20, 21);
        removeButton.setGraphic(buttonImageView);
        removeButton.setOnAction(this::buttonAction);
    }

    private void buttonAction(ActionEvent actionEvent) {
        SalesController.lineItemData.remove(this);
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setRemoveButton(Button button) { this.removeButton = button; }

    public Button getRemoveButton() { return removeButton; }
}
