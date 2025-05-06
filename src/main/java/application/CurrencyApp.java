package application;

import dao.CurrencyDao;
import entity.Currency;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.Optional;

public class CurrencyApp extends Application {

    private CurrencyDao currencyDao = new CurrencyDao();
    private ComboBox<Currency> fromCurrencyComboBox = new ComboBox<>();
    private ComboBox<Currency> toCurrencyComboBox = new ComboBox<>();
    private TextField amountField = new TextField();
    private Label resultLabel = new Label("Result will be shown here");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("Currency Converter");

            // Create layout
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(10, 10, 10, 10));
            grid.setVgap(8);
            grid.setHgap(10);

            // Labels
            Label amountLabel = new Label("Amount:");
            GridPane.setConstraints(amountLabel, 0, 0);

            Label fromLabel = new Label("From Currency:");
            GridPane.setConstraints(fromLabel, 0, 1);

            Label toLabel = new Label("To Currency:");
            GridPane.setConstraints(toLabel, 0, 2);

            // Amount input
            amountField.setPromptText("Enter amount");
            GridPane.setConstraints(amountField, 1, 0);

            // From currency dropdown
            GridPane.setConstraints(fromCurrencyComboBox, 1, 1);

            // To currency dropdown
            GridPane.setConstraints(toCurrencyComboBox, 1, 2);

            // Result display
            GridPane.setConstraints(resultLabel, 1, 3);

            // Convert button
            Button convertButton = new Button("Convert");
            convertButton.setOnAction(e -> convertCurrency());
            GridPane.setConstraints(convertButton, 1, 4);

            // Add currency button
            Button addCurrencyButton = new Button("Add New Currency");
            addCurrencyButton.setOnAction(e -> openAddCurrencyWindow());
            GridPane.setConstraints(addCurrencyButton, 1, 5);

            grid.getChildren().addAll(
                amountLabel, amountField, 
                fromLabel, fromCurrencyComboBox, 
                toLabel, toCurrencyComboBox, 
                resultLabel, convertButton, addCurrencyButton
            );

            // Load currencies from database
            updateCurrencies();

            Scene scene = new Scene(grid, 400, 250);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            showError("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void convertCurrency() {
        try {
            if (amountField.getText().isEmpty()) {
                showError("Please enter an amount");
                return;
            }

            Currency fromCurrency = fromCurrencyComboBox.getValue();
            Currency toCurrency = toCurrencyComboBox.getValue();

            if (fromCurrency == null || toCurrency == null) {
                showError("Please select both currencies");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            
            // Convert to EUR first (base currency)
            double amountInEUR = amount / fromCurrency.getExchangeRate();
            
            // Then convert from EUR to target currency
            double result = amountInEUR * toCurrency.getExchangeRate();
            
            resultLabel.setText(String.format("%.2f %s = %.2f %s", 
                amount, fromCurrency.getCode(), result, toCurrency.getCode()));
        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
        } catch (Exception e) {
            showError("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCurrencies() {
        try {
            List<Currency> currencies = currencyDao.findAll();
            
            fromCurrencyComboBox.getItems().clear();
            toCurrencyComboBox.getItems().clear();
            
            fromCurrencyComboBox.getItems().addAll(currencies);
            toCurrencyComboBox.getItems().addAll(currencies);
            
            // Set default selection if possible
            if (!currencies.isEmpty()) {
                fromCurrencyComboBox.setValue(currencies.get(0));
                toCurrencyComboBox.setValue(currencies.get(0));
            }
        } catch (PersistenceException e) {
            showError("Failed to connect to the database. Please check your database connection.");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Error loading currencies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAddCurrencyWindow() {
        Stage newStage = new Stage();
        newStage.setTitle("Add New Currency");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Currency code input
        Label codeLabel = new Label("Currency Code (e.g., USD):");
        GridPane.setConstraints(codeLabel, 0, 0);
        
        TextField codeField = new TextField();
        codeField.setPromptText("Enter currency code");
        GridPane.setConstraints(codeField, 1, 0);

        // Currency name input
        Label nameLabel = new Label("Currency Name:");
        GridPane.setConstraints(nameLabel, 0, 1);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter currency name");
        GridPane.setConstraints(nameField, 1, 1);

        // Exchange rate input
        Label rateLabel = new Label("Exchange Rate (to EUR):");
        GridPane.setConstraints(rateLabel, 0, 2);
        
        TextField rateField = new TextField();
        rateField.setPromptText("Enter exchange rate");
        GridPane.setConstraints(rateField, 1, 2);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        GridPane.setConstraints(buttonBox, 1, 3);

        grid.getChildren().addAll(
            codeLabel, codeField,
            nameLabel, nameField,
            rateLabel, rateField,
            buttonBox
        );

        // Save button action
        saveButton.setOnAction(e -> {
            try {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();
                
                if (code.isEmpty() || name.isEmpty()) {
                    showError("Currency code and name are required");
                    return;
                }
                
                double rate;
                try {
                    rate = Double.parseDouble(rateField.getText().trim());
                    if (rate <= 0) {
                        showError("Exchange rate must be greater than zero");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showError("Please enter a valid exchange rate");
                    return;
                }
                
                // Save new currency to database
                Currency newCurrency = new Currency(code, name, rate);
                currencyDao.persist(newCurrency);
                
                newStage.close();
                
                // Update currency lists
                updateCurrencies();
            } catch (Exception ex) {
                showError("Error saving currency: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Cancel button action
        cancelButton.setOnAction(e -> newStage.close());

        Scene scene = new Scene(grid, 400, 200);
        newStage.setScene(scene);
        newStage.showAndWait(); // Will block until the window is closed
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
