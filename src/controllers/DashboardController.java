package controllers;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.fxml.Initializable;
import utils.DBConnection;
import utils.DBQuery;
import utils.SettingsModule;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import static models.Main.*;

public class DashboardController implements Initializable {

    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName;
    @FXML
    private TextField textUser;
    @FXML
    private PasswordField textPassword;
    @FXML
    private StackPane rootStackPane, stackPaneLoginWindow, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    public void initialize(URL location, ResourceBundle resources) {
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
        textUser.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textUser.setStyle("-fx-effect: null; -fx-background-color: white;");
            }
        });
        textPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textPassword.setStyle("-fx-effect: null; -fx-background-color: white;");
            }
        });
        setBackground();
    }

    public void setBackground() {
        imageBackground.setImage(SettingsModule.getDashboardBackgroundImage());
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

    @FXML
    private void buttonLogInPressed() throws SQLException {
        if(!textUser.getText().isEmpty() && !textPassword.getText().isEmpty()) {
            String statement = "SELECT * FROM hydra_users";
            DBQuery.setPreparedStatement(conn, statement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            try {
                ps.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                conn = DBConnection.startConnection();
                buttonLogInPressed();
            }
            ResultSet rs = ps.getResultSet();
            while(rs.next()) {
                String username = rs.getString("User_Name");
                if (username.equals(textUser.getText())) {
                    String password = rs.getString("Password");
                    if (password.equals(textPassword.getText())) {
                        // Successful log in.
                        System.out.println("Log in successful");
                        currentUser = textUser.getText();
                        String privStatement = "SELECT Privilege FROM hydra_users WHERE User_Name = '" + currentUser + "'";
                        DBQuery.setPreparedStatement(conn, privStatement);
                        PreparedStatement privPS = DBQuery.getPreparedStatement();
                        privPS.execute();
                        ResultSet privRS = privPS.getResultSet();
                        while (privRS.next()) {
                            privilege = Integer.parseInt(rs.getString("Privilege"));
                        }
                        stackPaneMenu.setDisable(false);
                        paneCurrentUser.setDisable(false);
                        paneCurrentUser.setOpacity(1.0);
                        labelUserName.setText(currentUser);
                        stackPaneLoginWindow.setDisable(true);
                        stackPaneLoginWindow.setVisible(false);
                        return;
                    } else {
                        // Password incorrect.
                        textPassword.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.6), 10, 0.75, 0, 0);" +
                                "-fx-background-color: red;");
                    }
                } else {
                    // User name incorrect.
                    textUser.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.6), 10, 0.75, 0, 0);" +
                            "-fx-background-color: red;");
                }
            }
        } else {
            // Username or password fields empty.
            System.out.println("Either username or password blank");
        }
    }

    @FXML
    private void ButtonClearPressed() {
        textUser.clear();
        textPassword.clear();
        textUser.requestFocus();
    }

    private void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        menuButton.getScene().setRoot(pane);
    }

    @FXML
    private void buttonLogOutPressed() {
        stackPaneMenu.setDisable(true);
        paneCurrentUser.setDisable(true);
        paneCurrentUser.setOpacity(0.3);
        labelUserName.setText("None");
        textUser.clear();
        textPassword.clear();
        textUser.setStyle("-fx-effect: null; -fx-background-color: white;");
        textPassword.setStyle("-fx-effect: null; -fx-background-color: white;");
        stackPaneLoginWindow.setDisable(false);
        stackPaneLoginWindow.setVisible(true);
    }

}
