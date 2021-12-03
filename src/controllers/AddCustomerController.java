package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Customer;
import utils.DBQuery;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static models.Main.conn;

public class AddCustomerController implements Initializable {

    @FXML
    private TextField name, email, phone;
    @FXML
    private DatePicker birthdate;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    private final Robot robot = new Robot();

    public AddCustomerController() throws AWTException {
    }

    public void initialize(URL location, ResourceBundle resources) {

        birthdate.getEditor().setDisable(true);
        birthdate.getEditor().setOpacity(1);
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        defaultValues();
    }

    private void defaultValues() {
        name.setText("");
        email.setText("");
        phone.setText("");
        birthdate.setValue(LocalDate.now());
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!name.getText().isEmpty() && !email.getText().isEmpty() && !phone.getText().isEmpty()) {
            if (email.getText().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")) {
                if (phone.getText().matches("\\d{3}-\\d{3}-\\d{4}")) {
                    CustomersController.getAllCustomers();
                    for (Customer check : CustomersController.allCustomers) {
                        if (check.getEmail().toLowerCase(Locale.ROOT).equals(email.getText().toLowerCase(Locale.ROOT))) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ADD CUSTOMER");
                            alert.setContentText("Email already exists!");
                            DialogPane dialogPane = alert.getDialogPane();
                            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                            alert.showAndWait();
                            return;
                        } else if (check.getPhone().equals(phone.getText())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ADD CUSTOMER");
                            alert.setContentText("Phone number already exists!");
                            DialogPane dialogPane = alert.getDialogPane();
                            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                            alert.showAndWait();
                            return;
                        }
                    }
                    robot.keyPress(KeyEvent.VK_ENTER);
                    String statement = "INSERT INTO hydra_customers (Customer_Name, Customer_Email, Customer_Phone, Customer_Birthdate)" +
                            "VALUES (?, ?, ?, ?)";
                    DBQuery.setPreparedStatement(conn, statement);
                    PreparedStatement ps = DBQuery.getPreparedStatement();
                    String aName, aEmail, aPhone, aBirthdateFormatted;
                    LocalDate aBirthdate;
                    DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    aName = name.getText();
                    aEmail = email.getText();
                    aPhone = phone.getText();
                    aBirthdate = birthdate.getValue();
                    aBirthdateFormatted = aBirthdate.format(pattern);
                    ps.setString(1, aName);
                    ps.setString(2, aEmail);
                    ps.setString(3, aPhone);
                    ps.setString(4, aBirthdateFormatted);
                    ps.execute();
                    closeWindow();
                }  else {
                    //Phone doesn't match
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ADD CUSTOMER");
                    alert.setContentText("Invalid phone number!");
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                    Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                    alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                    alert.showAndWait();
                }
            }  else {
                //Email doesn't match
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ADD CUSTOMER");
                alert.setContentText("Invalid email address!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD CUSTOMER");
            alert.setContentText("All fields must be filled in!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void clearButtonPressed() {
        defaultValues();
        name.requestFocus();
    }

    public void cancelButtonPressed() {
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) name.getScene().getWindow();
        thisStage.close();
    }





}
