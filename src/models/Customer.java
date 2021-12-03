package models;

import controllers.ReportsController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class Customer {

    private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthdate;
    private Button transactionHistory;

    public Customer (String id, String name, String email, String phone, LocalDate birthdate) {
        Image buttonImage = new Image ("/images/history.png");
        ImageView buttonImageView = new ImageView(buttonImage);
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
        this.transactionHistory = new Button("");
        transactionHistory.setStyle("-fx-padding: 0 0 0 0;");
        transactionHistory.setPrefSize(30, 30);
        transactionHistory.setGraphic(buttonImageView);
        transactionHistory.setOnAction(this::buttonAction);


    }

    public Button getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(Button transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    private void buttonAction(ActionEvent actionEvent) {
        try {
            ReportsController.customerInfo = getName() + " | " + getEmail();
            Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/reports.fxml")));
            pane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
            Main.priStage.getScene().setRoot(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }
}
