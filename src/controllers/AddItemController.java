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

import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static controllers.InventoryController.getAllItems;
import static models.Main.conn;

public class AddItemController implements Initializable {

    @FXML
    private CheckBox sellableCheck;
    @FXML
    private NoPasteTextField cost, markup, minStock, maxStock, amount;
    @FXML
    private TextField name, price;
    @FXML
    private TextArea description;
    @FXML
    private ComboBox<String> category;
    @FXML
    private ComboBox<String> vendor;
    @FXML
    private ComboBox<String> measurement;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

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
        if (amount.getText().length() < 6) {
            if (amountChange.isReplaced()) {
                if (amountChange.getText().matches("[^0-9]")) {
                    amountChange.setText(amountChange.getControlText().substring(amountChange.getRangeStart(), amountChange.getRangeEnd()));
                }
            }
            if (amountChange.isAdded()) {
                if (amountChange.getControlText().contains(".")) {
                    if (amountChange.getText().matches("[^0-9]")) {
                        amountChange.setText("");
                    }
                } else if (amountChange.getText().matches("[^0-9.]")) {
                    amountChange.setText("");
                }
                if (amountChange.getText().matches("[.]")) {
                    if (amount.getText().length() >= 4 || amount.getText().length() == 0) {
                        amountChange.setText("");
                    }
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
        name.setTextFormatter(new TextFormatter<>(nameFormatter));
        cost.setTextFormatter(new TextFormatter<>(numberFormatter));
        markup.setTextFormatter(new TextFormatter<>(markupFormatter));
        minStock.setTextFormatter(new TextFormatter<>(minStockFormatter));
        maxStock.setTextFormatter(new TextFormatter<>(maxStockFormatter));
        amount.setTextFormatter(new TextFormatter<>(amountFormatter));
        defaultValues();
    }

    private void defaultValues() {
        name.setText("");
        category.setValue("Draft");
        description.setText("");
        vendor.setValue("Southern Wine & Spirits of California");
        amount.setText("");
        measurement.setValue("pound");
        cost.setText("");
        markup.setText("");
        minStock.setText("");
        maxStock.setText("");
        sellableCheck.setSelected(false);
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!name.getText().isEmpty() && !description.getText().isEmpty() && !cost.getText().isEmpty()
                && !markup.getText().isEmpty() && !minStock.getText().isEmpty() && !maxStock.getText().isEmpty() &&
                !amount.getText().isEmpty()) {
            getAllItems();
            for (Item check : InventoryController.allItems) {
                if (check.getName().toLowerCase(Locale.ROOT).equals(name.getText().toLowerCase(Locale.ROOT))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ADD ITEM");
                    alert.setContentText("Item already exists!");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                    return;
                }
            }
            String statement = "INSERT INTO hydra_items (SKU, Name, Description, Category, Cost, Markup, Vendor, Cur_Stock, Min_Stock, Max_Stock, Amount_Per_Unit, Measurement, Sellable)\n" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            DBQuery.setPreparedStatement(conn, statement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            String aSKU, skuPrefix, sku1, sku2, sku3, aName, aDescription, aCategory, aCost, aMarkup, aVendor, aCur_Stock, aMin_Stock, aMax_Stock, aAmount, aMeasurement, aSellable;
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
            aName = name.getText();
            aDescription = description.getText();
            aCategory = category.getValue();
            aCost = cost.getText();
            aMarkup = markup.getText();
            aVendor = vendor.getValue();
            aCur_Stock = "0";
            aMin_Stock = minStock.getText();
            aMax_Stock = maxStock.getText();
            aAmount = amount.getText();
            aMeasurement = measurement.getValue();
            aSellable = sellableCheck.isSelected() ? "1" : "0";
            ps.setString(1, aSKU);
            ps.setString(2, aName);
            ps.setString(3, aDescription);
            ps.setString(4, aCategory);
            ps.setString(5, aCost);
            ps.setString(6, aMarkup);
            ps.setString(7, aVendor);
            ps.setString(8, aCur_Stock);
            ps.setString(9, aMin_Stock);
            ps.setString(10, aMax_Stock);
            ps.setString(11, aAmount);
            ps.setString(12, aMeasurement);
            ps.setString(13, aSellable);
            ps.execute();
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD ITEM");
            alert.setContentText("All fields must be filled in!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    @FXML
    private void ItemPrice() {
        if (!cost.getText().isEmpty() && !markup.getText().isEmpty()) {
            BigDecimal bdCost = new BigDecimal(cost.getText());
            BigDecimal bdMarkup = new BigDecimal(markup.getText());
            BigDecimal bdTotal = bdCost.add(bdCost.multiply(bdMarkup.divide(new BigDecimal(100))));
            Locale locale = Locale.getDefault();
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(locale);
            price.setText(moneyFormat.format(bdTotal));
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
}
