package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.DBQuery;

import java.net.URL;
import java.sql.*;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static models.Main.*;

public class EditUserController implements Initializable{

    @FXML
    private TextField textUsername, textPassword;
    @FXML
    private ComboBox<String> comboBoxPrivilege;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    private String userToUpdate;

    public void initialize(URL location, ResourceBundle resources) {
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        comboBoxPrivilege.setItems(SettingsController.privilegeList);
        comboBoxPrivilege.setValue("Associate");
    }

    public void initData(String loadedUser) throws SQLException {
        userToUpdate = loadedUser;
        SettingsController.getAllUsers();
        String searchStatement = "SELECT * FROM hydra_users WHERE User_Name = '" + loadedUser + "'";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement userNameCheckPS = DBQuery.getPreparedStatement();
        userNameCheckPS.execute();
        ResultSet rs = userNameCheckPS.getResultSet();
        rs.next();
        textUsername.setText(rs.getString("User_Name"));
        textPassword.setText(rs.getString("Password"));
        switch (rs.getString("Privilege")) {
            case "1" -> comboBoxPrivilege.setValue("Admin");
            case "2" -> comboBoxPrivilege.setValue("Manager");
            case "3" -> comboBoxPrivilege.setValue("Associate");
        }
    }

    public void buttonSubmitPressed() throws SQLException {
        String userPriv = "0";
        SettingsController.getAllUsers();
        if (!textUsername.getText().isEmpty() && !textPassword.getText().isEmpty()) {
            for (int i = 0; i < SettingsController.allUsers.size(); i++) {
                if (textUsername.getText().toLowerCase(Locale.ROOT).equals(SettingsController.allUsers.get(i).getUserName().toLowerCase(Locale.ROOT))) {
                    if (!textUsername.getText().toLowerCase(Locale.ROOT).equals(userToUpdate.toLowerCase(Locale.ROOT))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("EDIT USER");
                        alert.setContentText("User name already exists!");
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                        alert.showAndWait();
                        return;
                    }
                }
            }
            switch (comboBoxPrivilege.getValue()) {
                case "Admin" -> userPriv = "1";
                case "Manager" -> userPriv = "2";
                case "Associate" -> userPriv = "3";
            }
            String searchStatement = "UPDATE hydra_users SET User_Name = '" + textUsername.getText() + "', Password = '"
                    + textPassword.getText() + "', Privilege = '" + userPriv + "', Last_Updated = CURRENT_TIMESTAMP, " +
                    "Updated_By = '" + currentUser + "' WHERE User_Name = '" + userToUpdate + "';";
            DBQuery.setPreparedStatement(conn, searchStatement);
            PreparedStatement updateUserPS = DBQuery.getPreparedStatement();
            updateUserPS.execute();
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT USER");
            alert.setContentText("User name and password both must be filled in!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void buttonClearPressed() {
        textUsername.clear();
        textPassword.clear();
        comboBoxPrivilege.setValue("Associate");
        textUsername.requestFocus();
    }

    public void buttonCancelPressed() {
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) textUsername.getScene().getWindow();
        thisStage.close();
    }
}
