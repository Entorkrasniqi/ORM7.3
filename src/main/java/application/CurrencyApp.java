package application;

import dao.CurrencyDao;
import entity.Currency;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class CurrencyApp extends Application {

    private CurrencyDao currencyDao = new CurrencyDao();
    private ListView<String> currencyListView = new ListView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Currency Converter");

        Button addCurrencyButton = new Button("Add Currency");
        addCurrencyButton.setOnAction(e -> openAddCurrencyWindow());

        VBox layout = new VBox(10, currencyListView, addCurrencyButton);
        updateCurrencyList();

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateCurrencyList() {
        try {
            List<Currency> currencies = currencyDao.findAll();
            currencyListView.getItems().clear();
            for (Currency currency : currencies) {
                currencyListView.getItems().add(currency.getCode() + " - " + currency.getName());
            }
        } catch (Exception e) {
            showError("Failed to retrieve currency data from the database.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openAddCurrencyWindow() {
        Stage newStage = new Stage();
        newStage.setTitle("Add New Currency");

        TextField codeField = new TextField();
        codeField.setPromptText("Currency Code");

        TextField nameField = new TextField();
        nameField.setPromptText("Currency Name");

        TextField rateField = new TextField();
        rateField.setPromptText("Exchange Rate");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String code = codeField.getText();
            String name = nameField.getText();
            double rate = Double.parseDouble(rateField.getText());

            currencyDao.persist(new Currency(code, name, rate));
            newStage.close();
            updateCurrencyList();
        });

        VBox layout = new VBox(10, codeField, nameField, rateField, saveButton);
        Scene scene = new Scene(layout, 300, 200);
        newStage.setScene(scene);
        newStage.showAndWait();
    }
}
