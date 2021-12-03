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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Item;
import models.Product;
import utils.DBQuery;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static models.Main.*;

public class InventoryController implements Initializable{

    @FXML
    private TableView<Product> tableViewProducts;
    @FXML
    private TableView<Item> tableViewItems;
    @FXML
    private TextField textField;
    @FXML
    private TableColumn<String, String> sku, name, description, category, cost, markup, stock, skuProduct, nameProduct,
            descriptionProduct, categoryProduct, costProduct, markupProduct, stockProduct;
    @FXML
    private ImageView imageBackground;
    @FXML
    private Label labelUserName, variableTextFieldLabel;
    @FXML
    private StackPane rootStackPane, stackPaneMenu, paneCurrentUser;
    @FXML
    private Button menuButton, salesButton, customersButton, inventoryButton, reportsButton, settingsButton, buttonDelete, buttonEdit, buttonAdd,
            buttonSearch;
    @FXML
    private Tooltip ttMainMenu, ttSales, ttCustomers, ttInventory, ttReports, ttSettings;

    private String invCat;
    private final MenuItem selectedCell = new MenuItem("Copy");
    public static final ObservableList<String> allItemCategories = FXCollections.observableArrayList("Draft", "Bottle", "Food", "Ingredient");
    public static final ObservableList<String> allProductCategories = FXCollections.observableArrayList("Draft", "Cocktail", "Food");
    public static final ObservableList<String> allVendors = FXCollections.observableArrayList("Southern Wine & Spirits of California",
            "Henry Wine Group", "Walmart", "Sysco" );
    public static final ObservableList<String> allMeasurements = FXCollections.observableArrayList("gallon", "quart",
            "pint", "fl oz", "cup", "tsp", "tbsp", "L", "mL", "pound", "oz", "count");
    private int old_r = -1;
    final ClipboardContent content = new ClipboardContent();

    public static final ObservableList<Item> itemData = FXCollections.observableArrayList();
    public static final ArrayList<Item> allItems = new ArrayList<>();

    public static final ObservableList<Product> productData = FXCollections.observableArrayList();
    public static final ArrayList<Product> allProducts = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources) {

        priStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                try {
                    deleteButtonPressed();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        invCat = "Items";
        priStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), this::copyDataFromCell);
        selectedCell.setOnAction(actionEvent -> copyDataFromCell());
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(selectedCell);
        tableViewItems.setContextMenu(menu);
        tableViewItems.getSelectionModel().setCellSelectionEnabled(true);
        tableViewProducts.setContextMenu(menu);
        tableViewProducts.getSelectionModel().setCellSelectionEnabled(true);

        tableViewProducts.setVisible(false);
        tableViewProducts.setDisable(true);

        sku.prefWidthProperty().bind(tableViewItems.widthProperty().divide(14));
        name.prefWidthProperty().bind(tableViewItems.widthProperty().divide(7));
        description.prefWidthProperty().bind(tableViewItems.widthProperty().divide(2));
        category.prefWidthProperty().bind(tableViewItems.widthProperty().divide(14));
        cost.prefWidthProperty().bind(tableViewItems.widthProperty().divide(14));
        markup.prefWidthProperty().bind(tableViewItems.widthProperty().divide(14));
        stock.prefWidthProperty().bind(tableViewItems.widthProperty().divide(14));

        skuProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(14));
        nameProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(7));
        descriptionProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(2));
        categoryProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(14));
        costProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(14));
        markupProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(14));
        stockProduct.prefWidthProperty().bind(tableViewProducts.widthProperty().divide(14));

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
        if (invCat.equals("Items")) {
            ObservableList<TablePosition> posList = tableViewItems.getSelectionModel().getSelectedCells();
            StringBuilder clipboardString = new StringBuilder();
            for (TablePosition p : posList) {
                int r = p.getRow();
                int c = p.getColumn();
                Object cell = tableViewItems.getColumns().get(c).getCellData(r);
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
        } else if (invCat.equals("Products")) {
            ObservableList<TablePosition> posList = tableViewProducts.getSelectionModel().getSelectedCells();
            StringBuilder clipboardString = new StringBuilder();
            for (TablePosition p : posList) {
                int r = p.getRow();
                int c = p.getColumn();
                Object cell = tableViewProducts.getColumns().get(c).getCellData(r);
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
        }
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void hideAdminOptions() {
        buttonDelete.setDisable(true);
        buttonDelete.setVisible(false);
    }

    private void hideManagerOptions() {
        buttonAdd.setDisable(true);
        buttonAdd.setVisible(false);
        buttonEdit.setDisable(true);
        buttonEdit.setVisible(false);
        buttonDelete.setDisable(true);
        buttonDelete.setVisible(false);
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

    public void itemsCatButtonPressed() {
        tableViewItems.getItems().clear();
        tableViewProducts.getItems().clear();
        invCat = "Items";
        buttonSearch.setText("Search Item");
        buttonAdd.setText("Add Item");
        buttonEdit.setText("Edit Item");
        buttonDelete.setText("Delete Item");
        textField.setPromptText("Leave blank for all items");
        textField.setPrefWidth(150);
        tableViewProducts.setVisible(false);
        tableViewProducts.setDisable(true);
        tableViewItems.setVisible(true);
        tableViewItems.setDisable(false);
    }

    public void productsCatButtonPressed() {
        tableViewItems.getItems().clear();
        tableViewProducts.getItems().clear();
        invCat = "Products";
        buttonSearch.setText("Search Product");
        buttonAdd.setText("Add Product");
        buttonEdit.setText("Edit Product");
        buttonDelete.setText("Delete Product");
        textField.setPromptText("Leave blank for all products");
        textField.setPrefWidth(165);
        tableViewProducts.setVisible(true);
        tableViewProducts.setDisable(false);
        tableViewItems.setVisible(false);
        tableViewItems.setDisable(true);
    }

    public void searchButtonPressed() throws SQLException {
        if (invCat.equals("Items")) {
            itemData.clear();
            getAllItems();
            sku.setCellValueFactory(new PropertyValueFactory<>("sku"));
            name.setCellValueFactory(new PropertyValueFactory<>("name"));
            description.setCellValueFactory(new PropertyValueFactory<>("description"));
            category.setCellValueFactory(new PropertyValueFactory<>("category"));
            cost.setCellValueFactory(new PropertyValueFactory<>("cost"));
            markup.setCellValueFactory(new PropertyValueFactory<>("markup"));
            stock.setCellValueFactory(new PropertyValueFactory<>("curStock"));
            // Filter items by search string
            if (!textField.getText().isEmpty()) {
                for (Item check : allItems) {
                    String allProperties = check.getSku() + " " + check.getName() + " " + check.getDescription() + " " + check.getCategory() + " "
                            + check.getCost() + " " + check.getMarkup() + " " + check.getCurStock();
                    if (allProperties.toLowerCase(Locale.ROOT).contains(textField.getText().toLowerCase(Locale.ROOT))) {
                        itemData.add(check);
                    }
                }
            } else {
                itemData.addAll(allItems);
            }

            // Add relevant data to table.
            tableViewItems.setItems(itemData);
        } else if (invCat.equals("Products")) {
            productData.clear();
            getAllProducts();
            skuProduct.setCellValueFactory(new PropertyValueFactory<>("sku"));
            nameProduct.setCellValueFactory(new PropertyValueFactory<>("name"));
            descriptionProduct.setCellValueFactory(new PropertyValueFactory<>("description"));
            categoryProduct.setCellValueFactory(new PropertyValueFactory<>("category"));
            costProduct.setCellValueFactory(new PropertyValueFactory<>("cost"));
            markupProduct.setCellValueFactory(new PropertyValueFactory<>("markup"));
            stockProduct.setCellValueFactory(new PropertyValueFactory<>("possibleStock"));
            // Filter products by search string
            if (!textField.getText().isEmpty()) {
                for (Product check : allProducts) {
                    String allProperties = check.getSku() + " " + check.getName() + " " + check.getDescription() + " " + check.getCategory() + " "
                            + check.getCost() + " " + check.getMarkup() + " " + check.getPossibleStock();
                    if (allProperties.toLowerCase(Locale.ROOT).contains(textField.getText().toLowerCase(Locale.ROOT))) {
                        productData.add(check);
                    }
                }
            } else {
                productData.addAll(allProducts);
            }

            // Add relevant data to table.
            tableViewProducts.setItems(productData);
        }

    }

    public void addButtonPressed() throws IOException {
        if (invCat.equals("Items")) {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/addItem.fxml")));
            Stage addItemStage = new Stage();
            addItemStage.setTitle("Add Item");
            addItemStage.setScene(new Scene(root, 800, 600));
            addItemStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
            addItemStage.setResizable(false);
            addItemStage.initModality(Modality.APPLICATION_MODAL);
            addItemStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            addItemStage.show();
        } else if (invCat.equals("Products")) {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/addProduct.fxml")));
            Stage addProductStage = new Stage();
            addProductStage.setTitle("Add Product");
            addProductStage.setScene(new Scene(root, 800, 600));
            addProductStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
            addProductStage.setResizable(false);
            addProductStage.initModality(Modality.APPLICATION_MODAL);
            addProductStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            addProductStage.show();
        }

    }

    public void editButtonPressed() throws IOException, SQLException {
        if (invCat.equals("Items")) {
            if (tableViewItems.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("EDIT ITEM");
                alert.setContentText("Item must be selected to edit!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("../views/editItem.fxml")));
            Parent root = loader.load();
            Stage editItemStage = new Stage();
            editItemStage.setTitle("Edit Item");
            editItemStage.setScene(new Scene(root, 800, 600));
            editItemStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
            editItemStage.setResizable(false);
            EditItemController controller = loader.getController();
            controller.initData(tableViewItems.getSelectionModel().getSelectedItem().getName());
            editItemStage.initModality(Modality.APPLICATION_MODAL);
            editItemStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            editItemStage.showAndWait();
        } else {
            if (tableViewProducts.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("EDIT PRODUCT");
                alert.setContentText("Product must be selected to edit!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("../views/editProduct.fxml")));
            Parent root = loader.load();
            Stage editProductStage = new Stage();
            editProductStage.setTitle("Edit Product");
            editProductStage.setScene(new Scene(root, 800, 600));
            editProductStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
            editProductStage.setResizable(false);
            EditProductController controller = loader.getController();
            controller.initData(tableViewProducts.getSelectionModel().getSelectedItem().getName());
            editProductStage.initModality(Modality.APPLICATION_MODAL);
            editProductStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            editProductStage.showAndWait();
        }
        searchButtonPressed();
    }

    public void deleteButtonPressed() throws SQLException {
        if (invCat.equals("Items")) {
            if (tableViewItems.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("DELETE ITEM");
                alert.setContentText("Item must be selected to delete!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                return;
            }
            String item = tableViewItems.getSelectionModel().getSelectedItem().getName();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("DELETE ITEM");
            alert.setContentText("Are you sure you want to delete " + item + "?" + "\nThis action cannot be undone!");
            DialogPane dialogPane = alert.getDialogPane();
            ((Button) dialogPane.lookupButton(ButtonType.OK)).setText("Yes");
            ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("No");
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElseThrow() == ButtonType.OK) {
                String deleteStatement = "DELETE FROM hydra_items WHERE Name = '" + item + "'";
                DBQuery.setPreparedStatement(conn, deleteStatement);
                PreparedStatement ps = DBQuery.getPreparedStatement();
                ps.execute();
            }
        } else {
            if (tableViewProducts.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("DELETE ITEM");
                alert.setContentText("Product must be selected to delete!");
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
                Stage alertStage = (Stage) dialogPane.getScene().getWindow();
                alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
                alert.showAndWait();
                return;
            }
            String product = tableViewProducts.getSelectionModel().getSelectedItem().getName();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("DELETE PRODUCT");
            alert.setContentText("Are you sure you want to delete " + product + "?" + "\nThis action cannot be undone!");
            DialogPane dialogPane = alert.getDialogPane();
            ((Button) dialogPane.lookupButton(ButtonType.OK)).setText("Yes");
            ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).setText("No");
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/dialog.css")).toExternalForm());
            Stage alertStage = (Stage) dialogPane.getScene().getWindow();
            alertStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElseThrow() == ButtonType.OK) {
                String productID = "";
                String getIDStatement = "SELECT ID FROM hydra_products WHERE Name = '" + product + "'";
                DBQuery.setPreparedStatement(conn, getIDStatement);
                PreparedStatement getIDPS = DBQuery.getPreparedStatement();
                getIDPS.execute();
                ResultSet getIDRS = getIDPS.getResultSet();
                while (getIDRS.next()) {
                    productID = getIDRS.getString("ID");
                }

                String deleteStatement = "DELETE FROM hydra_products WHERE Name = '" + product + "'";
                DBQuery.setPreparedStatement(conn, deleteStatement);
                PreparedStatement ps = DBQuery.getPreparedStatement();
                ps.execute();

                // Also delete from hydra_product_items
                String deleteStatement2 = "DELETE FROM hydra_product_items WHERE Product_ID = '" + productID + "'";
                DBQuery.setPreparedStatement(conn, deleteStatement2);
                PreparedStatement ps2 = DBQuery.getPreparedStatement();
                ps2.execute();
            }
        }
        searchButtonPressed();
    }

    public static void getAllItems() throws SQLException {
        allItems.clear();
        String searchStatement = "SELECT * FROM hydra_items";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while(rs.next()) {
            allItems.add(new Item(Integer.parseInt(rs.getString("ID")),
                            rs.getString("SKU"),
                            rs.getString("Name"),
                            rs.getString("Description"),
                            rs.getString("Category"),
                            new BigDecimal(rs.getString("Cost")),
                            Double.parseDouble(rs.getString("Markup")),
                            rs.getString("Vendor"),
                            Integer.parseInt(rs.getString("Cur_Stock")),
                            Integer.parseInt(rs.getString("Min_Stock")),
                            Integer.parseInt(rs.getString("Max_Stock")),
                            Double.parseDouble(rs.getString("Amount_Per_Unit")),
                            rs.getString("Measurement"),
                            rs.getBoolean("Sellable")

                    )
            );
        }
    }

    public static void getAllProducts() throws SQLException {
        allProducts.clear();
        String searchStatement = "SELECT * FROM hydra_products";
        DBQuery.setPreparedStatement(conn, searchStatement);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while(rs.next()) {
            allProducts.add(new Product(Integer.parseInt(rs.getString("ID")),
                            rs.getString("SKU"),
                            rs.getString("Name"),
                            rs.getString("Description"),
                            rs.getString("Category"),
                            new BigDecimal(rs.getString("Cost")),
                            Double.parseDouble(rs.getString("Markup"))
                    )
            );
        }
    }
}
