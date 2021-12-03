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
import models.User;
import utils.DBQuery;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static models.Main.conn;
import static models.Main.currentUser;

public class AddUserController implements Initializable{

    @FXML
    private TextField textUsername, textPassword;
    @FXML
    private ComboBox<String> comboBoxPrivilege;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    public void initialize(URL location, ResourceBundle resources) {
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        comboBoxPrivilege.setItems(SettingsController.privilegeList);
        comboBoxPrivilege.setValue("Associate");
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!textUsername.getText().isEmpty() && !textPassword.getText().isEmpty()) {
            String searchStatement = "SELECT * FROM hydra_users";
            DBQuery.setPreparedStatement(conn, searchStatement);
            PreparedStatement userNameCheckPS = DBQuery.getPreparedStatement();
            userNameCheckPS.execute();
            ResultSet rs = userNameCheckPS.getResultSet();
            while(rs.next()) {
                SettingsController.allUsers.add(new User(rs.getString("User_ID"),
                        rs.getString("User_Name"),
                        rs.getString("Date_Created").substring(0, 9),
                        rs.getString("Privilege"))
                );
            }

            for (User check : SettingsController.allUsers) {
                if (check.getUserName().toLowerCase(Locale.ROOT).equals(textUsername.getText().toLowerCase(Locale.ROOT))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ADD USER");
                    alert.setContentText("User name already exists!");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                    return;
                }
            }
            String statement = "INSERT INTO hydra_users (User_Name, Password, Date_Created, Created_By, Last_Updated, Updated_By, Privilege)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            Timestamp timeNow = new Timestamp(System.currentTimeMillis());
            DBQuery.setPreparedStatement(conn, statement);
            PreparedStatement ps = DBQuery.getPreparedStatement();
            String aUsername, aPassword, aCreatedBy, aUpdatedBy, aPrivilege;
            aUsername = textUsername.getText();
            aPassword = textPassword.getText();
            aCreatedBy = currentUser;
            aUpdatedBy = currentUser;
            aPrivilege = "null";
            aPrivilege = switch (comboBoxPrivilege.getValue()) {
                case "Admin" -> "1";
                case "Manager" -> "2";
                case "Associate" -> "3";
                default -> aPrivilege;
            };
            ps.setString(1, aUsername);
            ps.setString(2, aPassword);
            ps.setString(3, String.valueOf(timeNow));
            ps.setString(4, aCreatedBy);
            ps.setString(5, String.valueOf(timeNow));
            ps.setString(6, aUpdatedBy);
            ps.setString(7, aPrivilege);
            ps.execute();
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD USER");
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
