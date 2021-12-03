package models;

import javafx.beans.property.SimpleStringProperty;

public class User {

    private final SimpleStringProperty userID;
    private final SimpleStringProperty userName;
    private final SimpleStringProperty created;
    private final SimpleStringProperty role;

    public User(String uID, String uName, String uCreated, String uRole) {
        this.userID = new SimpleStringProperty(uID);
        this.userName = new SimpleStringProperty(uName);
        this.created = new SimpleStringProperty(uCreated);
        this.role = new SimpleStringProperty(uRole);
    }

    public String getUserID() {
        return userID.get();
    }

    public void setUserID(String userID) {
        this.userID.set(userID);
    }

    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getCreated() {
        return created.get();
    }

    public void setCreated(String created) {
        this.created.set(created);
    }

    public String getRole() {
        return switch (role.get()) {
            case "1" -> "Admin";
            case "2" -> "Manager";
            case "3" -> "Associate";
            default -> "Unassigned";
        };
    }

    public void setRole(String role) {
        this.role.set(role);
    }
}
