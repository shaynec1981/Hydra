package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.Customer;
import org.controlsfx.control.SearchableComboBox;
import utils.CustomerForInvoice;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class CustomerToInvoiceController implements Initializable {

    @FXML
    private SearchableComboBox<String> customerComboBox;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            CustomersController.getAllCustomers();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for (Customer customer : CustomersController.allCustomers) {
            customerComboBox.getItems().add(customer.getName() + " | " + customer.getPhone() + " | " + customer.getEmail() + " | " +
                    customer.getBirthdate());
        }
    }

    public void buttonSubmitPressed() throws SQLException {
        if (!customerComboBox.getSelectionModel().isEmpty()) {
            String[] stringParts = customerComboBox.getValue().split("\\|");
            String customerEmail = stringParts[2].trim();
            CustomersController.getAllCustomers();
            for (Customer customer : CustomersController.allCustomers) {
                    if (customerEmail.equals(customer.getEmail())) {
                        CustomerForInvoice.setCustomer(customer);
                        closeWindow();
                        return;
                    }
                }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("CRITICAL ERROR");
            alert.setContentText("Something went wrong with customer information.\n" +
                    "Please contact your administrator or the developer.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
            } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ADD CUSTOMER TO INVOICE");
            alert.setContentText("Customer must be selected to add to invoice.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            alert.showAndWait();
        }
    }

    public void cancelButtonPressed() {
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) customerComboBox.getScene().getWindow();
        thisStage.close();
    }





}
