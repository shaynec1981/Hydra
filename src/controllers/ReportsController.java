package controllers;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Invoice;
import models.Report;
import org.controlsfx.control.CheckComboBox;
import utils.DBQuery;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static models.Main.*;

public class ReportsController implements Initializable {

    @FXML
    private CheckBox allTimeCheckBox, todayCheckBox, yesterdayCheckBox, thisMonthCheckBox, lastMonthCheckBox, thisYearCheckBox;
    @FXML
    private ComboBox<String> reportCategory1, reportCategory2;
    @FXML
    private CheckComboBox<String> reportFilterBy;
    @FXML
    private DatePicker reportDateFrom, reportDateTo;
    @FXML
    private TableView<Report> reportResultsTable;
    @FXML
    private VBox dateVBox;
    @FXML
    private TableColumn<Report, String> column1, column2, column3, column4, column5, column6, column7, column8;
    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName, invoiceHintLabel;
    @FXML
    private StackPane rootStackPane, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton, openInvoiceButton;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    private final String[] allReportsCategories1 = {"Sales by", "Users by", "Gross Profit by"};
    private final ArrayList<String> allReportsCategories2Sales = new ArrayList<>(Arrays.asList("Customer", "All Sellable"));
    private final ArrayList<String> allReportsCategories2Users = new ArrayList<>(Arrays.asList("Hire Date", "Role"));
    private final ArrayList<String> allReportsCategories2GP = new ArrayList<>(Arrays.asList("Invoice", "Date"));

    private final MenuItem selectedCell = new MenuItem("Copy");
    private int old_r = -1;
    final ClipboardContent content = new ClipboardContent();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    public static String customerInfo = "";

    String dateFrom;
    String dateTo;

    public void initialize(URL location, ResourceBundle resources) {
        priStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), this::copyDataFromCell);
        selectedCell.setOnAction(actionEvent -> copyDataFromCell());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(selectedCell);
        reportResultsTable.setContextMenu(menu);
        reportResultsTable.getSelectionModel().setCellSelectionEnabled(true);

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

        reportDateFrom.getEditor().setDisable(true);
        reportDateFrom.getEditor().setOpacity(1);
        reportDateTo.getEditor().setDisable(true);
        reportDateTo.getEditor().setOpacity(1);
        reportCategory1.getItems().addAll(allReportsCategories1);
        reportCategory2.getItems().add("Select previous category first");
        reportFilterBy.getItems().add("Select previous category first");
        clearReportingButtonPressed();

        reportFilterBy.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded() && reportFilterBy.getItems().size() > 1) {
                    try {
                        if (change.toString().contains("All Customers") || change.toString().contains("All Sellables") || change.toString().contains("All Roles")) {
                            for (int i = 1; i < reportFilterBy.getItems().size(); i++) {
                                reportFilterBy.getCheckModel().clearCheck(i);
                            }
                        } else {
                            if (reportFilterBy.getCheckModel().isChecked(0)) {
                                reportFilterBy.getCheckModel().clearCheck(0);
                            }
                        }
                    } catch(Exception e) {
                        /* Known bug with abstract class CheckBitSetModelBase in ControlsFX. When clicking on label
                        of checkbox item after item in index 0 is already checked. ListChangeListener is fired twice
                        instead of just once causing an IndexOutOfBoundsException.
                         */
                        e.printStackTrace();
                    }
                }
            }
        });
        if (!customerInfo.equals("")) {
            reportCategory1.getSelectionModel().selectFirst();
            determineCategory2();
            reportCategory2.getSelectionModel().selectFirst();
            try {
                determineFilter();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            reportFilterBy.getCheckModel().check(reportFilterBy.getItems().indexOf(customerInfo));
            allTimeCheckBox.setSelected(true);
            try {
                runReportingButtonPressed();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        reportResultsTable.setRowFactory( tv -> {
            TableRow<Report> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Report rowData = row.getItem();
                    try {
                        if (rowData.getInvoiceNumber() != null && !rowData.getInvoiceNumber().equals("")) { // If null, report type ran does not contain invoices.
                            Invoice.print(rowData.getInvoiceNumber());
                        }
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });
    }

    private void determineCategory2() {
        reportCategory2.getItems().clear();
        if (reportCategory1.getSelectionModel().getSelectedItem() != null) {
            switch (reportCategory1.getSelectionModel().getSelectedItem()) {
                case "Sales by" -> {
                    reportCategory2.getItems().addAll(allReportsCategories2Sales);
                    openInvoiceButton.setVisible(false);
                    openInvoiceButton.setDisable(true);
                    invoiceHintLabel.setVisible(false);
                }
                case "Users by" -> {
                    reportCategory2.getItems().addAll(allReportsCategories2Users);
                    openInvoiceButton.setVisible(false);
                    openInvoiceButton.setDisable(true);
                    invoiceHintLabel.setVisible(false);
                }
                case "Gross Profit by" -> {
                    reportCategory2.getItems().addAll(allReportsCategories2GP);
                    openInvoiceButton.setVisible(true);
                    openInvoiceButton.setDisable(false);
                    invoiceHintLabel.setVisible(true);
                }
            }
        }
    }

    private void determineFilter() throws SQLException {
        if (reportFilterBy.getCheckModel().getCheckedItems() != null && reportCategory2.getSelectionModel().getSelectedItem() != null) {
            reportFilterBy.getItems().clear();
            switch (reportCategory2.getSelectionModel().getSelectedItem()) {
                case "Customer" -> {
                    reportFilterBy.getItems().addAll(returnInfo("Customer"));
                    openInvoiceButton.setVisible(true);
                    openInvoiceButton.setDisable(false);
                    invoiceHintLabel.setVisible(true);
                }
                case "Date", "Hire Date" -> {
                    reportFilterBy.setDisable(true);
                    reportFilterBy.setVisible(false);
                    openInvoiceButton.setVisible(false);
                    openInvoiceButton.setDisable(true);
                    invoiceHintLabel.setVisible(false);

                }
                case "Invoice" -> {
                    reportFilterBy.setDisable(true);
                    reportFilterBy.setVisible(false);
                    openInvoiceButton.setVisible(true);
                    openInvoiceButton.setDisable(false);
                    invoiceHintLabel.setVisible(true);
                }
                case "All Sellable" -> {
                    reportFilterBy.getItems().addAll(returnInfo("All Sellable"));
                    openInvoiceButton.setVisible(true);
                    openInvoiceButton.setDisable(false);
                    invoiceHintLabel.setVisible(true);
                }
                case "Role" -> {
                    reportFilterBy.getItems().addAll(new ArrayList<>(Arrays.asList("All Roles", "Admin", "Manager", "Associate")));
                    dateVBox.setVisible(false);
                    dateVBox.setDisable(true);
                    openInvoiceButton.setVisible(false);
                    openInvoiceButton.setDisable(true);
                    invoiceHintLabel.setVisible(false);
                }
            }
        }
    }

    private ArrayList<String> returnInfo(String type) throws SQLException {
        ArrayList<String> filterList = new ArrayList<>();
        switch (type) {
            case "Customer" -> {
                filterList.add("All Customers");
                String searchStatement = "SELECT Customer_Name, Customer_Email FROM hydra_customers";
                DBQuery.setPreparedStatement(conn, searchStatement);
                PreparedStatement ps = DBQuery.getPreparedStatement();
                ps.execute();
                ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    filterList.add(rs.getString("Customer_Name") + " | " + rs.getString("Customer_Email"));
                }
            }
            case "All Sellable" -> {
                filterList.add("All Sellables");
                String searchSellableStatement = "SELECT DISTINCT Name FROM hydra_products UNION SELECT DISTINCT Name FROM hydra_items WHERE Sellable = '1'";
                DBQuery.setPreparedStatement(conn, searchSellableStatement);
                PreparedStatement psSellables = DBQuery.getPreparedStatement();
                psSellables.execute();
                ResultSet rsSellables = psSellables.getResultSet();
                while (rsSellables.next()) {
                    filterList.add(rsSellables.getString("Name"));
                }
            }
        }
        return filterList;
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

    private void menuFoldUp(String scene) {
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
        customerInfo = "";
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        menuButton.getScene().setRoot(pane);
    }


    public void runReportingButtonPressed() throws SQLException {
        if (!reportCategory1.getSelectionModel().isEmpty() && !reportCategory2.getSelectionModel().isEmpty()) {
            if (reportFilterBy.isVisible()) {
                if (!reportFilterBy.getCheckModel().isEmpty()) {

                    // reporting that requires 3rd filter and all are selected

                    String value1 = reportCategory1.getSelectionModel().getSelectedItem();
                    String value2 = reportCategory2.getSelectionModel().getSelectedItem();
                    ObservableList<String> value3 = reportFilterBy.getCheckModel().getCheckedItems();

                    pullReportData(value1, value2, value3);
                } else {
                    // Alert that all filters must have a selection.
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("REPORTS");
                    alert.setContentText("All filters must have a valid selection.");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                }
            } else {

                // reporting that doesn't require 3rd filter and all are selected

                String value1 = reportCategory1.getSelectionModel().getSelectedItem();
                String value2 = reportCategory2.getSelectionModel().getSelectedItem();

                pullReportData(value1, value2, null);
            }
        } else {
            // Alert that all filters must have a selection.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("REPORTS");
            alert.setContentText("All filters must have a valid selection.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    private void pullReportData(String value1, String value2, ObservableList<String> value3) throws SQLException {
        StringBuilder selectStatement = new StringBuilder();
        if (dateVBox.isVisible()) {
            dateFrom = determineFromDate();
            dateTo = determineToDate();
        }
        switch (value1) {
            case "Sales by":
                switch (value2) {
                    case "Customer":
                        if (reportFilterBy.getCheckModel().isChecked(0)) {
                            for (String eachCustomer : reportFilterBy.getItems()){
                                if (!eachCustomer.equals("All Customers")) {
                                    String email = eachCustomer.substring(eachCustomer.indexOf("|") + 2);
                                    selectStatement.append("SELECT * FROM hydra_invoices WHERE Customer_ID = (SELECT Customer_ID FROM hydra_customers WHERE Customer_Email = '").append(email).append("')");
                                    // Add date check
                                    selectStatement.append(" AND Date >= '").append(dateFrom).append("' AND Date <= '").append(dateTo).append("'");
                                    if (!eachCustomer.equals(reportFilterBy.getCheckModel().getItem(reportFilterBy.getItems().size() - 1))) {
                                        selectStatement.append(" UNION ");
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < value3.size(); i++) {
                                String fullItem = reportFilterBy.getCheckModel().getItem(reportFilterBy.getCheckModel().getCheckedIndices().get(i));
                                String email = fullItem.substring(fullItem.indexOf("|") + 2);
                                selectStatement.append("SELECT * FROM hydra_invoices WHERE Customer_ID = (SELECT Customer_ID FROM hydra_customers WHERE Customer_Email = '").append(email).append("')");
                                // Add date check
                                selectStatement.append(" AND Date >= '").append(dateFrom).append("' AND Date <= '").append(dateTo).append("'");
                                if (i != value3.size() - 1) {
                                    selectStatement.append(" UNION ");
                                }
                            }
                        }
                        break;
                    case "All Sellable":
                        if (reportFilterBy.getCheckModel().isChecked(0)) {
                            for (String eachSellable : reportFilterBy.getItems()){
                                if (!eachSellable.equals("All Sellables")) {
                                    String sellableSKU = getSellableSKU(eachSellable);
                                    selectStatement.append("SELECT hydra_invoices.*, ip.* FROM hydra_invoices INNER JOIN " + "(SELECT Invoice_ID FROM hydra_invoice_lineitems WHERE SKU = '").append(sellableSKU).append("') i ").append("ON hydra_invoices.Invoice_ID = i.Invoice_ID INNER JOIN (SELECT SKU, Name FROM ").append("hydra_items UNION SELECT SKU, Name FROM hydra_products) ip ON ip.SKU = '").append(sellableSKU).append("'");
                                    // Add date check
                                    selectStatement.append(" AND Date >= '").append(dateFrom).append("' AND Date <= '").append(dateTo).append("'");
                                    if (!eachSellable.equals(reportFilterBy.getCheckModel().getItem(reportFilterBy.getItems().size() - 1))) {
                                        selectStatement.append(" UNION ");
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < value3.size(); i++) {
                                String sellableSKU = getSellableSKU(reportFilterBy.getCheckModel().getItem(reportFilterBy.getCheckModel().getCheckedIndices().get(i)));
                                selectStatement.append("SELECT hydra_invoices.*, ip.* FROM hydra_invoices INNER JOIN " + "(SELECT Invoice_ID FROM hydra_invoice_lineitems WHERE SKU = '").append(sellableSKU).append("') i ").append("ON hydra_invoices.Invoice_ID = i.Invoice_ID INNER JOIN (SELECT SKU, Name FROM ").append("hydra_items UNION SELECT SKU, Name FROM hydra_products) ip ON ip.SKU = '").append(sellableSKU).append("'");
                                // Add date check
                                selectStatement.append(" AND Date >= '").append(dateFrom).append("' AND Date <= '").append(dateTo).append("'");
                                if (i != value3.size() - 1) {
                                    selectStatement.append(" UNION ");
                                }
                            }
                        }
                        break;
                }
                break;
            case "Users by":
                switch (value2) {
                    case "Hire Date":
                        selectStatement.append("SELECT * FROM hydra_users WHERE ");
                        // Add date check
                        selectStatement.append(" Date_Created >= '").append(dateFrom).append("' AND Date_Created <= '").append(dateTo).append("'");
                        break;
                    case "Role":
                        if (reportFilterBy.getCheckModel().isChecked(0)) {
                            selectStatement.append("SELECT * FROM hydra_users");
                        } else {
                            ObservableList<Integer> allItemsCheck = reportFilterBy.getCheckModel().getCheckedIndices();
                            for (int i = 0; i < value3.size(); i++) {
                                selectStatement.append("SELECT * FROM hydra_users WHERE Privilege = '").append(allItemsCheck.get(i)).append("'");
                                if (i != value3.size() - 1) {
                                    selectStatement.append(" UNION ");
                                }
                            }
                        }
                        break;
            }
                break;
            case "Gross Profit by":
                switch (value2) {
                    case "Invoice", "Date" -> {
                        selectStatement.append("SELECT * FROM hydra_invoices WHERE ");
                        // Add date check
                        selectStatement.append(" Date >= '").append(dateFrom).append("' AND Date <= '").append(dateTo).append("'");
                    }
                }
                break;
        }
        DBQuery.setPreparedStatement(conn, selectStatement.toString());
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();

        setColumns(value1, value2, rs);
    }

    private String getSellableSKU(String sellableName) throws SQLException {
        String selectStatement = "SELECT SKU FROM hydra_items WHERE Name = '" + sellableName + "' UNION " +
                "SELECT SKU FROM hydra_products WHERE Name = '" + sellableName + "'";
        DBQuery.setPreparedStatement(conn, selectStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();
        return rs.getString("SKU");
    }

    private void setColumns(String reportType, String categoryType, ResultSet rs) throws SQLException {
        reportResultsTable.getItems().clear();
        ArrayList<TableColumn<Report, String>> columns = new ArrayList<>(Arrays.asList(column1, column2, column3, column4, column5, column6, column7, column8));
        switch (reportType) {
            case "Sales by":
                switch (categoryType) {
                    case "Customer" -> {
                        String customerName = "";
                        column1.setText("Customer");
                        column2.setText("Invoice");
                        column3.setText("Date");
                        column4.setText("Total");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2") && !column.getId().equals("column3") && !column.getId().equals("column4")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(4));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(4));
                        column3.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(4));
                        column4.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(4));
                        column1.setCellValueFactory(new PropertyValueFactory<>("customerName"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
                        column3.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
                        column4.setCellValueFactory(new PropertyValueFactory<>("invoiceGrandTotal"));
                        while (rs.next()) {
                            String nameSelectStatement = "SELECT Customer_Name, Customer_Email FROM hydra_customers WHERE Customer_ID = '" +
                                    rs.getString("Customer_ID") + "'";
                            DBQuery.setPreparedStatement(conn, nameSelectStatement);
                            PreparedStatement namePS = DBQuery.getPreparedStatement();
                            namePS.execute();
                            ResultSet nameRS = namePS.getResultSet();
                            while (nameRS.next()) {
                                customerName = nameRS.getString("Customer_Name") + " | " + nameRS.getString("Customer_Email");
                            }
                            reportResultsTable.getItems().add(new Report(customerName,
                                    rs.getString("Invoice_ID"),
                                    rs.getString("Date").substring(0, rs.getString("Date").indexOf(" ")),
                                    currencyFormat.format(Double.parseDouble(rs.getString("Grand_Total"))),
                                    "", "", "", "", "", "", "", "",
                                    ""));
                        }
                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column1);
                    }
                    case "All Sellable" -> {
                        column1.setText("Name");
                        column2.setText("SKU");
                        column3.setText("Invoice");
                        column4.setText("Date");
                        column5.setText("Total");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2") &&
                                    !column.getId().equals("column3") && !column.getId().equals("column4") &&
                                    !column.getId().equals("column5")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column3.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column4.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column5.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column1.setCellValueFactory(new PropertyValueFactory<>("sellableName"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("sellableSKU"));
                        column3.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
                        column4.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
                        column5.setCellValueFactory(new PropertyValueFactory<>("invoiceGrandTotal"));
                        while (rs.next()) {
                            reportResultsTable.getItems().add(new Report(
                                    "",
                                    rs.getString("Invoice_ID"),
                                    rs.getString("Date").substring(0, rs.getString("Date").indexOf(" ")),
                                    currencyFormat.format(Double.parseDouble(rs.getString("Grand_Total"))),
                                    rs.getString("Name"),
                                    rs.getString("SKU"),
                                    "", "", "", "", "", "", ""));
                        }
                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column1);
                    }
                }
            break;
            case "Users by":
                switch (categoryType) {
                    case "Hire Date" -> {
                        column1.setText("User ID");
                        column2.setText("User Name");
                        column3.setText("Date Hired");
                        column4.setText("Profile Create By");
                        column5.setText("Role");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2") &&
                                    !column.getId().equals("column3") && !column.getId().equals("column4") &&
                                    !column.getId().equals("column5")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column3.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column4.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column5.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(5));
                        column1.setCellValueFactory(new PropertyValueFactory<>("userID"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("userName"));
                        column3.setCellValueFactory(new PropertyValueFactory<>("userCreated"));
                        column4.setCellValueFactory(new PropertyValueFactory<>("userCreatedBy"));
                        column5.setCellValueFactory(new PropertyValueFactory<>("role"));
                        while (rs.next()) {
                            reportResultsTable.getItems().add(new Report(
                                    "", "", "", "", "",
                                    "",
                                    rs.getString("User_ID"),
                                    rs.getString("User_Name"),
                                    rs.getString("Date_Created").substring(0, rs.getString("Date_Created").indexOf(" ")),
                                    rs.getString("Created_By"),
                                    rs.getString("Privilege"),
                                    "",
                                    ""
                            ));
                        }
                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column3);
                    }
                    case "Role" -> {
                        column1.setText("User ID");
                        column2.setText("User Name");
                        column3.setText("Role");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2") &&
                                    !column.getId().equals("column3")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column3.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column1.setCellValueFactory(new PropertyValueFactory<>("userID"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("userName"));
                        column3.setCellValueFactory(new PropertyValueFactory<>("role"));
                        while (rs.next()) {
                            reportResultsTable.getItems().add(new Report(
                                    "", "", "", "", "",
                                    "",
                                    rs.getString("User_ID"),
                                    rs.getString("User_Name"),
                                    rs.getString("Date_Created").substring(0, rs.getString("Date_Created").indexOf(" ")),
                                    rs.getString("Created_By"),
                                    rs.getString("Privilege"),
                                    "",
                                    ""
                            ));
                        }
                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column3);
                    }
                }
                break;
            case "Gross Profit by":
                switch (categoryType) {
                    case "Invoice":
                        String invoiceGP;
                        column1.setText("Invoice");
                        column2.setText("Gross Profit");
                        column3.setText("Transaction Date");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2") &&
                                    !column.getId().equals("column3")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column3.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(3));
                        column1.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("grossProfit"));
                        column3.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
                        while (rs.next()) {
                            invoiceGP = getInvoiceGP(rs.getString("Invoice_ID"), rs.getString("Items_Total"));
                            reportResultsTable.getItems().add(new Report(
                                    "", rs.getString("Invoice_ID"),
                                    rs.getString("Date").substring(0, rs.getString("Date").indexOf(" ")),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    invoiceGP,
                                    ""
                            ));
                        }
                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column1);
                        break;
                    case "Date":
                        BigDecimal dateGP = new BigDecimal("0");
                        String date = "1900-01-01";
                        String oldDate = "";
                        column1.setText("Date");
                        column2.setText("Gross Profit");
                        for (TableColumn<Report, String> column : columns) {
                            if (!column.getId().equals("column1") && !column.getId().equals("column2")) {
                                column.setVisible(false);
                            }
                        }
                        column1.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(2));
                        column2.prefWidthProperty().bind(reportResultsTable.widthProperty().divide(2));
                        column1.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
                        column2.setCellValueFactory(new PropertyValueFactory<>("grossProfitDay"));
                        while (rs.next()) {
                            invoiceGP = getInvoiceGP(rs.getString("Invoice_ID"), rs.getString("Items_Total"));
                            if (date.equals("1900-01-01")) {
                                date = rs.getString("Date").substring(0, rs.getString("Date").indexOf(" "));
                            }
                            if (date.equals(rs.getString("Date").substring(0, rs.getString("Date").indexOf(" ")))) {
                                dateGP = dateGP.add(BigDecimal.valueOf(Double.parseDouble(invoiceGP)));
                                if (rs.isLast()) {
                                    reportResultsTable.getItems().add(new Report(
                                            "",
                                            "",
                                            date,
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            currencyFormat.format(Double.parseDouble(dateGP.toString()))
                                    ));
                                }
                            } else {
                                reportResultsTable.getItems().add(new Report(
                                        "",
                                        "",
                                        oldDate,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        currencyFormat.format(Double.parseDouble(dateGP.toString()))
                                ));
                                date = rs.getString("Date").substring(0, rs.getString("Date").indexOf(" "));
                                dateGP = BigDecimal.valueOf(Double.parseDouble(invoiceGP));
                                if (rs.isLast()) {
                                    reportResultsTable.getItems().add(new Report(
                                            "",
                                            "",
                                            date,
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            currencyFormat.format(Double.parseDouble(dateGP.toString()))
                                    ));
                                }
                            }
                            oldDate = date;
                        }

                        reportResultsTable.getSortOrder().clear();
                        reportResultsTable.getSortOrder().add(column1);
                        break;
                }
                break;
        }
    }

    private String getInvoiceGP(String invID, String itemsTotal) throws SQLException {
        BigDecimal itemsTotalBD = new BigDecimal(itemsTotal);
        BigDecimal itemsCost = null;

        String selectStatement = "SELECT SUM(totalCost.Cost) AS TotalCost FROM (SELECT Cost FROM (SELECT SKU, Name, Cost FROM hydra_items UNION SELECT SKU, Name, Cost FROM hydra_products) ip INNER JOIN (SELECT SKU FROM hydra_invoice_lineitems WHERE Invoice_ID = '" + invID + "') skus ON skus.SKU = ip.SKU) totalCost";
        DBQuery.setPreparedStatement(conn, selectStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            if (rs.getString("TotalCost") != null) { //TODO: Fix when Item or Product no longer exists in database
                itemsCost = BigDecimal.valueOf(Double.parseDouble(rs.getString("TotalCost")));
            } else {
                itemsCost = BigDecimal.valueOf(0.00);
            }

        }
        return itemsTotalBD.subtract(itemsCost).toString();
    }

    public String determineFromDate() {
        if (todayCheckBox.isSelected()) {
            return LocalDate.now().toString();
        }
        if (yesterdayCheckBox.isSelected()) {
            return LocalDate.now().minusDays(1).toString();
        }
        if (thisMonthCheckBox.isSelected()) {
            return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        }
        if (lastMonthCheckBox.isSelected()) {
            return LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toString();
        }
        if (thisYearCheckBox.isSelected()) {
            return LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).toString();
        }
        if (allTimeCheckBox.isSelected()) {
            return "1900-01-01";
        }
        return reportDateFrom.getValue().toString();
    }

    public String determineToDate() {
        if (todayCheckBox.isSelected()) {
            return LocalDate.now().plusDays(1).toString();
        }
        if (yesterdayCheckBox.isSelected()) {
            return LocalDate.now().toString();
        }
        if (thisMonthCheckBox.isSelected()) {
            return LocalDate.now().plusDays(1).toString();
        }
        if (lastMonthCheckBox.isSelected()) {
            return LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toString();
        }
        if (thisYearCheckBox.isSelected()) {
            return LocalDate.now().plusDays(1).toString();
        }
        if (allTimeCheckBox.isSelected()) {
            return "2200-01-01";
        }
        return reportDateTo.getValue().plusDays(1).toString();
    }

    public void clearReportingButtonPressed() {
        reportCategory1.getSelectionModel().clearSelection();
        reportCategory2.getSelectionModel().clearSelection();
        reportCategory2.getItems().clear();
        reportCategory2.getItems().add("Select previous category first");
        reportFilterBy.getCheckModel().clearChecks();
        reportFilterBy.getItems().clear();
        reportFilterBy.getItems().add("Select previous category first");
        todayCheckBox.setSelected(false);
        yesterdayCheckBox.setSelected(false);
        thisMonthCheckBox.setSelected(false);
        lastMonthCheckBox.setSelected(false);
        thisYearCheckBox.setSelected(false);
        allTimeCheckBox.setSelected(false);
        reportDateFrom.setDisable(false);
        reportDateFrom.setValue(LocalDate.now());
        reportDateTo.setDisable(false);
        reportDateTo.setValue(LocalDate.now());
        reportResultsTable.getItems().clear();
        openInvoiceButton.setDisable(true);
        openInvoiceButton.setVisible(false);
        invoiceHintLabel.setVisible(false);
    }

    private void checkboxesCheck(String checkbox, boolean selected) {
        if (selected) {
            reportDateFrom.setDisable(true);
            reportDateTo.setDisable(true);
            switch (checkbox) {
                case "today" -> {
                    yesterdayCheckBox.setSelected(false);
                    thisMonthCheckBox.setSelected(false);
                    lastMonthCheckBox.setSelected(false);
                    thisYearCheckBox.setSelected(false);
                    allTimeCheckBox.setSelected(false);
                }
                case "yesterday" -> {
                    todayCheckBox.setSelected(false);
                    thisMonthCheckBox.setSelected(false);
                    lastMonthCheckBox.setSelected(false);
                    thisYearCheckBox.setSelected(false);
                    allTimeCheckBox.setSelected(false);
                }
                case "thisMonth" -> {
                    yesterdayCheckBox.setSelected(false);
                    todayCheckBox.setSelected(false);
                    lastMonthCheckBox.setSelected(false);
                    thisYearCheckBox.setSelected(false);
                    allTimeCheckBox.setSelected(false);
                }
                case "lastMonth" -> {
                    yesterdayCheckBox.setSelected(false);
                    thisMonthCheckBox.setSelected(false);
                    todayCheckBox.setSelected(false);
                    thisYearCheckBox.setSelected(false);
                    allTimeCheckBox.setSelected(false);
                }
                case "thisYear" -> {
                    yesterdayCheckBox.setSelected(false);
                    thisMonthCheckBox.setSelected(false);
                    lastMonthCheckBox.setSelected(false);
                    todayCheckBox.setSelected(false);
                    allTimeCheckBox.setSelected(false);
                }
                case "allTime" -> {
                    yesterdayCheckBox.setSelected(false);
                    thisMonthCheckBox.setSelected(false);
                    lastMonthCheckBox.setSelected(false);
                    thisYearCheckBox.setSelected(false);
                    todayCheckBox.setSelected(false);
                }
            }
        } else {
            reportDateFrom.setDisable(false);
            reportDateTo.setDisable(false);
        }

    }

    public void allTimeCheckBoxAction() {
        checkboxesCheck("allTime", allTimeCheckBox.isSelected());
    }

    public void todayCheckBoxAction() {
        checkboxesCheck("today", todayCheckBox.isSelected());
    }

    public void yesterdayCheckBoxAction() {
        checkboxesCheck("yesterday", yesterdayCheckBox.isSelected());
    }

    public void thisMonthCheckBoxAction() {
        checkboxesCheck("thisMonth", thisMonthCheckBox.isSelected());
    }

    public void lastMonthCheckBoxAction() {
        checkboxesCheck("lastMonth", lastMonthCheckBox.isSelected());
    }

    public void thisYearCheckBoxAction() {
        checkboxesCheck("thisYear", thisYearCheckBox.isSelected());
    }

    public void reportCategory1Changed() {
        reportCategory2.getSelectionModel().clearSelection();
        reportCategory2.getItems().clear();
        hideAfterChange();
        determineCategory2();
    }

    public void reportCategory2Changed() throws SQLException {
        hideAfterChange();
        determineFilter();
    }

    private void hideAfterChange() {
        reportFilterBy.setDisable(false);
        reportFilterBy.setVisible(true);
        reportFilterBy.getCheckModel().clearChecks();
        reportFilterBy.getItems().clear();
        dateVBox.setVisible(true);
        dateVBox.setDisable(false);
        column1.setVisible(true);
        column2.setVisible(true);
        column3.setVisible(true);
        column4.setVisible(true);
        column5.setVisible(true);
        column6.setVisible(true);
        column7.setVisible(true);
        column8.setVisible(true);
    }

    private void copyDataFromCell() {
        ObservableList<TablePosition> posList = reportResultsTable.getSelectionModel().getSelectedCells();
        StringBuilder clipboardString = new StringBuilder();
        for (TablePosition p : posList) {
            int r = p.getRow();
            int c = p.getColumn();
            Object cell = reportResultsTable.getColumns().get(c).getCellData(r);
            if (cell == null) {
                cell = "";
            }
            if (old_r == r) {
                clipboardString.append("\t");
            } else if ( old_r != -1) {
                clipboardString.append("\n");
            }
            clipboardString.append(cell);
            old_r = r;
        }
        content.putString(clipboardString.toString());
    Clipboard.getSystemClipboard().setContent(content);
    }

    public void openInvoiceButtonPressed() throws SQLException, IOException {
        if (reportResultsTable.getSelectionModel().getSelectedItem() != null) {
            if (reportResultsTable.getSelectionModel().getSelectedItem().getInvoiceNumber() != null &&
                    !reportResultsTable.getSelectionModel().getSelectedItem().getInvoiceNumber().equals("")) {
                Invoice.print(reportResultsTable.getSelectionModel().getSelectedItem().getInvoiceNumber());
            }
        }
    }
}


