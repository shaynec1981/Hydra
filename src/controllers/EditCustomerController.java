package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utils.DBQuery;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static models.Main.conn;

public class EditCustomerController implements Initializable {

    @FXML
    private TextField name, email, phone;
    @FXML
    private DatePicker birthdate;
    @FXML
    private ImageView imageBackground;
    @FXML
    private StackPane rootStackPane;

    private String customerEmailToUpdate;
    private String customerPhoneToUpdate;

    public void initialize(URL location, ResourceBundle resources) {
        birthdate.getEditor().setDisable(true);
        birthdate.getEditor().setOpacity(1);
        imageBackground.fitWidthProperty().bind(rootStackPane.widthProperty());
        imageBackground.fitHeightProperty().bind(rootStackPane.heightProperty());
        defaultValues();
    }

    public void initData(String loadedEmail, String loadedPhone) throws SQLException {
        customerEmailToUpdate = loadedEmail;
        customerPhoneToUpdate = loadedPhone;
        CustomersController.getAllCustomers();
        String searchStatement = "SELECT * FROM hydra_customers WHERE Customer_Email = '" + customerEmailToUpdate + "'";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement customerEmailCheckPS = DBQuery.getPreparedStatement();
        customerEmailCheckPS.execute();
        ResultSet rs = customerEmailCheckPS.getResultSet();
        rs.next();

        name.setText(rs.getString("Customer_Name"));
        email.setText(rs.getString("Customer_Email"));
        phone.setText(rs.getString("Customer_Phone"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        birthdate.setValue(LocalDate.parse(rs.getString("Customer_Birthdate"), formatter));
    }

    private void defaultValues() {
        name.setText("");
        email.setText("");
        phone.setText("");
        birthdate.setValue(LocalDate.now());
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!name.getText().isEmpty() && !email.getText().isEmpty() && !phone.getText().isEmpty()) {
            for (int i = 0; i < CustomersController.allCustomers.size(); i++) {
                if (email.getText().toLowerCase(Locale.ROOT).equals(CustomersController.allCustomers.get(i).getEmail().toLowerCase(Locale.ROOT))) {
                    if (!email.getText().toLowerCase(Locale.ROOT).equals(customerEmailToUpdate.toLowerCase(Locale.ROOT))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("EDIT CUSTOMER");
                        alert.setContentText("Email already exists!");
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                        alert.showAndWait();
                        return;
                    }
                }
                if (phone.getText().toLowerCase(Locale.ROOT).equals(CustomersController.allCustomers.get(i).getPhone().toLowerCase(Locale.ROOT))) {
                    if (!phone.getText().toLowerCase(Locale.ROOT).equals(customerPhoneToUpdate.toLowerCase(Locale.ROOT))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("EDIT CUSTOMER");
                        alert.setContentText("Phone number already exists!");
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                        Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                        alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                        alert.showAndWait();
                        return;
                    }
                }
            }
            String aName, aEmail, aPhone, aBirthdateFormatted;
            LocalDate aBirthdate;
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            aName = name.getText();
            aEmail = email.getText();
            aPhone = phone.getText();
            aBirthdate = birthdate.getValue();
            aBirthdateFormatted = aBirthdate.format(pattern);

            String updateStatement = "UPDATE hydra_customers SET Customer_Name = '" + aName + "', Customer_Email = '" + aEmail + "', Customer_Phone = '"
                    + aPhone + "', Customer_Birthdate = '" + aBirthdateFormatted + "' WHERE Customer_Email = '" + customerEmailToUpdate + "';";
            DBQuery.setPreparedStatement(conn, updateStatement);
            PreparedStatement updateCustomerPS = DBQuery.getPreparedStatement();
            updateCustomerPS.execute();
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("EDIT CUSTOMER");
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
