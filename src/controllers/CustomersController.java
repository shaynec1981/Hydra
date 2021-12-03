package controllers;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Customer;
import utils.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static models.Main.*;

public class CustomersController implements Initializable{

    public Button deleteCustomersButton;
    @FXML
    private TableView<Customer> tableViewCustomers;
    @FXML
    private TableColumn history;
    @FXML
    private TableColumn birthdate;
    @FXML
    private TableColumn<Object, Object> phone;
    @FXML
    private TableColumn<Object, Object> email;
    @FXML
    private TableColumn<Object, Object> name;
    @FXML
    private TableColumn<Object, Object> id;
    @FXML
    private TextField textField;
    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName;
    @FXML
    private StackPane rootStackPane, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    public final static ObservableList<Customer> customerData = FXCollections.observableArrayList();
    public static final ArrayList<Customer> allCustomers = new ArrayList<>();
    private int old_r = -1;
    final ClipboardContent content = new ClipboardContent();
    private final MenuItem selectedCell = new MenuItem("Copy");

    public void initialize(URL location, ResourceBundle resources) {

        priStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                try {
                    deleteButtonPressed();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        priStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), this::copyDataFromCell);
        selectedCell.setOnAction(actionEvent -> copyDataFromCell());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(selectedCell);
        tableViewCustomers.setContextMenu(menu);
        tableViewCustomers.getSelectionModel().setCellSelectionEnabled(true);

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

        if (privilege > 2) {
            hideAdminOptions();
            hideManagerOptions();
        }
        if (privilege == 2) {
            hideAdminOptions();
        }
    }

  private void copyDataFromCell() {
        ObservableList<TablePosition> posList = tableViewCustomers.getSelectionModel().getSelectedCells();
        StringBuilder clipboardString = new StringBuilder();
        for (TablePosition p : posList) {
            int r = p.getRow();
            int c = p.getColumn();
            Object cell = tableViewCustomers.getColumns().get(c).getCellData(r);
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
    public void reportsButtonClicked() {
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

    public void searchButtonPressed() throws SQLException {

        customerData.clear();
        getAllCustomers();
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        birthdate.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        birthdate.setCellFactory(column -> new TableCell<Customer, LocalDate>() {

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    setText(item.format(pattern));
                }
            }
        });
        history.setCellValueFactory(new PropertyValueFactory<>("transactionHistory"));
        history.setStyle("-fx-alignment: CENTER;");

        // Filter customers by search string
        if (!textField.getText().isEmpty()) {
            for (Customer check : allCustomers) {
                String allProperties = check.getId() + " " + check.getName() + " " + check.getEmail() + " " + check.getPhone() + " "
                        + check.getBirthdate();
                if (allProperties.toLowerCase(Locale.ROOT).contains(textField.getText().toLowerCase(Locale.ROOT))) {
                    customerData.add(check);
                }
            }
        } else {
            customerData.addAll(allCustomers);
        }

        // Add relevant data to table.
        tableViewCustomers.setItems(customerData);
    }

    public void addButtonPressed() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/addCustomer.fxml")));
        Stage addCustomerStage = new Stage();
        addCustomerStage.setTitle("Add Customer");
        addCustomerStage.setScene(new Scene(root, 800, 600));
        addCustomerStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        addCustomerStage.setResizable(false);
        addCustomerStage.initModality(Modality.APPLICATION_MODAL);
        addCustomerStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        addCustomerStage.show();
    }

    public void editButtonPressed() throws IOException, SQLException {
        if (tableViewCustomers.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT CUSTOMER");
            alert.setContentText("Customer must be selected to edit!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        if (tableViewCustomers.getSelectionModel().getSelectedItem().getId().equals("1")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT CUSTOMER");
            alert.setContentText("Cannot edit default customer profile!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("../views/editCustomer.fxml")));
        Parent root = loader.load();
        Stage editCustomerStage = new Stage();
        editCustomerStage.setTitle("Edit Customer");
        editCustomerStage.setScene(new Scene(root, 800, 600));
        editCustomerStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        editCustomerStage.setResizable(false);
        EditCustomerController controller = loader.getController();
        controller.initData(tableViewCustomers.getSelectionModel().getSelectedItem().getEmail(), tableViewCustomers.getSelectionModel().getSelectedItem().getPhone());
        editCustomerStage.initModality(Modality.APPLICATION_MODAL);
        editCustomerStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        editCustomerStage.showAndWait();

        searchButtonPressed();
    }

    public void deleteButtonPressed() throws SQLException {
        if (tableViewCustomers.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DELETE CUSTOMER");
            alert.setContentText("Customer must be selected to delete!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        if (tableViewCustomers.getSelectionModel().getSelectedItem().getId().equals("1")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DELETE CUSTOMER");
            alert.setContentText("You cannot delete the default customer profile!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        String email = tableViewCustomers.getSelectionModel().getSelectedItem().getEmail();
        String name = tableViewCustomers.getSelectionModel().getSelectedItem().getName();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("DELETE CUSTOMER");
        alert.setContentText("Are you sure you want to delete " + name + "?" + "\nThis action cannot be undone!");
        DialogPane dialogPane = alert.getDialogPane();
        ((Button) dialogPane.lookupButton(ButtonType.OK)).setText("Yes");
        ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("No");
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElseThrow() == ButtonType.OK) {
            String deleteStatement = "DELETE FROM hydra_customers WHERE Customer_Email = '" + email + "'";
            DBQuery.setPreparedStatement(conn, deleteStatement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            ps.execute();
            searchButtonPressed();
        }
    }

    public static void getAllCustomers() throws SQLException {
        allCustomers.clear();
        String searchStatement = "SELECT * FROM hydra_customers";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        final DateTimeFormatter birthdateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        while(rs.next()) {
            allCustomers.add(new Customer(rs.getString("Customer_ID"),
                            rs.getString("Customer_Name"),
                            rs.getString("Customer_Email"),
                            rs.getString("Customer_Phone"),
                            LocalDate.parse(rs.getString("Customer_Birthdate"), birthdateFormatter)
                    )
            );
        }
    }

    private void hideAdminOptions() {

    }

    private void hideManagerOptions() {
        deleteCustomersButton.setDisable(true);
        deleteCustomersButton.setVisible(false);
    }
}
