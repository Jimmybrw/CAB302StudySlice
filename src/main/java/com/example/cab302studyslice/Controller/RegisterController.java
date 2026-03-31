package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.DatabaseManager;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    // Creates a new account if the inputs are valid
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Passwords do not match.");
            return;
        }

        if (DatabaseManager.userExists(username)) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Username already exists.");
            return;
        }

        boolean registered = DatabaseManager.registerUser(username, password);

        if (registered) {
            LoginController.setRegisterMessage("Account created successfully. Please log in.");
            ViewManager.switchScene("login-view.fxml");
        } else {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Could not create account.");
        }
    }

    // Returns to the login page
    @FXML
    private void handleBackToLogin() {
        ViewManager.switchScene("login-view.fxml");
    }
}