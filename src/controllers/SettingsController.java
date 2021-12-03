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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Main;
import models.User;
import utils.DBQuery;
import utils.SettingsModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;

import static models.Main.*;

public class SettingsController implements Initializable {

    public Label salesTaxLabel;
    public TextField salesTaxTextField;
    public Label salesTaxPercentLabel;
    public Label dashboardImageLabel;
    public Label dashboardImageSubtextLabel;
    public Button dashboardImageButton;
    public ImageView thumbnailImage;
    public VBox userManagementGroup;
    public VBox programSettingsGroup;
    @FXML
    private TableColumn<String, String> userIDColumn, userNameColumn, createdColumn, roleColumn;
    @FXML
    private TableView<User> tableViewUsers;
    @FXML
    private TextField textUser;
    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName, labelUser;
    @FXML
    private StackPane rootStackPane, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton, buttonSearchUser,
    buttonAddUser, buttonEditUser, buttonDeleteUser;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    private final MenuItem selectedCell = new MenuItem("Copy");

    private int old_r = -1;
    final ClipboardContent content = new ClipboardContent();

    public static final ObservableList<User> userData = FXCollections.observableArrayList();
    public static final ArrayList<User> allUsers = new ArrayList<>();

    private final FileChooser dashboardImageChooser = new FileChooser();
    private File dashboardImageFile;
    private Image dashboardImage;
    private final Image dashboardImageDefault = new Image("/images/hydraBG.png");

    public static final ObservableList<String> privilegeList = FXCollections.observableArrayList("Admin", "Manager", "Associate");

    private final UnaryOperator<TextFormatter.Change> taxFormatter = markupChange -> {
        if (salesTaxTextField.getText().length() < 6) {
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
                    if (salesTaxTextField.getText().length() >= 4 || salesTaxTextField.getText().length() == 0) {
                        markupChange.setText("");
                    }
                }
            }
        } else {
            markupChange.setText("");
        }
        return markupChange;
    };

    public void initialize(URL location, ResourceBundle resources) {
        salesTaxTextField.setTextFormatter(new TextFormatter<>(taxFormatter));
        thumbnailImage.setImage(SettingsModule.getDashboardBackgroundImage());
        salesTaxTextField.setText(String.valueOf(SettingsModule.getSalesTax()));
        priStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), this::copyDataFromCell);
        selectedCell.setOnAction(actionEvent -> copyDataFromCell());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(selectedCell);
        tableViewUsers.setContextMenu(menu);
        tableViewUsers.getSelectionModel().setCellSelectionEnabled(true);

        userIDColumn.prefWidthProperty().bind(tableViewUsers.widthProperty().divide(4));
        userNameColumn.prefWidthProperty().bind(tableViewUsers.widthProperty().divide(4));
        createdColumn.prefWidthProperty().bind(tableViewUsers.widthProperty().divide(4));
        roleColumn.prefWidthProperty().bind(tableViewUsers.widthProperty().divide(4));
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
        ObservableList<TablePosition> posList = tableViewUsers.getSelectionModel().getSelectedCells();
        StringBuilder clipboardString = new StringBuilder();
        for (TablePosition p : posList) {
            int r = p.getRow();
            int c = p.getColumn();
            Object cell = tableViewUsers.getColumns().get(c).getCellData(r);
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

    public void tbUserManagePressed() {
        programSettingsGroup.setDisable(true);
        programSettingsGroup.setVisible(false);
        userManagementGroup.setDisable(false);
        userManagementGroup.setVisible(true);
        if (privilege > 2) {
            hideAdminOptions();
            hideManagerOptions();
        }
        if (privilege == 2) {
            hideAdminOptions();
        }
    }

    public void tbProgramSettingsPressed() {
        userManagementGroup.setDisable(true);
        userManagementGroup.setVisible(false);
        programSettingsGroup.setDisable(false);
        programSettingsGroup.setVisible(true);
        if (privilege > 2) {
            hideAdminOptions();
            hideManagerOptions();
        }
        if (privilege == 2) {
            hideAdminOptions();
        }
    }

    private void hideAdminOptions() {
        // Hide anything higher than role 1
    }

    private void hideManagerOptions() {
        buttonAddUser.setDisable(true);
        buttonAddUser.setVisible(false);
        buttonEditUser.setDisable(true);
        buttonEditUser.setVisible(false);
        buttonDeleteUser.setDisable(true);
        buttonDeleteUser.setVisible(false);
    }

    public void buttonSearchUserPressed() throws SQLException {
        userData.clear();
        getAllUsers();
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("created"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Filter users by search string
        if (!textUser.getText().isEmpty()) {
            for (User check : allUsers) {
                String allProperties = check.getUserID() + " " + check.getUserName() + " " + check.getRole();
                if (allProperties.toLowerCase(Locale.ROOT).contains(textUser.getText().toLowerCase(Locale.ROOT))) {
                    userData.add(check);
                }
            }
        } else {
            userData.addAll(allUsers);
        }

        // Add relevant data to table.
        tableViewUsers.setItems(userData);
    }

    public void buttonAddUserPressed() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/addUser.fxml")));
        Stage addUserStage = new Stage();
        addUserStage.setTitle("Add User");
        addUserStage.setScene(new Scene(root, 800, 600));
        addUserStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        addUserStage.setResizable(false);
        addUserStage.initModality(Modality.APPLICATION_MODAL);
        addUserStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        addUserStage.show();
    }

    public void buttonEditUserPressed() throws IOException, SQLException {
        if (tableViewUsers.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT USER");
            alert.setContentText("User must be selected to edit!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("../views/editUser.fxml")));
        Parent root = loader.load();
        Stage editUserStage = new Stage();
        editUserStage.setTitle("Edit User");
        editUserStage.setScene(new Scene(root, 800, 600));
        editUserStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        editUserStage.setResizable(false);
        EditUserController controller = loader.getController();
        controller.initData(tableViewUsers.getSelectionModel().getSelectedItem().getUserName());
        editUserStage.initModality(Modality.APPLICATION_MODAL);
        editUserStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        editUserStage.showAndWait();
        buttonSearchUserPressed();
    }

    public void buttonDeleteUserPressed() throws SQLException {
        if (tableViewUsers.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DELETE USER");
            alert.setContentText("User must be selected to delete!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            return;
        }
        String user = tableViewUsers.getSelectionModel().getSelectedItem().getUserName();
        if (!user.equals(Main.currentUser)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("DELETE USER");
            alert.setContentText("Are you sure you want to delete " + user + "?" + "\nThis action cannot be undone!");
            DialogPane dialogPane = alert.getDialogPane();
            ((Button) dialogPane.lookupButton(ButtonType.OK)).setText("Yes");
            ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("No");
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElseThrow() == ButtonType.OK) {
                String deleteStatement = "DELETE FROM hydra_users WHERE User_Name = '" + user + "'";
                DBQuery.setPreparedStatement(conn, deleteStatement);
                PreparedStatement ps = DBQuery.getPreparedStatement();
                ps.execute();
            }
            buttonSearchUserPressed();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DELETE USER");
            alert.setContentText("You cannot delete your own profile!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public static void getAllUsers() throws SQLException {
        allUsers.clear();
        String searchStatement = "SELECT * FROM hydra_users";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while(rs.next()) {
            allUsers.add(new User(rs.getString("User_ID"),
                    rs.getString("User_Name"),
                    rs.getString("Date_Created").substring(0, 10),
                    rs.getString("Privilege"))
            );
        }
    }

    public void dashboardImageChooserButterPressed() {
        dashboardImageChooser.setTitle("Select Image");
        dashboardImageChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.bmp", "*.gif", "*.jpeg", "*.jpg",
                "*.png"));
        dashboardImageFile = dashboardImageChooser.showOpenDialog(priStage);
        if (dashboardImageFile != null) {
            thumbnailImage.setImage(new Image(dashboardImageFile.toURI().toString()));
            dashboardImage = new Image(dashboardImageFile.toURI().toString());
        }
    }

    public void saveSettingsButtonPressed() {
        SettingsModule.setSalesTax(Double.parseDouble(salesTaxTextField.getText()));
        SettingsModule.setDashboardBackgroundImage(dashboardImage);
        SettingsModule.userPreferences.putDouble("SALES_TAX", Double.parseDouble(salesTaxTextField.getText()));
        if (dashboardImageFile == null) {
            SettingsModule.userPreferences.put("DASHBOARD_IMAGE", "/images/hydraBG.png");
        } else {
            SettingsModule.userPreferences.put("DASHBOARD_IMAGE", dashboardImageFile.toURI().toString());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("SAVE SETTINGS");
        alert.setContentText("Settings saved!");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        alert.showAndWait();
    }

    public void defaultButtonPressed() {
        salesTaxTextField.setText(String.valueOf(SettingsModule.getSalesTax()));
        dashboardImage = dashboardImageDefault;
        thumbnailImage.setImage(dashboardImage);
    }
}
