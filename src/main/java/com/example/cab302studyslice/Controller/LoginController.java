package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.DatabaseManager;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private static String registerMessage = "";

    // Displays a message if the user has just created an account
    @FXML
    public void initialize() {
        if (!registerMessage.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #819D93;");
            messageLabel.setText(registerMessage);
            registerMessage = "";
        }
    }

    // Checks the login details and moves to the home page if they are correct
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        boolean validUser = DatabaseManager.validateLogin(username, password);

        if (validUser) {
            ViewManager.switchScene("home-view.fxml");
        } else {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText("Username or password was incorrect.");
        }
    }

    // Opens the register page
    @FXML
    private void handleGoToRegister() {
        ViewManager.switchScene("register-view.fxml");
    }

    public static void setRegisterMessage(String message) {
        registerMessage = message;
    }
}