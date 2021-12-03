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
import utils.DBQuery;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static models.Main.conn;

public class EditItemController implements Initializable{

    @FXML
    private CheckBox sellableCheck;
    @FXML
    private NoPasteTextField cost, markup, minStock, maxStock, amount;
    @FXML
    private TextField name;
    @FXML
    private ComboBox<String> category, vendor, measurement;
    @FXML
    private TextArea description;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    private String itemToUpdate;

    private final UnaryOperator<TextFormatter.Change> nameFormatter = nameChange -> {
        if (nameChange.getText().matches("['\"]")) {
            nameChange.setText("");
        }
        return nameChange;
    };

    private final UnaryOperator<TextFormatter.Change> numberFormatter = costChange -> {
        if (cost.getText().length() < 10) {
            if (costChange.isReplaced()) {
                if(costChange.getText().matches("[^0-9]")) {
                    costChange.setText(costChange.getControlText().substring(costChange.getRangeStart(), costChange.getRangeEnd()));
                }
            }
            if (costChange.isAdded()) {
                if (costChange.getControlText().contains(".")) {
                    if (costChange.getText().matches("[^0-9]")) {
                        costChange.setText("");
                    }
                } else if (costChange.getText().matches("[^0-9.]")) {
                    costChange.setText("");
                }
                if (costChange.getText().matches("[.]")) {
                    if (cost.getText().length() >= 8 || cost.getText().length() == 0) {
                        costChange.setText("");
                    }
                }
            }
        } else {
            costChange.setText("");
        }

        return costChange;
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

    private final UnaryOperator<TextFormatter.Change> minStockFormatter = minStockChange -> {
        if (minStock.getText().length() < 10) {
            if (minStockChange.isReplaced() || minStockChange.isAdded()) {
                if (minStockChange.getText().matches("[^0-9]")) {
                    minStockChange.setText(minStockChange.getControlText().substring(minStockChange.getRangeStart(), minStockChange.getRangeEnd()));
                }
            }
        } else {
            minStockChange.setText("");
        }
        return minStockChange;
    };

    private final UnaryOperator<TextFormatter.Change> maxStockFormatter = maxStockChange -> {
        if (maxStock.getText().length() < 10) {
            if (maxStockChange.isReplaced() || maxStockChange.isAdded()) {
                if (maxStockChange.getText().matches("[^0-9]")) {
                    maxStockChange.setText(maxStockChange.getControlText().substring(maxStockChange.getRangeStart(), maxStockChange.getRangeEnd()));
                }
            }
        } else {
            maxStockChange.setText("");
        }
        return maxStockChange;
    };

    private final UnaryOperator<TextFormatter.Change> amountFormatter = amountChange -> {
        if (amount.getText().length() < 10) {
            if (amountChange.isReplaced() || amountChange.isAdded()) {
                if (amountChange.getText().matches("[^0-9]")) {
                    amountChange.setText(amountChange.getControlText().substring(amountChange.getRangeStart(), amountChange.getRangeEnd()));
                }
            }
        } else {
            amountChange.setText("");
        }
        return amountChange;
    };

    public void initialize(URL location, ResourceBundle resources) {
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        category.setItems(InventoryController.allItemCategories);
        vendor.setItems(InventoryController.allVendors);
        measurement.setItems(InventoryController.allMeasurements);
        cost.setTextFormatter(new TextFormatter<>(numberFormatter));
        markup.setTextFormatter(new TextFormatter<>(markupFormatter));
        minStock.setTextFormatter(new TextFormatter<>(minStockFormatter));
        maxStock.setTextFormatter(new TextFormatter<>(maxStockFormatter));
        amount.setTextFormatter(new TextFormatter<>(amountFormatter));
        name.setTextFormatter(new TextFormatter<>(nameFormatter));
    }

    public void initData(String loadedItem) throws SQLException {
        itemToUpdate = loadedItem;
        InventoryController.getAllItems();
        String searchStatement = "SELECT * FROM hydra_items WHERE Name = '" + loadedItem + "'";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement itemNameCheckPS = DBQuery.getPreparedStatement();
        itemNameCheckPS.execute();
        ResultSet rs = itemNameCheckPS.getResultSet();
        rs.next();

        name.setText(rs.getString("Name"));
        cost.setText(rs.getString("Cost").replaceFirst("^0+(?!$)", ""));
        markup.setText(rs.getString("Markup").replaceFirst("^0+(?!$)", ""));
        minStock.setText(rs.getString("Min_Stock").replaceFirst("^0+(?!$)", ""));
        maxStock.setText(rs.getString("Max_Stock").replaceFirst("^0+(?!$)", ""));
        category.setValue(rs.getString("Category"));
        vendor.setValue(rs.getString("Vendor"));
        amount.setText(rs.getString("Amount_Per_Unit").replaceFirst("^0+(?!$)", ""));
        measurement.setValue(rs.getString("Measurement"));
        description.setText(rs.getString("Description"));
        sellableCheck.setSelected(rs.getString("Sellable").equals("1"));
    }

    public void buttonSubmitPressed() throws SQLException {
        InventoryController.getAllItems();
        if (!name.getText().isEmpty() && !description.getText().isEmpty() && !cost.getText().isEmpty()
                && !markup.getText().isEmpty() && !minStock.getText().isEmpty() && !maxStock.getText().isEmpty() &&
                !amount.getText().isEmpty()) {
            for (int i = 0; i < InventoryController.allItems.size(); i++) {
                if (name.getText().toLowerCase(Locale.ROOT).equals(InventoryController.allItems.get(i).getName().toLowerCase(Locale.ROOT))) {
                    if (!name.getText().toLowerCase(Locale.ROOT).equals(itemToUpdate.toLowerCase(Locale.ROOT))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("EDIT ITEM");
                        alert.setContentText("Item name already exists!");
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                        alert.showAndWait();
                        return;
                    }
                }
            }
            String aSKU, skuPrefix, sku1, sku2, sku3, aSellable;
            skuPrefix = "IT";
            sku1 = category.getValue().substring(0, 2).toUpperCase(Locale.ROOT);
            if (name.getText().contains(" ")) {
                sku2 = name.getText().substring(0, 1).toUpperCase(Locale.ROOT) + name.getText().split(" ")[1].substring(0, 1).toUpperCase(Locale.ROOT);
            } else {
                sku2 = name.getText().substring(0, 2).toUpperCase(Locale.ROOT);
            }
            sku3 = "0001";
            for (Item item : InventoryController.allItems) {
                if (item.getSku().substring(0, 6).equals(skuPrefix.toUpperCase(Locale.ROOT) + sku1.toUpperCase(Locale.ROOT) + sku2.toUpperCase(Locale.ROOT))) {
                    int i;
                    i = Integer.parseInt(item.getSku().substring(6)) + 1;
                    sku3 = ("0000" + i).substring(String.valueOf(i).length());
                }
            }
            aSKU = skuPrefix + sku1 + sku2 + sku3;
            aSellable = sellableCheck.isSelected() ? "1" : "0";
            String updateStatement = "UPDATE hydra_items SET SKU = '" + aSKU + "', Name = '" + name.getText() + "', Category = '"
                    + category.getValue() + "', Description = '" + description.getText() + "', Vendor = '" +
                    vendor.getValue() + "', Measurement = '" + measurement.getValue() + "', Cost = '" + cost.getText() +
                    "', Markup = '" + markup.getText() + "', Min_Stock = '" + minStock.getText() + "', Max_Stock = '" +
                    maxStock.getText() + "', Amount_Per_Unit = '" + amount.getText() + "', Sellable = '" + aSellable + "' WHERE Name = '" + itemToUpdate + "';";
            DBQuery.setPreparedStatement(conn, updateStatement);
            PreparedStatement updateItemPS = DBQuery.getPreparedStatement();
            updateItemPS.execute();
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT ITEM");
            alert.setContentText("All fields must be filled in!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void buttonClearPressed() {
        name.clear();
        category.setValue("Draft");
        description.clear();
        vendor.setValue("Southern Wine & Spirits of California");
        measurement.setValue("pound");
        cost.clear();
        markup.clear();
        minStock.clear();
        maxStock.clear();
        amount.clear();
    }

    public void buttonCancelPressed() {
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) name.getScene().getWindow();
        thisStage.close();
    }
}
