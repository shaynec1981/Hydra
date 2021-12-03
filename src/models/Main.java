package models;

import com.mysql.jdbc.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.DBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends Application {

    public static Connection conn;
    public static String currentUser;
    public static int privilege;
    public static Stage priStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        priStage = primaryStage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../views/dashboard.fxml")));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/hydraIcon.png"))));
        primaryStage.setTitle("Hydra");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("../css/hydraStyle.css")).toString());
        primaryStage.setMinWidth(1038);
        primaryStage.setMinHeight(665);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        conn = DBConnection.startConnection();
        launch(args);
        pdfCleanup();
        DBConnection.closeConnection();
    }

    private static void pdfCleanup() throws IOException {
        List<String> files = findFiles(Paths.get("src/resources"));
        files.forEach(pdfFile -> {
            if (!pdfFile.equals("src\\resources\\invoicetemplate.pdf")) {
                System.out.println(pdfFile);
                File pdfToDelete = new File(pdfFile);
                if (pdfToDelete.delete()) {
                    System.out.println(pdfToDelete.getName() + " deleted successfully.");
                } else {
                    System.out.println(pdfToDelete.getName() + " failed to delete.");
                }
            }
        });
    }

    private static List<String> findFiles(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory.");
        }
        List<String> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith("pdf"))
                    .collect(Collectors.toList());
        }
        return result;
    }

}
