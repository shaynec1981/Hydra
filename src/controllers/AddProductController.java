package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Item;
import utils.NoPasteTextField;
import models.Product;
import org.controlsfx.control.SearchableComboBox;
import resources.HelperMethods;
import utils.DBQuery;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static controllers.InventoryController.*;
import static models.Main.conn;

public class AddProductController implements Initializable{
    @FXML
    private NoPasteTextField itemAmount, markup;
    @FXML
    private SearchableComboBox<String> availableItems;
    @FXML
    private ListView<String> listViewItemsInProduct;
    @FXML
    private TextField name, cost, price;
    @FXML
    private TextArea description;
    @FXML
    private ComboBox<String> category, itemMeasurement;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    private BigDecimal costTotal = new BigDecimal("0");

    private final UnaryOperator<TextFormatter.Change> descriptionFormatter = descriptionChange -> {
        if (descriptionChange.getText().matches("['\"]")) {
            descriptionChange.setText("");
        }
        return descriptionChange;
    };

    private final UnaryOperator<TextFormatter.Change> markupFormatter = markupChange -> {
        if (markup.getText().length() < 6) {
            if (markupChange.isReplaced()) {
                if (markupChange.getText().matches("[^0-9]")) {
                    markupChange.setText(markupChange.getControlText().substring(markupChange.getRangeStart(), markupChange.getRangeEnd()));
                }
            }
            if (markupChange.isAdded()) {
                if (markupChange.getControlText().contains(".")) {
                    if (markupChange.getText().matches("[^0-9]")) {
                        markupChange.setText("");
                    }
                } else if (markupChange.getText().matches("[^0-9.]")) {
                    markupChange.setText("");
                }
                if (markupChange.getText().matches("[.]")) {
                    if (markup.getText().length() >= 4 || markup.getText().length() == 0) {
                        markupChange.setText("");
                    }
                }
            }
        } else {
            markupChange.setText("");
        }
        return markupChange;
    };

    private final UnaryOperator<TextFormatter.Change> itemAmountFormatter = itemAmountChange -> {
        if (itemAmount.getText().length() < 6) {
            if (itemAmountChange.isReplaced()) {
                if (itemAmountChange.getText().matches("[^0-9]")) {
                    itemAmountChange.setText(itemAmountChange.getControlText().substring(itemAmountChange.getRangeStart(), itemAmountChange.getRangeEnd()));
                }
            }
            if (itemAmountChange.isAdded()) {
                if (itemAmountChange.getControlText().contains(".")) {
                    if (itemAmountChange.getText().matches("[^0-9]")) {
                        itemAmountChange.setText("");
                    }
                } else if (itemAmountChange.getText().matches("[^0-9.]")) {
                    itemAmountChange.setText("");
                }
                if (itemAmountChange.getText().matches("[.]")) {
                    if (itemAmount.getText().length() >= 4 || itemAmount.getText().length() == 0) {
                        itemAmountChange.setText("");
                    }
                }
            }
        } else {
            itemAmountChange.setText("");
        }
        return itemAmountChange;
    };

    public void initialize(URL location, ResourceBundle resources) {
        try {
            InventoryController.getAllItems();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for (Item item : allItems) {
            availableItems.getItems().add(item.getName());
        }

        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        category.setItems(InventoryController.allProductCategories);
        itemMeasurement.setItems(allMeasurements);
        itemAmount.setTextFormatter(new TextFormatter<>(itemAmountFormatter));
        markup.setTextFormatter(new TextFormatter<>(markupFormatter));
        description.setTextFormatter(new TextFormatter<>(descriptionFormatter));
        defaultValues();
    }

    private void defaultValues() {
        name.setText("");
        category.setValue("Draft");
        description.setText("");
        availableItems.getSelectionModel().clearSelection();
        itemAmount.setText("");
        itemMeasurement.getSelectionModel().clearSelection();
        cost.setText("$0.00");
        markup.setText("");
        price.setText("$0.00");
        listViewItemsInProduct.getItems().clear();
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!name.getText().isEmpty() && !description.getText().isEmpty() && !listViewItemsInProduct.getItems().isEmpty()
                && !markup.getText().isEmpty()) {
            getAllProducts();
            for (Product check : allProducts) {
                if (check.getName().toLowerCase(Locale.ROOT).equals(name.getText().toLowerCase(Locale.ROOT))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ADD PRODUCT");
                    alert.setContentText("Product already exists!");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                    return;
                }
            }
            String statement = "INSERT INTO hydra_products (SKU, Name, Description, Category, Cost, Markup)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            DBQuery.setPreparedStatement(conn, statement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            String aSKU, skuPrefix, sku1, sku2, sku3, aName, aDescription, aCategory, aCost, aMarkup;
            skuPrefix = "PR";
            sku1 = category.getValue().substring(0, 2).toUpperCase(Locale.ROOT);
            if (name.getText().contains(" ")) {
                sku2 = name.getText().substring(0, 1).toUpperCase(Locale.ROOT) + name.getText().split(" ")[1].substring(0, 1).toUpperCase(Locale.ROOT);
            } else {
                sku2 = name.getText().substring(0, 2).toUpperCase(Locale.ROOT);
            }
            sku3 = "0001";
            for (Product product : allProducts) {
                if (product.getSku().substring(0, 6).equals(skuPrefix.toUpperCase(Locale.ROOT) + sku1.toUpperCase(Locale.ROOT) + sku2.toUpperCase(Locale.ROOT))) {
                    int i;
                    i = Integer.parseInt(product.getSku().substring(6)) + 1;
                    sku3 = ("0000" + i).substring(String.valueOf(i).length());
                }
            }
            aSKU = skuPrefix + sku1 + sku2 + sku3;
            aName = name.getText();
            aDescription = description.getText();
            aCategory = category.getValue();
            aCost = HelperMethods.removeCharacter(cost.getText().substring(1), ",");
            aMarkup = markup.getText();
            ps.setString(1, aSKU);
            ps.setString(2, aName);
            ps.setString(3, aDescription);
            ps.setString(4, aCategory);
            ps.setString(5, aCost);
            ps.setString(6, aMarkup);
            ps.execute();

            // Add product and item info to hydra_product_items
            // Get Product's ID
            String productID = null;
            String itemID = null;
            String idGetStatement = "SELECT ID FROM hydra_products WHERE Name = '" + aName + "'";
            DBQuery.setPreparedStatement(conn, idGetStatement);
            PreparedStatement getIDPreparedStatement = DBQuery.getPreparedStatement();
            getIDPreparedStatement.execute();
            ResultSet getIDResultSet = getIDPreparedStatement.getResultSet();
            while (getIDResultSet.next()) {
                productID = getIDResultSet.getString("ID");
            }
            for (String item : listViewItemsInProduct.getItems()) {
                String itemName = item.split(",")[0];
                String s = item.substring(item.indexOf(",") + 2);
                String itemMeasurement = s.substring(s.indexOf(" ") + 1);
                double itemAmount = Double.parseDouble(s.split(" ")[0]);
                String itemIDGetStatement = "SELECT ID FROM hydra_items WHERE Name = '" + itemName + "'";
                DBQuery.setPreparedStatement(conn, itemIDGetStatement);
                PreparedStatement getItemIDPreparedStatement = DBQuery.getPreparedStatement();
                getItemIDPreparedStatement.execute();
                ResultSet getItemIDResultSet = getItemIDPreparedStatement.getResultSet();
                while (getItemIDResultSet.next()) {
                    itemID = getItemIDResultSet.getString("ID");
                }

                String insertStatement = "INSERT INTO hydra_product_items (Product_ID, Item_ID, Item_Name, Item_Amount, Item_Amount_Measurement)\n" +
                        "VALUES (?, ?, ?, ?, ?)";
                DBQuery.setPreparedStatement(conn, insertStatement);
                PreparedStatement insertPS = DBQuery.getPreparedStatement();
                insertPS.setString(1, productID);
                insertPS.setString(2, itemID);
                insertPS.setString(3, itemName);
                insertPS.setString(4, String.valueOf(itemAmount));
                insertPS.setString(5, itemMeasurement);
                insertPS.execute();
            }

            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD PRODUCT");
            alert.setContentText("All fields must be completed!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void buttonClearPressed() {
        defaultValues();
        name.requestFocus();
    }

    public void buttonCancelPressed() {
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) name.getScene().getWindow();
        thisStage.close();
    }

    public void addItemPressed() throws SQLException {
        if (availableItems.getValue() != null && itemMeasurement.getValue() != null && !itemAmount.getText().isEmpty()) {
            for (String listItem : listViewItemsInProduct.getItems()) {
                if (listItem.split(",")[0].equals(availableItems.getValue())) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ADD ITEM TO PRODUCT");
                    alert.setContentText("Item already exists as product element.");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                    return;
                }
            }
            BigDecimal itemCost;
            String measurement;
            double fullItemAmount;
            BigDecimal productItemCost = new BigDecimal("0");
            listViewItemsInProduct.getItems().add(availableItems.getValue() + ", " + itemAmount.getText() + " " +
                    itemMeasurement.getValue());
            String searchStatement = "SELECT Cost, Amount_Per_Unit, Measurement FROM hydra_items WHERE Name = '" + availableItems.getValue() + "'";
            DBQuery.setPreparedStatement(conn, searchStatement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            ps.execute();
            ResultSet rs = ps.getResultSet();
            while(rs.next()) {
                fullItemAmount = Double.parseDouble(rs.getString("Amount_Per_Unit"));
                measurement = rs.getString("Measurement");
                itemCost = BigDecimal.valueOf(Double.parseDouble(rs.getString("Cost")));
                if (itemMeasurement.getValue().equals(measurement)) {
                    productItemCost = BigDecimal.valueOf( (itemCost.doubleValue() / fullItemAmount) * Double.parseDouble(itemAmount.getText()) );
                } else {
                    double convertedItemAmount = HelperMethods.measurementConverter(itemAmount.getText(), itemMeasurement.getValue(), measurement);
                    productItemCost = BigDecimal.valueOf( (itemCost.doubleValue() / fullItemAmount) * convertedItemAmount );

                }
            }
            costTotal = costTotal.add(productItemCost);
            Locale locale = Locale.getDefault();
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(locale);
            cost.setText(moneyFormat.format(costTotal));
            ProductPrice();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD PRODUCT");
            alert.setContentText("Item & measurement must be selected and amount filled in!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void deleteItemPressed() throws SQLException {
        if (listViewItemsInProduct.getSelectionModel().getSelectedItem() != null) {
            BigDecimal itemCost;
            String measurement;
            double fullItemAmount;
            String itemInList = listViewItemsInProduct.getSelectionModel().getSelectedItem();
            String itemName = itemInList.split(",")[0];
            String s = itemInList.substring(itemInList.indexOf(",") + 2);
            String itemMeasurement = s.substring(s.indexOf(" ") + 1);
            double itemAmount = Double.parseDouble(s.split(" ")[0]);

            BigDecimal productItemCost = new BigDecimal("0");
            String searchStatement = "SELECT Cost, Amount_Per_Unit, Measurement FROM hydra_items WHERE Name = '" + itemName + "'";
            DBQuery.setPreparedStatement(conn, searchStatement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            ps.execute();
            ResultSet rs = ps.getResultSet();
            while(rs.next()) {
                fullItemAmount = Double.parseDouble(rs.getString("Amount_Per_Unit"));
                measurement = rs.getString("Measurement");
                itemCost = BigDecimal.valueOf(Double.parseDouble(rs.getString("Cost")));
                if (measurement.equals(itemMeasurement)) {
                    productItemCost = BigDecimal.valueOf( (itemCost.doubleValue() / fullItemAmount) * itemAmount );
                } else {
                    double convertedItemAmount = HelperMethods.measurementConverter(String.valueOf(itemAmount), itemMeasurement, measurement);
                    productItemCost = BigDecimal.valueOf( (itemCost.doubleValue() / fullItemAmount) * convertedItemAmount );
                }

            }
            costTotal = costTotal.subtract(productItemCost);
            Locale locale = Locale.getDefault();
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(locale);
            cost.setText(moneyFormat.format(costTotal));
            listViewItemsInProduct.getItems().remove(listViewItemsInProduct.getSelectionModel().getSelectedItem());
            ProductPrice();
        }
    }

    @FXML
    private void ProductPrice() {
        if (!cost.getText().isEmpty() && !markup.getText().isEmpty()) {
            String tempCostString = cost.getText().substring(1);
            tempCostString = HelperMethods.removeCharacter(tempCostString, ",");
            BigDecimal bdCost = new BigDecimal(tempCostString);
            BigDecimal bdMarkup = new BigDecimal(markup.getText());
            BigDecimal bdTotal = bdCost.add(bdCost.multiply(bdMarkup.divide(new BigDecimal(100))));
            Locale locale = Locale.getDefault();
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(locale);
            price.setText(moneyFormat.format(bdTotal));
        }
    }
}
