package controllers;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.*;
import org.controlsfx.control.SearchableComboBox;
import utils.CustomerForInvoice;
import utils.DBQuery;
import utils.SettingsModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

import static controllers.InventoryController.allItems;
import static controllers.InventoryController.allProducts;
import static models.Main.conn;
import static models.Main.currentUser;

public class SalesController implements Initializable {

    @FXML
    private TableColumn<LineItem, String> removeButtonColumn;
    @FXML
    private TableView<LineItem> invoiceTableView;
    @FXML
    private TableColumn<Object, Object> descriptionColumn, nameColumn, skuColumn;
    @FXML
    private TableColumn<LineItem, BigDecimal> subTotalColumn, discountColumn, taxColumn, priceColumn;
    @FXML
    private SearchableComboBox<String> addItemComboBox;
    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName, customerNameText, grandTotalText, discountsTotalText, taxesTotalText, itemsTotalText;
    @FXML
    private StackPane rootStackPane, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton, invoiceCustomerButton;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    public final double salesTax = SettingsModule.getSalesTax() / 100;

    public static final ObservableList<LineItem> lineItemData = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    public Customer customer;
    private String thisInvoiceID;

    public void initialize(URL location, ResourceBundle resources) {

        labelUserName.setText(currentUser);
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        ttMainMenu.setShowDelay(Duration.millis(0));
        ttMainMenu.setShowDuration(Duration.INDEFINITE);
        ttMainMenu.setHideDelay(Duration.millis(0));
        ttSales.setShowDelay(Duration.millis(0));
        ttSales.setShowDuration(Duration.INDEFINITE);
        ttSales.setHideDelay(Duration.millis(0));
        ttCustomers.setShowDelay(Duration.millis(0));
        ttCustomers.setShowDuration(Duration.INDEFINITE);
        ttCustomers.setHideDelay(Duration.millis(0));
        ttInventory.setShowDelay(Duration.millis(0));
        ttInventory.setShowDuration(Duration.INDEFINITE);
        ttInventory.setHideDelay(Duration.millis(0));
        ttReports.setShowDelay(Duration.millis(0));
        ttReports.setShowDuration(Duration.INDEFINITE);
        ttReports.setHideDelay(Duration.millis(0));
        ttSettings.setShowDelay(Duration.millis(0));
        ttSettings.setShowDuration(Duration.INDEFINITE);
        ttSettings.setHideDelay(Duration.millis(0));

        skuColumn.prefWidthProperty().bind(invoiceTableView.widthProperty().divide(12));
        nameColumn.prefWidthProperty().bind(invoiceTableView.widthProperty().divide(8));
        descriptionColumn.prefWidthProperty().bind(invoiceTableView.widthProperty().divide(2.9 ));
        removeButtonColumn.prefWidthProperty().bind(invoiceTableView.widthProperty().divide(14));
        try {
            InventoryController.getAllItems();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            InventoryController.getAllProducts();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for (Item item : allItems) {
            if (item.isSellable()) {
                addItemComboBox.getItems().add(item.getName() + " : " + item.getSku());
            }
        }
        for (Product product : allProducts) {
            addItemComboBox.getItems().add(product.getName() + " : " + product.getSku());
        }
        try {
            clearAll();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        setPropertyValues();
        invoiceTableView.setItems(lineItemData);
        lineItemData.addListener((ListChangeListener<LineItem>) change -> determineTotalValues());

        try {
            CustomersController.getAllCustomers();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        CustomerForInvoice.setCustomer(CustomersController.allCustomers.get(0));
        customer = CustomerForInvoice.getCustomer();
        try {
            clearAll();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void setPropertyValues() {

        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(cell -> new TableCell<>() {

            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price));
                }
            }
        });


        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        taxColumn.setCellFactory(cell -> new TableCell<>() {

            @Override
            protected void updateItem(BigDecimal tax, boolean empty) {
                super.updateItem(tax, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(tax));
                }
            }
        });

        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountColumn.setCellFactory(cell -> new TableCell<>() {

            @Override
            protected void updateItem(BigDecimal discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(discount));
                }
            }
        });

        subTotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subTotalColumn.setCellFactory(cell -> new TableCell<>() {

            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(subtotal));
                }
            }
        });

        removeButtonColumn.setCellValueFactory(new PropertyValueFactory<>("removeButton"));
        removeButtonColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void clearAll() throws SQLException {
        itemsTotalText.setText("$0.00");
        taxesTotalText.setText("$0.00");
        discountsTotalText.setText("($0.00)");
        grandTotalText.setText("$0.00");
        invoiceTableView.getItems().clear();
        CustomersController.getAllCustomers();
        CustomerForInvoice.setCustomer(CustomersController.allCustomers.get(0));
        customer = CustomerForInvoice.getCustomer();
        customerNameText.setText(customer.getName());
    }

    @FXML
    private void buttonLogOutPressed() throws IOException {
        stackPaneMenu.setDisable(true);
        paneCurrentUser.setDisable(true);
        paneCurrentUser.setOpacity(0.3);
        labelUserName.setText("None");
        changeScene("../views/dashboard.fxml");
    }

    @FXML
    private void menuButtonClicked() {
        // Menu opening
        menuButton.toFront();
        if ((menuButton.getRotate() / 270) % 2 == 0) {
            menuButton.setDisable(true);
            RotateTransition rotateTransition = new RotateTransition();
            rotateTransition.setDuration(Duration.millis(500));
            rotateTransition.setNode(menuButton);
            rotateTransition.setByAngle(270);
            rotateTransition.setCycleCount(1);
            rotateTransition.setAutoReverse(false);
            rotateTransition.setOnFinished(e -> menuButton.setDisable(false));
            rotateTransition.play();

            salesButton.setVisible(true);
            customersButton.setVisible(true);
            inventoryButton.setVisible(true);
            reportsButton.setVisible(true);
            settingsButton.setVisible(true);

            TranslateTransition animateSalesButton = new TranslateTransition(
                    Duration.seconds(0.5), salesButton
            );
            animateSalesButton.setToY(75);
            animateSalesButton.setOnFinished(e -> {
                salesButton.setDisable(false);
                salesButton.toFront();
            });
            animateSalesButton.play();

            TranslateTransition animateCustomersButton = new TranslateTransition(
                    Duration.seconds(0.5), customersButton
            );
            animateCustomersButton.setToY(150);
            animateCustomersButton.setOnFinished(e -> {
                customersButton.setDisable(false);
                customersButton.toFront();
            });
            animateCustomersButton.play();

            TranslateTransition animateInventoryButton = new TranslateTransition(
                    Duration.seconds(0.5), inventoryButton
            );
            animateInventoryButton.setToY(225);
            animateInventoryButton.setOnFinished(e -> {
                inventoryButton.setDisable(false);
                inventoryButton.toFront();
            });
            animateInventoryButton.play();

            TranslateTransition animateReportsButton = new TranslateTransition(
                    Duration.seconds(0.5), reportsButton
            );
            animateReportsButton.setToY(300);
            animateReportsButton.setOnFinished(e -> {
                reportsButton.setDisable(false);
                reportsButton.toFront();
            });
            animateReportsButton.play();

            TranslateTransition animateSettingsButton = new TranslateTransition(
                    Duration.seconds(0.5), settingsButton
            );
            animateSettingsButton.setToY(375);
            animateSettingsButton.setOnFinished(e -> {
                settingsButton.setDisable(false);
                settingsButton.toFront();
            });
            animateSettingsButton.play();
        } else {
            // Menu closing
            menuFoldUp("none");
        }
    }

    @FXML
    private void salesButtonClicked() {
        menuFoldUp("../views/sales.fxml");
    }

    @FXML
    private void customersButtonClicked() {
        menuFoldUp("../views/customers.fxml");
    }

    @FXML
    private void inventoryButtonClicked() {
        menuFoldUp("../views/inventory.fxml");
    }

    @FXML
    private void reportsButtonClicked() {
        menuFoldUp("../views/reports.fxml");
    }

    @FXML
    private void settingsButtonClicked() {
        menuFoldUp("../views/settings.fxml");
    }

    private void menuFoldUp(String scene){
        menuButton.setDisable(true);
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setDuration(Duration.millis(500));
        rotateTransition.setNode(menuButton);
        rotateTransition.setByAngle(270);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);
        rotateTransition.setOnFinished(e -> {
            menuButton.setDisable(false);
            try {
                if (!scene.contains("none")) changeScene(scene);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        rotateTransition.play();

        salesButton.setDisable(true);
        customersButton.setDisable(true);
        inventoryButton.setDisable(true);
        reportsButton.setDisable(true);
        settingsButton.setDisable(true);

        TranslateTransition animateSalesButton = new TranslateTransition(
                Duration.seconds(0.5), salesButton
        );
        animateSalesButton.setToY(0);
        animateSalesButton.setOnFinished(e -> {
            salesButton.setVisible(false);
            salesButton.setDisable(true);
        });
        animateSalesButton.play();

        TranslateTransition animateCustomersButton = new TranslateTransition(
                Duration.seconds(0.5), customersButton
        );
        animateCustomersButton.setToY(0);
        animateCustomersButton.setOnFinished(e -> {
            customersButton.setVisible(false);
            customersButton.setDisable(true);
        });
        animateCustomersButton.play();

        TranslateTransition animateInventoryButton = new TranslateTransition(
                Duration.seconds(0.5), inventoryButton
        );
        animateInventoryButton.setToY(0);
        animateInventoryButton.setOnFinished(e -> {
            inventoryButton.setVisible(false);
            inventoryButton.setDisable(true);
        });
        animateInventoryButton.play();

        TranslateTransition animateReportsButton = new TranslateTransition(
                Duration.seconds(0.5), reportsButton
        );
        animateReportsButton.setToY(0);
        animateReportsButton.setOnFinished(e -> {
            reportsButton.setVisible(false);
            reportsButton.setDisable(true);
        });
        animateReportsButton.play();

        TranslateTransition animateSettingsButton = new TranslateTransition(
                Duration.seconds(0.5), settingsButton
        );
        animateSettingsButton.setToY(0);
        animateSettingsButton.setOnFinished(e -> {
            settingsButton.setVisible(false);
            settingsButton.setDisable(true);
        });
        animateSettingsButton.play();
    }

    private void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        menuButton.getScene().setRoot(pane);
    }

    public void invoiceCustomerButtonPressed() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/customerToInvoice.fxml")));
        Stage customerToInvoiceStage = new Stage();
        customerToInvoiceStage.setTitle("Add Customer To Invoice");
        customerToInvoiceStage.setScene(new Scene(root, 800, 600));
        customerToInvoiceStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        customerToInvoiceStage.setResizable(false);
        customerToInvoiceStage.initModality(Modality.APPLICATION_MODAL);
        customerToInvoiceStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        customerToInvoiceStage.showAndWait();
        customer = CustomerForInvoice.getCustomer();
        customerNameText.setText(customer.getName());
    }

    public void visualPOSMenuButtonPressed() {
        //TODO: If enough time, create a button based POS menu.
    }

    public void tenderSaleButtonPressed() throws SQLException, IOException {
        if (!invoiceTableView.getItems().isEmpty()) {
            if (beginPayment()) {
                // Payment successful
                createNewInvoice();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("INVOICE");
                alert.setContentText("Transaction successful.");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                Invoice.print(thisInvoiceID);
                clearAll();
            } else {
                // Payment failed
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("INVOICE");
                alert.setContentText("Payment failed!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
            }
        }

    }

    public void cancelSaleButtonPressed() throws SQLException {
        clearAll();
    }

    public void addToInvoiceButtonClicked() {
        if (!addItemComboBox.getSelectionModel().isEmpty()) {

            String item = addItemComboBox.getSelectionModel().getSelectedItem();
            String sku = item.substring(item.indexOf(":") + 2);
            BigDecimal price;
            BigDecimal tax;
            BigDecimal discount;
            BigDecimal subtotal;
            for (Item itemCheck : allItems) {
                if (sku.equals(itemCheck.getSku())) {
                    price = itemCheck.getCost().add(itemCheck.getCost().multiply(new BigDecimal(String.valueOf(itemCheck.getMarkup())).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)));
                    price = price.setScale(2, RoundingMode.HALF_UP);
                    tax = price.multiply(BigDecimal.valueOf(salesTax));
                    tax = tax.setScale(2, RoundingMode.HALF_UP);
                    discount = new BigDecimal("0");
                    discount = discount.setScale(2, RoundingMode.HALF_UP);
                    subtotal = price.add(tax).subtract(discount);
                    subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
                    lineItemData.add(new LineItem(itemCheck.getSku(), itemCheck.getName(), itemCheck.getDescription(),
                            price, tax, discount, subtotal
                    ));
                }
            }
            for (Product productCheck : allProducts) {
                if (sku.equals(productCheck.getSku())) {
                    price = productCheck.getCost().add(productCheck.getCost().multiply(new BigDecimal(String.valueOf(productCheck.getMarkup())).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)));
                    price = price.setScale(2, RoundingMode.HALF_UP);
                    tax = price.multiply(BigDecimal.valueOf(salesTax));
                    tax = tax.setScale(2, RoundingMode.HALF_UP);
                    discount = new BigDecimal("0");
                    discount = discount.setScale(2, RoundingMode.HALF_UP);
                    subtotal = price.add(tax).subtract(discount);
                    subtotal= subtotal.setScale(2, RoundingMode.HALF_UP);
                    lineItemData.add(new LineItem(productCheck.getSku(), productCheck.getName(), productCheck.getDescription(),
                            price, tax, discount, subtotal
                    ));
                }
            }
        }
    }

    private void determineTotalValues() {
        BigDecimal itemsTotal = new BigDecimal("0");
        BigDecimal taxesTotal = new BigDecimal("0");
        BigDecimal discountsTotal = new BigDecimal("0");
        BigDecimal grandTotal = new BigDecimal("0");
        if (lineItemData.size() == 0) {
            itemsTotal = BigDecimal.valueOf(0);
            taxesTotal = BigDecimal.valueOf(0);
            discountsTotal = BigDecimal.valueOf(0);
            grandTotal = BigDecimal.valueOf(0);
        } else {
            for (LineItem itemCheck : lineItemData) {
                itemsTotal = itemsTotal.add(itemCheck.getPrice()).setScale(2, RoundingMode.HALF_UP);
                taxesTotal = taxesTotal.add(itemCheck.getTax()).setScale(2, RoundingMode.HALF_UP);
                discountsTotal = discountsTotal.add(itemCheck.getDiscount()).setScale(2, RoundingMode.HALF_UP);
                grandTotal = grandTotal.add(itemCheck.getSubtotal()).setScale(2, RoundingMode.HALF_UP);
            }
        }
        itemsTotalText.setText(currencyFormat.format(itemsTotal));
        taxesTotalText.setText(currencyFormat.format(taxesTotal));
        discountsTotalText.setText(currencyFormat.format(discountsTotal));
        grandTotalText.setText(currencyFormat.format(grandTotal));
    }

    private boolean beginPayment() {
        // Payment screen popup
        return true;
    }

    private void createNewInvoice() throws SQLException {
        // Create entry into hydra_invoices
        String statement = "INSERT INTO hydra_invoices (Customer_ID, Date, Items_Total, Taxes_Total, Discounts_Total, Grand_Total, Tax_Rate)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        DBQuery.setPreparedStatement(conn, statement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        String aCustomerID, aDate, aItemsTotal, aTaxesTotal, aDiscountsTotal, aGrandTotal, aTaxRate;

        aCustomerID = customer.getId();
        aDate = String.valueOf(new Timestamp(System.currentTimeMillis())).substring(0, 19); // Only keep first 19 characters.
        aItemsTotal = itemsTotalText.getText().substring(1).replaceAll(",", "");
        aTaxesTotal = taxesTotalText.getText().substring(1).replaceAll(",", "");
        aDiscountsTotal = discountsTotalText.getText().substring(1).replaceAll(",", "");
        aGrandTotal = grandTotalText.getText().substring(1).replaceAll(",", "");
        aTaxRate = String.valueOf(SettingsModule.getSalesTax());

        ps.setString(1, aCustomerID);
        ps.setString(2, aDate);
        ps.setString(3, aItemsTotal);
        ps.setString(4, aTaxesTotal);
        ps.setString(5, aDiscountsTotal);
        ps.setString(6, aGrandTotal);
        ps.setString(7, aTaxRate);
        ps.execute();

        // Create entry into hydra_invoice_lineitems for each line item in invoice
        for (LineItem itemCheck : invoiceTableView.getItems()) {
            String lineItemStatement = "INSERT INTO hydra_invoice_lineitems (Invoice_ID, SKU)\n" +
                    "VALUES (?, ?)";
            DBQuery.setPreparedStatement(conn, lineItemStatement);
            PreparedStatement lineItemPS = DBQuery.getPreparedStatement();
            String aInvoiceID = "0", aSKU;

            // Get Invoice ID by looking invoice table matching customer ID and timestamp of current invoice.
            String searchStatement = "SELECT Invoice_ID FROM hydra_invoices WHERE Customer_ID = '" + aCustomerID + "' AND " +
                    "Date = '" + aDate + "'";
            DBQuery.setPreparedStatement(conn, searchStatement);
            PreparedStatement invoiceIDPS = DBQuery.getPreparedStatement();
            invoiceIDPS.execute();
            ResultSet invoiceIDRS = invoiceIDPS.getResultSet();
            while(invoiceIDRS.next()) {
                aInvoiceID = invoiceIDRS.getString("Invoice_ID");
                thisInvoiceID = invoiceIDRS.getString("Invoice_ID");
            }

            aSKU = itemCheck.getSku();

            lineItemPS.setString(1, aInvoiceID);
            lineItemPS.setString(2, aSKU);
            lineItemPS.execute();
        }

    }
}
