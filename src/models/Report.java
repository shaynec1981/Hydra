package models;

public class Report {
    String customerName, invoiceNumber, transactionDate, invoiceGrandTotal, sellableName, sellableSKU, userID, userName,
    userCreated, userCreatedBy, role, grossProfit, grossProfitDay;

    public Report(String customerName, String invoiceNumber, String transactionDate, String invoiceGrandTotal,
                  String sellableName, String sellableSKU, String userID, String userName, String userCreated,
                  String userCreatedBy, String role, String grossProfit, String grossProfitDay) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.transactionDate = transactionDate;
        this.invoiceGrandTotal = invoiceGrandTotal;
        this.sellableName = sellableName;
        this.sellableSKU = sellableSKU;
        this.userID = userID;
        this.userName = userName;
        this.userCreated = userCreated;
        this.userCreatedBy = userCreatedBy;
        this.role = role;
        this.grossProfit = grossProfit;
        this.grossProfitDay = grossProfitDay;
    }

    public String getGrossProfitDay() {
        return grossProfitDay;
    }

    public void setGrossProfitDay(String grossProfitDay) {
        this.grossProfitDay = grossProfitDay;
    }

    public String getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(String grossProfit) {
        this.grossProfit = grossProfit;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    public String getUserCreatedBy() {
        return userCreatedBy;
    }

    public void setUserCreatedBy(String userCreatedBy) {
        this.userCreatedBy = userCreatedBy;
    }

    public String getRole() {
        return switch (role) {
            case "1" -> "Admin";
            case "2" -> "Manager";
            case "3" -> "Associate";
            default -> "Unassigned";
        };
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSellableName() {
        return sellableName;
    }

    public void setSellableName(String sellableName) {
        this.sellableName = sellableName;
    }

    public String getSellableSKU() {
        return sellableSKU;
    }

    public void setSellableSKU(String sellableSKU) {
        this.sellableSKU = sellableSKU;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getInvoiceGrandTotal() {
        return invoiceGrandTotal;
    }

    public void setInvoiceGrandTotal(String invoiceGrandTotal) {
        this.invoiceGrandTotal = invoiceGrandTotal;
    }
}
